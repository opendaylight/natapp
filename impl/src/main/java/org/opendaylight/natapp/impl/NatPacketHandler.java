/*
 * Copyright © 2016 Tata Consultancy Services and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.natapp.impl;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.NatappService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.NatTypeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.nat.type.input.NatType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.nat.type.input.nat.type.Dynamic;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.nat.type.input.nat.type.Static;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NatPacketHandler implements PacketProcessingListener, NatappService {

	private static final Logger LOG = LoggerFactory.getLogger(NatPacketHandler.class);
	private DataBroker dataBroker;
	private PacketProcessingService packetProcessingService;
	private static NatType type;
	private NatFlow natFlow = new NatFlow();
	String srcIP, dstIP, globalIP, ingressNode;
	byte[] payload, srcIPRaw, dstIPRaw,rawSrcPort;
	private NodeConnectorRef ingressNodeConnectorRef;
	private NodeId ingressNodeId;
	private NodeConnectorId ingressNodeConnectorId;

	public DataBroker getdataBroker() {
		return dataBroker;
	}

	public void setdataBroker(DataBroker dataBroker) {
		this.dataBroker = dataBroker;
	}

	public void setPacketProcessingService(PacketProcessingService packetProcessingService) {
		this.packetProcessingService = packetProcessingService;
	}

	public Future<RpcResult<java.lang.Void>> natType(NatTypeInput input) {
		type = input.getNatType();
		return Futures.immediateFuture(RpcResultBuilder.<Void> success().build());
	}

	@Override
	public void onPacketReceived(PacketReceived notification) {

		LOG.info("Packet Received");
		natFlow.setDataBroker(dataBroker);
		NatYangStore natYangStore = new NatYangStore(dataBroker);
		
		// packet parsing
		ingressNodeConnectorRef = notification.getIngress();
		ingressNodeConnectorId = NatInventoryUtility.getNodeConnectorId(ingressNodeConnectorRef);
		ingressNodeId = NatInventoryUtility.getNodeId(ingressNodeConnectorRef);
		ingressNode = ingressNodeId.getValue();
		payload = notification.getPayload();
		dstIPRaw = NatPacketParsing.extractDstIP(payload);
		srcIPRaw = NatPacketParsing.extractSrcIP(payload);
		dstIP = NatPacketParsing.rawIPToString(dstIPRaw);
		srcIP = NatPacketParsing.rawIPToString(srcIPRaw);
		rawSrcPort = NatPacketParsing.extractSrcPort(payload);
		NodeConnectorId outPort = new NodeConnectorId("openflow:1:5");
		LOG.info("Packet Details: DstIP {}, SrcIP {}, Ingress Node {}, NatType {} ", dstIP, srcIP, ingressNode, type);

		// create Static flow
		if (type instanceof Static) {
			Static staticType = (Static) type;
			if (staticType.isStatic()) {
				String staticIP = natYangStore.addStaticMap(srcIP);
				dstIP += "/32";
				LOG.info("Static flow creation");
				natFlow.createFlow(ingressNodeId, ingressNodeConnectorId, outPort, staticIP, dstIP, 0);
			}
		}else if (type instanceof Dynamic) {
			Dynamic dynamicType = (Dynamic) type;
			if (dynamicType.isDynamic()) {
				String dynamicIP = natYangStore.addDynamicMap(srcIP);
				dstIP += "/32";
				int idleTimeout = 20;
				LOG.info("Dynamic flow creation");
				natFlow.createFlow(ingressNodeId, ingressNodeConnectorId, outPort, dynamicIP, dstIP,idleTimeout);
			}
		} 
		
		// Sending packet out
		InstanceIdentifier<NodeConnector> instanceIdentifier = InstanceIdentifier.builder(Nodes.class)
				.child(Node.class, new NodeKey(ingressNodeId))
				.child(NodeConnector.class, new NodeConnectorKey(ingressNodeConnectorId)).toInstance();
		NodeConnectorRef egress = new NodeConnectorRef(instanceIdentifier);

		sendPacketOut(notification.getPayload(), notification.getIngress(), egress);
	}

	private void sendPacketOut(byte[] payload, NodeConnectorRef ingress, NodeConnectorRef egress) {

		InstanceIdentifier<Node> egressNodePath = getNodePath(egress.getValue());
		TransmitPacketInput input = new TransmitPacketInputBuilder().setPayload(payload)
				.setNode(new NodeRef(egressNodePath)).setEgress(egress).setIngress(ingress).build();
		packetProcessingService.transmitPacket(input);
		LOG.info("Packet Sent");
	}

	public static final InstanceIdentifier<Node> getNodePath(final InstanceIdentifier<?> nodeChild) {
		return nodeChild.firstIdentifierOf(Node.class);
	}

}
