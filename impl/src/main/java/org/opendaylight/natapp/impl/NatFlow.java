/*
 * Copyright © 2016 Tata Consultancy Services and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.natapp.impl;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;


import com.google.common.collect.Lists;


public class NatFlow {
	
    private static final Logger LOG = LoggerFactory.getLogger(NatFlow.class);
    private static final int HIGH_PRIORITY = 211, LOW_PRIORITY = 212;
    private static final int HARD_TIMEOUT = 0;
    private DataBroker dataBroker;

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void createFlow(NodeId nodeId, NodeConnectorId inPort, NodeConnectorId outPort, String srcIP, String dstIP,
            int timeout) {
    	
    	String outport = outPort.getValue();
    	String inport = inPort.getValue();
        String localPort = inPort.getValue();
        if (!(localPort.contains("LOCAL") || srcIP.contains("null") || dstIP.contains("-") || srcIP.contains("-"))) {
        	
            EthernetType ethTypeBuilder = new EthernetTypeBuilder().setType(new EtherType(0x0800L)).build();
            EthernetMatch eth = new EthernetMatchBuilder().setEthernetType(ethTypeBuilder).build();
            
            MatchBuilder matchBuilderSrc = new MatchBuilder().setInPort(inPort).setEthernetMatch(eth);

            MatchBuilder matchBuilderDst = new MatchBuilder().setInPort(outPort).setEthernetMatch(eth);

            List<Action> actionListSrc = new ArrayList<Action>();

            Action actionSrcOutput = new ActionBuilder().setOrder(1)
                    .setAction(new OutputActionCaseBuilder().setOutputAction(new OutputActionBuilder()
                            .setMaxLength(Integer.valueOf(0xffff)).setOutputNodeConnector(new Uri(outport)).build()).build())
                    .build();







            Ipv4Builder ipsrc = new Ipv4Builder().setIpv4Address(new Ipv4Prefix(srcIP));

            SetNwSrcActionBuilder setNwsrcActionBuilder = new SetNwSrcActionBuilder().setAddress(ipsrc.build());

            Action actionNwSrc = new ActionBuilder().setOrder(0)
                    .setAction(new SetNwSrcActionCaseBuilder().setSetNwSrcAction(setNwsrcActionBuilder.build()).build())
                    .build();

            actionListSrc.add(actionNwSrc);
            actionListSrc.add(actionSrcOutput);

            LOG.info("Action List Src: " + actionListSrc);

            List<Action> actionListDst = Lists.newArrayList();

            Action actionDstOutput = new ActionBuilder().setOrder(1)
                    .setAction(new OutputActionCaseBuilder().setOutputAction(new OutputActionBuilder()
                            .setMaxLength(Integer.valueOf(0xffff)).setOutputNodeConnector(new Uri(inport)).build()).build())
                    .build();

            Ipv4Builder ipDst = new Ipv4Builder().setIpv4Address(new Ipv4Prefix(dstIP));

            SetNwDstActionBuilder setNwDstActionBuilder = new SetNwDstActionBuilder().setAddress(ipDst.build());

            Action actionnwDst = new ActionBuilder().setOrder(0)
                    .setAction(new SetNwDstActionCaseBuilder().setSetNwDstAction(setNwDstActionBuilder.build()).build())
                    .build();

            actionListDst.add(actionnwDst);
            actionListDst.add(actionDstOutput);

            LOG.info("Action List Dst: " + actionListDst);
            ApplyActionsBuilder aabSrc = new ApplyActionsBuilder().setAction(actionListSrc);
            ApplyActionsBuilder aabDst = new ApplyActionsBuilder().setAction(actionListDst);
            
            InstructionsBuilder isbSrc = new InstructionsBuilder();
            List<Instruction> instructionSrcList = Lists.newArrayList();
            Instruction instructionSrc = new InstructionBuilder().setOrder(0)
                    .setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aabSrc.build()).build()).build();

            InstructionsBuilder isbDst = new InstructionsBuilder();
            List<Instruction> instructionDstList = Lists.newArrayList();
            Instruction instructionDst = new InstructionBuilder().setOrder(1)
                    .setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aabDst.build()).build()).build();

            instructionSrcList.add(instructionSrc);
            instructionDstList.add(instructionDst);

            String flowIdStringSrc = "FlowSrc";
            FlowId flowIdSrc = new FlowId(flowIdStringSrc);
            FlowKey keySrc = new FlowKey(flowIdSrc);

            FlowBuilder flowBuilderSrc = new FlowBuilder().setMatch(matchBuilderSrc.build()).setId(new FlowId(flowIdSrc))
                    .setBarrier(true).setTableId((short) 0).setKey(keySrc).setPriority(HIGH_PRIORITY)
                    .setFlowName(flowIdStringSrc).setHardTimeout(0).setIdleTimeout(timeout).setId(flowIdSrc)
                    .setInstructions(isbSrc.setInstruction(instructionSrcList).build());

            LOG.info("Flow Builder 1: Instruction " + flowBuilderSrc.getInstructions());

            String flowIdStringDst = "FlowDst";
            FlowId flowIdDst = new FlowId(flowIdStringDst);
            FlowKey keyDst = new FlowKey(flowIdDst);

            FlowBuilder flowBuilderDst = new FlowBuilder().setMatch(matchBuilderDst.build()).setId(new FlowId(flowIdDst))
                    .setBarrier(true).setTableId((short) 0).setKey(keyDst).setPriority(LOW_PRIORITY)
                    .setFlowName(flowIdStringDst).setHardTimeout(0).setIdleTimeout(timeout).setId(flowIdDst)
                    .setInstructions(isbDst.setInstruction(instructionDstList).build());

            LOG.info("Flow Builder 2: Instruction " + flowBuilderDst.getInstructions());

            NodeKey nodeKey = new NodeKey(nodeId);

            InstanceIdentifier<Flow> flowSrc = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey)
                    .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(flowBuilderSrc.getTableId()))
                    .child(Flow.class, flowBuilderSrc.getKey()).build();

            InstanceIdentifier<Flow> flowDst = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey)
                    .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(flowBuilderDst.getTableId()))
                    .child(Flow.class, flowBuilderDst.getKey()).build();

            WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
            writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, flowSrc, flowBuilderSrc.build(), true);
            writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, flowDst, flowBuilderDst.build(), true);
            writeTransaction.commit();
            LOG.info("Flows created");

        }
    }
}
