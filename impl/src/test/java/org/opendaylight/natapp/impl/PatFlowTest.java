/*
 * Copyright © 2016 Tata Consultancy Services and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.natapp.impl;

import java.util.concurrent.ExecutionException;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import org.junit.Test;
import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class PatFlowTest {
	@MockitoAnnotations.Mock
	private DataBroker dataBroker;
	@MockitoAnnotations.Mock
	private CheckedFuture checkedFuture;
	@MockitoAnnotations.Mock
	private WriteTransaction writeTransaction;
	private PatFlow patFlow;

	@Before
	public void initMocks() throws Exception {
		MockitoAnnotations.initMocks(this);
		patFlow = new PatFlow();
		patFlow.setDataBroker(dataBroker);
		when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
		doReturn(checkedFuture).when(writeTransaction).submit();
	}

	@Test
	public void createFlowTest() {
		NodeId nodeId = new NodeId("openflow:1");
		NodeConnectorId inPort = new NodeConnectorId("openflow:1:2");
		NodeConnectorId outPort = new NodeConnectorId("openflow:1:1");
		String srcIP = "172.0.0.2/32", dstIP = "10.0.0.3/32";
		patFlow.createFlow(nodeId, inPort, outPort, 2000, srcIP, dstIP, 20);
		verify(writeTransaction, times(2)).merge(any(LogicalDatastoreType.class), any(InstanceIdentifier.class),any(DataObject.class), anyBoolean());
		verify(writeTransaction, times(0)).submit();
	}
}
