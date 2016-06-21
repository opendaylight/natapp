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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatFlow {

    private DataBroker dataBroker;

    private static final Logger LOG = LoggerFactory.getLogger(PatFlow.class);

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void createFlow(NodeId nodeId, NodeConnectorId inPort, NodeConnectorId outPort, int tcpPort, String srcIP,
            String dstIP, int idleTimeOut) {
        // set inport, outport and IP

        EthernetType ethTypeBuilder = new EthernetTypeBuilder().setType(new EtherType(0x0800L)).build();

        EthernetMatch eth = new EthernetMatchBuilder().setEthernetType(ethTypeBuilder).build();

        TcpMatchBuilder tcpMatchBuilder = new TcpMatchBuilder();
        tcpMatchBuilder.setTcpSourcePort(new PortNumber(tcpPort));

        MatchBuilder matchBuilder1 = new MatchBuilder().setInPort(inPort).setLayer4Match(tcpMatchBuilder.build());

        MatchBuilder matchBuilder2 = new MatchBuilder().setInPort(outPort);

        List<Action> actionList1 = new ArrayList<Action>();

        Action action1 = new ActionBuilder().setOrder(1)
                .setAction(new OutputActionCaseBuilder().setOutputAction(new OutputActionBuilder()
                        .setMaxLength(Integer.valueOf(0xffff)).setOutputNodeConnector(outPort).build()).build())
                .build();

        // Setting new Src Ip to Global
        Ipv4Builder ipsrc = new Ipv4Builder().setIpv4Address(new Ipv4Prefix(srcIP));

        SetNwSrcActionBuilder setNwsrcActionBuilder = new SetNwSrcActionBuilder().setAddress(ipsrc.build());

        Action action11 = new ActionBuilder().setOrder(0)
                .setAction(new SetNwSrcActionCaseBuilder().setSetNwSrcAction(setNwsrcActionBuilder.build()).build())
                .build();

        actionList1.add(action11);
        actionList1.add(action1);

        LOG.info("Action List 1: " + actionList1);

        List<Action> actionList2 = Lists.newArrayList();

        Action action2 = new ActionBuilder().setOrder(1)
                .setAction(new OutputActionCaseBuilder().setOutputAction(new OutputActionBuilder()
                        .setMaxLength(Integer.valueOf(0xffff)).setOutputNodeConnector(outPort).build()).build())
                .build();

        // Setting new Dst Ip to Global
        Ipv4Builder ipDst = new Ipv4Builder().setIpv4Address(new Ipv4Prefix(dstIP));

        SetNwDstActionBuilder setNwDstActionBuilder = new SetNwDstActionBuilder().setAddress(ipDst.build());

        Action action21 = new ActionBuilder().setOrder(0)
                .setAction(new SetNwDstActionCaseBuilder().setSetNwDstAction(setNwDstActionBuilder.build()).build())
                .build();

        actionList2.add(action21);
        actionList2.add(action2);

        LOG.info("Action List 2: " + actionList2);
        // Create an Apply Action
        ApplyActionsBuilder aab1 = new ApplyActionsBuilder().setAction(actionList1);

        ApplyActionsBuilder aab2 = new ApplyActionsBuilder().setAction(actionList2);

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

        FlowBuilder flowBuilder1 = new FlowBuilder().setMatch(matchBuilder1.build()).setId(new FlowId(flowId1))
                .setBarrier(true).setTableId((short) 0).setKey(key1).setPriority(210).setFlowName(flowIdString1)
                .setHardTimeout(0).setIdleTimeout(idleTimeOut).setId(flowId1)
                .setInstructions(isb1.setInstruction(instruction1).build());

        LOG.info("Flow Builder 1: Instruction " + flowBuilder1.getInstructions());

        String flowIdString2 = "FlowDst";
        FlowId flowId2 = new FlowId(flowIdString2);
        FlowKey key2 = new FlowKey(flowId2);

        FlowBuilder flowBuilder2 = new FlowBuilder().setMatch(matchBuilder2.build()).setId(new FlowId(flowId2))
                .setBarrier(true).setTableId((short) 0).setKey(key2).setPriority(209).setFlowName(flowIdString2)
                .setHardTimeout(0).setIdleTimeout(20).setId(flowId2)
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