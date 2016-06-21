/*
 * Copyright © 2016 Tata Consultancy Services and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.natapp.impl;
import org.mockito.Mock;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NatInventoryUtilityTest {
	
	NodeId ingressNodeId =  new NodeId("openflow:1");
	NodeConnectorId ingressNodeConnectorId =  new NodeConnectorId("openflow:1:1");
	private InstanceIdentifier<NodeConnector> nodeConnectorInstance;
	private NodeConnectorRef nodeConnectorRef;
	private NatInventoryUtility natInventoryUtility;
	
	@Before
	public void initMocks() throws Exception {
		natInventoryUtility =  new NatInventoryUtility();
	    nodeConnectorInstance = InstanceIdentifier.builder(Nodes.class)
			.child(Node.class, new NodeKey(ingressNodeId))
			.child(NodeConnector.class, new NodeConnectorKey(ingressNodeConnectorId)).toInstance();
	    nodeConnectorRef = new NodeConnectorRef(nodeConnectorInstance);
	}
	
	@Test
	public void getNodeIdTest() {
		assertEquals("openflow:1",natInventoryUtility.getNodeId(nodeConnectorRef).getValue());
	}
	
	@Test
	public void getNodeConnectorIdTest() {
		assertEquals("openflow:1:1",natInventoryUtility.getNodeConnectorId(nodeConnectorRef).getValue());
	}
}
