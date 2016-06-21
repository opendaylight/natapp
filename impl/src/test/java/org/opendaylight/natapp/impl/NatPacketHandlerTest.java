/*
 * Copyright © 2016 Tata Consultancy Services and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.natapp.impl;

import java.util.concurrent.Future;
import java.lang.InterruptedException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceivedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.nat.type.input.NatType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.NatTypeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.NatTypeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.nat.type.input.nat.type.StaticBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

//import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.Matchers.*;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

public class NatPacketHandlerTest {

	private NatPacketHandler natPacketHandler;

	@MockitoAnnotations.Mock
	private DataBroker dataBroker;
	@MockitoAnnotations.Mock
	private PacketProcessingService packetProcessingService;
	@Mock
	NodeId ingressNodeId;
	@Mock
	NodeConnectorId ingressNodeConnectorId;
	@Mock
	NatYangStore natYangStore;
	@Mock
	NatFlow natFlow;

	InstanceIdentifier<NodeConnector> value0 = InstanceIdentifier.builder(Nodes.class)
			.child(Node.class, new NodeKey(ingressNodeId))
			.child(NodeConnector.class, new NodeConnectorKey(ingressNodeConnectorId)).toInstance();

	final NodeConnectorRef value1 = new NodeConnectorRef(value0);
	final byte[] value2 = { (byte) 204, 29, (byte) 207, (byte) 217 };

	@Before
	public void initMocks() throws Exception {
		MockitoAnnotations.initMocks(this);
		natPacketHandler = new NatPacketHandler();
		natPacketHandler.setdataBroker(dataBroker);
	}
	
	@Test
	public void testNatType() throws InterruptedException, Exception{
		NatTypeInputBuilder input = new NatTypeInputBuilder();
		StaticBuilder staticBuilder = new StaticBuilder();
		staticBuilder.setStatic(true);
		NatType natType = staticBuilder.build();
		input.setNatType(natType);
		Future<RpcResult<java.lang.Void>> futureNat = natPacketHandler.natType(input.build());
		RpcResult<java.lang.Void> rpcNat = futureNat.get();
		assertTrue(staticBuilder.build().isStatic());
	}

	/*@Test
	public void TestonPacketReceived() throws Exception {

		//when(natYangStore.addStaticMap(anyString()).thenReturn("172.0.0.1/32"));
		PacketReceivedBuilder notification = new PacketReceivedBuilder();
		notification.setIngress(value1);
		notification.setPayload(value2);
		natPacketHandler.onPacketReceived(notification.build());

		verify(natYangStore).addStaticMap(anyString());
		verify(natYangStore).addDynamicMap(anyString());
		verify(natFlow).createFlow(any(NodeId.class), any(NodeConnectorId.class), any(NodeConnectorId.class),
				anyString(), anyString(),anyInt());
	}*/

}
