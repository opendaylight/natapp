/*
 * Copyright © 2016 Tata Consultancy Services and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.natapp.impl;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class NatFlow {

	private static final Logger LOG = LoggerFactory.getLogger(NatFlow.class);
	private DataBroker dataBroker;
 	int hardTimeOut=0,priority1=209,priority=210;

	public void setDataBroker(DataBroker dataBroker) {
		this.dataBroker = dataBroker;
	}

	public void createFlow(NodeId nodeId, NodeConnectorId inPort, NodeConnectorId outPort, String srcIP, String dstIP,
			int timeout) {

		String localPort = inPort.getValue();
		if (localPort.contains("LOCAL") || srcIP.contains("null") || dstIP.contains("null") || srcIP.contains("-")) {

			// drop a packet
			MatchBuilder matchBuilder = new MatchBuilder().setInPort(inPort);
			List<Action> actionList = new ArrayList<Action>();

			DropActionBuilder drop = new DropActionBuilder();
			DropAction dropAction = drop.build();

			Action action = new ActionBuilder().setOrder(0)
					.setAction(new DropActionCaseBuilder().setDropAction(dropAction).build()).build();
			actionList.add(action);
			ApplyActionsBuilder aab = new ApplyActionsBuilder().setAction(actionList);

			InstructionsBuilder isb = new InstructionsBuilder();
			List<Instruction> instruction = Lists.newArrayList();
			Instruction applyActionsInstruction = new InstructionBuilder().setOrder(0)
					.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build()).build();
			instruction.add(applyActionsInstruction);

			String flowIdString = "FlowDrop";
			FlowId flowId = new FlowId(flowIdString);
			FlowKey key = new FlowKey(flowId);

			FlowBuilder flowBuilder = new FlowBuilder().setMatch(matchBuilder.build()).setId(new FlowId(flowId))
					.setBarrier(true).setTableId((short) 0).setKey(key).setPriority(125).setFlowName(flowIdString)
					.setHardTimeout(hardTimeOut).setIdleTimeout(timeout).setId(flowId)
					.setInstructions(isb.setInstruction(instruction).build());

			NodeKey nodeKey = new NodeKey(nodeId);

			InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey)
					.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(flowBuilder.getTableId()))
					.child(Flow.class, flowBuilder.getKey()).build();

			WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
			writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build());
		} else {

			// Set new SourceIP and DestinationIP to Global and
			// OutputNodeConnector to port

			MatchBuilder matchBuilder1 = new MatchBuilder().setInPort(inPort);

			MatchBuilder matchBuilder2 = new MatchBuilder().setInPort(outPort);

			List<Action> actionList1 = new ArrayList<Action>();

			Action action1 = new ActionBuilder().setOrder(1)
					.setAction(new OutputActionCaseBuilder().setOutputAction(new OutputActionBuilder()
							.setMaxLength(Integer.valueOf(0xffff)).setOutputNodeConnector(outPort).build()).build())
					.build();

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
							.setMaxLength(Integer.valueOf(0xffff)).setOutputNodeConnector(inPort).build()).build())
					.build();

			Ipv4Builder ipDst = new Ipv4Builder().setIpv4Address(new Ipv4Prefix(dstIP));

			SetNwDstActionBuilder setNwDstActionBuilder = new SetNwDstActionBuilder().setAddress(ipDst.build());

			Action action21 = new ActionBuilder().setOrder(0)
					.setAction(new SetNwDstActionCaseBuilder().setSetNwDstAction(setNwDstActionBuilder.build()).build())
					.build();

			actionList2.add(action21);
			actionList2.add(action2);

			LOG.info("Action List 2: " + actionList2);
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
					.setBarrier(true).setTableId((short) 0).setKey(key1).setPriority(priority2).setFlowName(flowIdString1)
					.setHardTimeout(hardTimeOut).setIdleTimeout(timeout).setId(flowId1)
					.setInstructions(isb1.setInstruction(instruction1).build());

			LOG.info("Flow Builder 1: Instruction " + flowBuilder1.getInstructions());

			String flowIdString2 = "FlowDst";
			FlowId flowId2 = new FlowId(flowIdString2);
			FlowKey key2 = new FlowKey(flowId2);

			FlowBuilder flowBuilder2 = new FlowBuilder().setMatch(matchBuilder2.build()).setId(new FlowId(flowId2))
					.setBarrier(true).setTableId((short) 0).setKey(key2).setPriority(priority1).setFlowName(flowIdString2)
					.setHardTimeout(hardTimeOut).setIdleTimeout(timeout).setId(flowId2)
					.setInstructions(isb2.setInstruction(instruction2).build());

			LOG.info("Flow Builder 2: Instruction " + flowBuilder2.getInstructions());

			NodeKey nodeKey = new NodeKey(nodeId);

			InstanceIdentifier<Flow> flowIID1 = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey)
					.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(flowBuilder1.getTableId()))
					.child(Flow.class, flowBuilder1.getKey()).build();

			InstanceIdentifier<Flow> flowIID2 = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey)
					.augmentation(FlowCapableNode.class).child(Table.class, new TableKey(flowBuilder2.getTableId()))
					.child(Flow.class, flowBuilder2.getKey()).build();

			WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
			writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, flowIID1, flowBuilder1.build());
			writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, flowIID2, flowBuilder2.build());
			writeTransaction.commit();
			LOG.info("Flows created");

		}
	}
}
