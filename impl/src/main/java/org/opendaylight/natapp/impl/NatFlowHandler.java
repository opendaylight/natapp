/*
 * Copyright © 2016 Tata Consultancy Services and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.natapp.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Optional;

public class NatFlowHandler implements DataChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(NatFlowHandler.class);
    private DataBroker dataBroker;

    public NatFlowHandler(DataBroker broker) {
        this.dataBroker = broker;
        // flow
        InstanceIdentifier<Flow> flowPath = InstanceIdentifier.builder(Nodes.class).child(Node.class)
              .augmentation(FlowCapableNode.class).child(Table.class).child(Flow.class).build();

        dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, flowPath, this, DataChangeScope.BASE);
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        if (change == null) {
            LOG.info("FlowHandler ===>>> onDataChanged: change is null");
            return;
        }
        handleDeletedFlow(change);
    }

    public static NodeBuilder createNodeBuilder(String nodeId) {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeId));
        builder.setKey(new NodeKey(builder.getId()));
        return builder;
    }

    @SuppressWarnings("deprecation")
    private void handleDeletedFlow(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {

        NatYangStore natYangStore = new NatYangStore(dataBroker);
        Set<InstanceIdentifier<?>> removedData = change.getRemovedPaths();

        Iterator<InstanceIdentifier<?>> it = removedData.iterator();
        if (it.hasNext()) {
            while (it.hasNext()) {
                LOG.info("Set Iterator" + it.next().getPath());
                String globalSrcIP;
                String nodeId = "openflow:1";
                NodeBuilder nodeBuilder = createNodeBuilder(nodeId);
                String flowIdString = "FlowSrc";
                FlowId flowId = new FlowId(flowIdString);
                FlowKey key = new FlowKey(flowId);
                FlowBuilder flowBuilder = new FlowBuilder().setId(new FlowId(flowId)).setTableId((short) 0).setKey(key)
                        .setFlowName(flowIdString);
                InstanceIdentifier<Flow> flowPath = InstanceIdentifier.builder(Nodes.class)
                        .child(Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class)
                        .child(Table.class, new TableKey(flowBuilder.getTableId()))
                        .child(Flow.class, flowBuilder.getKey()).build();
                ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
                Flow flow = null;
                try {
                    Optional<Flow> dataObjectOptional = null;
                    dataObjectOptional = readOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, flowPath).get();
                    flow = (Flow) dataObjectOptional.get();
                    List<Instruction> srcInstructionList = flow.getInstructions().getInstruction();
                    for (int i = 0; i < srcInstructionList.size(); i++) {
                        List<Action> actionList = new ArrayList<>();
                        List<Action> existingActions;
                        for (Instruction in : srcInstructionList) {
                            if (in.getInstruction() instanceof ApplyActionsCase) {
                                existingActions = (((ApplyActionsCase) in.getInstruction()).getApplyActions()
                                         .getAction());
                                actionList.addAll(existingActions);
                                for (Action action : actionList) {
                                    if (action.getAction() instanceof SetNwSrcActionCase) {
                                        SetNwSrcActionCase setNwSrcAction = (SetNwSrcActionCase) action.getAction();
                                        Address address = setNwSrcAction.getSetNwSrcAction().getAddress();
                                        if (address instanceof Ipv4) {
                                            Ipv4 ipv4 = (Ipv4) address;
                                            Ipv4Prefix ipv4Prefix = ipv4.getIpv4Address();
                                            LOG.info("Deleted GlobalIP" + ipv4Prefix.getValue());
                                            globalSrcIP = ipv4Prefix.getValue();
                                            int index = globalSrcIP.indexOf('/');
                                            if (index > 0) {
                                                globalSrcIP = globalSrcIP.substring(0, index);
                                            }
                                            LOG.info("valid globalSrcIP "+globalSrcIP);
                                            natYangStore.deleteDynamicIP(globalSrcIP);
                                            return;
                                        }
                                    } else {
                                        LOG.info("Invalid");
                                    }
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    LOG.info("Failed to read nodes");
                    readOnlyTransaction.close();
                    throw new RuntimeException("Failed to read nodes from Operation data store.", e);
                } catch (ExecutionException e) {
                    LOG.info("Failed to read nodes");
                    readOnlyTransaction.close();
                    throw new RuntimeException("Failed to read nodes from Operation data store.", e);
                }
            }
        }
    }
}
