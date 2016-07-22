/*
 * Copyright © 2016 Tata Consultancy Services and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.natapp.impl;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.ArrayList;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatFlow {

    private DataBroker dataBroker;

    private static final Logger LOG = LoggerFactory.getLogger(PatFlow.class);

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void createFlow(NodeId nodeId, NodeConnectorId inPort, NodeConnectorId outPort, int srcPort, int dstPort,
            String srcIP, String dstIP) {

        // Ethernet Match
        EthernetType ethTypeBuilder = new EthernetTypeBuilder().setType(new EtherType(0x0800L)).build();
        EthernetMatch eth = new EthernetMatchBuilder().setEthernetType(ethTypeBuilder).build();

        // IP protocol match
        IpMatch ipmatch = new IpMatchBuilder().setIpProtocol((short) 6).build();

        // TCP port match
        TcpMatchBuilder tcpSrcMatchBuilder = new TcpMatchBuilder().setTcpSourcePort(new PortNumber(srcPort));

        TcpMatchBuilder tcpDstMatchBuilder = new TcpMatchBuilder().setTcpDestinationPort(new PortNumber(dstPort));

        MatchBuilder matchBuilderSrc = new MatchBuilder().setInPort(inPort).setEthernetMatch(eth)
                .setIpMatch(ipmatch).setLayer4Match(tcpSrcMatchBuilder.build());

        MatchBuilder matchBuilderDst = new MatchBuilder().setInPort(outPort).setEthernetMatch(eth)
                .setIpMatch(ipmatch).setLayer4Match(tcpDstMatchBuilder.build());

        List<Action> actionListSrc = new ArrayList<Action>();

        // Action to set Network Source IP to Global
        Ipv4Builder ipsrc = new Ipv4Builder().setIpv4Address(new Ipv4Prefix(srcIP));
        SetNwSrcActionBuilder setNwsrcActionBuilder = new SetNwSrcActionBuilder().setAddress(ipsrc.build());
        Action nwSrcIpAction = new ActionBuilder().setOrder(0)
                .setAction(new SetNwSrcActionCaseBuilder().setSetNwSrcAction(setNwsrcActionBuilder.build()).build())
                .build();

        // Action to set TCP Source Port
        SetTpSrcActionBuilder setTpSrcActionBuilder = new SetTpSrcActionBuilder().setPort(new PortNumber(dstPort));
        Action tpSrcAction = new ActionBuilder().setOrder(1)
                .setAction(new SetTpSrcActionCaseBuilder().setSetTpSrcAction(setTpSrcActionBuilder.build()).build()).build();

        // Action to Output the packets
        Action srcOutputAction = new ActionBuilder().setOrder(2)
                .setAction(new OutputActionCaseBuilder().setOutputAction(new OutputActionBuilder()
                        .setMaxLength(Integer.valueOf(0xffff)).setOutputNodeConnector(outPort).build()).build())
                .build();

        actionListSrc.add(nwSrcIpAction);
        actionListSrc.add(tpSrcAction);
        actionListSrc.add(srcOutputAction);
        LOG.info("Action List 1: " + actionListSrc);

        List<Action> actionListDst = Lists.newArrayList();

        // Action to set Network Destination IP to Global
        Ipv4Builder ipDst = new Ipv4Builder().setIpv4Address(new Ipv4Prefix(dstIP));
        SetNwDstActionBuilder setNwDstActionBuilder = new SetNwDstActionBuilder().setAddress(ipDst.build());
        Action nwDstIpAction = new ActionBuilder().setOrder(0)
                .setAction(new SetNwDstActionCaseBuilder().setSetNwDstAction(setNwDstActionBuilder.build()).build())
                .build();

        // Action to set TCP Destination Port
        SetTpDstActionBuilder setTpDstActionBuilder = new SetTpDstActionBuilder().setPort(new PortNumber(srcPort));
        Action tpDstAction = new ActionBuilder().setOrder(1)
                .setAction(new SetTpDstActionCaseBuilder().setSetTpDstAction(setTpDstActionBuilder.build()).build()).build();

        // Action to Output the packets
        Action dstOutputAction = new ActionBuilder().setOrder(2)
                .setAction(new OutputActionCaseBuilder().setOutputAction(new OutputActionBuilder()
                        .setMaxLength(Integer.valueOf(0xffff)).setOutputNodeConnector(inPort).build()).build())
                .build();

        actionListDst.add(nwDstIpAction);
        actionListDst.add(tpDstAction);
        actionListDst.add(dstOutputAction);

        LOG.info("Action List 2: " + actionListDst);
        // Create an Apply Action
        ApplyActionsBuilder aab1 = new ApplyActionsBuilder().setAction(actionListSrc);

        ApplyActionsBuilder aab2 = new ApplyActionsBuilder().setAction(actionListDst);

        InstructionsBuilder isb1 = new InstructionsBuilder();
        List<Instruction> instruction1 = Lists.newArrayList();
        Instruction applyActionsInstruction1 = new InstructionBuilder().setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab1.build()).build()).build();

        InstructionsBuilder isb2 = new InstructionsBuilder();
        List<Instruction> instruction2 = Lists.newArrayList();
        Instruction applyActionsInstruction2 = new InstructionBuilder().setOrder(1)
                .setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab2.build()).build()).build();

        instruction1.add(applyActionsInstruction1);
        instruction2.add(applyActionsInstruction2);

        String flowIdString1 = "FlowSrc";
        FlowId flowId1 = new FlowId(flowIdString1);
        FlowKey key1 = new FlowKey(flowId1);

        FlowBuilder flowBuilder1 = new FlowBuilder().setMatch(matchBuilderSrc.build()).setId(new FlowId(flowId1))
                .setBarrier(true).setTableId((short) 0).setKey(key1).setPriority(210).setFlowName(flowIdString1)
                .setHardTimeout(0).setIdleTimeout(0).setId(flowId1)
                .setInstructions(isb1.setInstruction(instruction1).build());

        LOG.info("Flow Builder 1: Instruction " + flowBuilder1.getInstructions());

        String flowIdString2 = "FlowDst";
        FlowId flowId2 = new FlowId(flowIdString2);
        FlowKey key2 = new FlowKey(flowId2);

        FlowBuilder flowBuilder2 = new FlowBuilder().setMatch(matchBuilderDst.build()).setId(new FlowId(flowId2))
                .setBarrier(true).setTableId((short) 0).setKey(key2).setPriority(209).setFlowName(flowIdString2)
                .setHardTimeout(0).setIdleTimeout(0).setId(flowId2)
                .setInstructions(isb2.setInstruction(instruction2).build());

        NodeKey nodeKey = new NodeKey(nodeId);

        InstanceIdentifier<Flow> flowIID1 = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey)
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(flowBuilder1.getTableId()))
                .child(Flow.class, flowBuilder1.getKey()).build();

        InstanceIdentifier<Flow> flowIID2 = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey)
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(flowBuilder2.getTableId()))
                .child(Flow.class, flowBuilder2.getKey()).build();

        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, flowIID1, flowBuilder1.build(), true);
        writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, flowIID2, flowBuilder2.build(), true);
        writeTransaction.commit();
        LOG.info("Flows created");

    }
}