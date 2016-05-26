package org.opendaylight.natapp.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import org.junit.Test;
import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.StaticNat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.StaticNatBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.staticmap.StaticMappingInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.DynamicNat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.DynamicNatBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.dynamicmap.DynamicMappingInfo;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class NatYangStoreTest {

	@MockitoAnnotations.Mock
	private DataBroker dataBroker;
	@MockitoAnnotations.Mock
	private CheckedFuture checkedFuture;
	@MockitoAnnotations.Mock
	private WriteTransaction writeTransaction;
	private NatYangStore natYangStore;

	@Before
	public void initMocks() throws Exception {
		MockitoAnnotations.initMocks(this);
		natYangStore = new NatYangStore(dataBroker);

		ReadOnlyTransaction readOnlyTransaction = mock(ReadOnlyTransaction.class);
		when(dataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
		when(readOnlyTransaction.read(any(LogicalDatastoreType.class), any(InstanceIdentifier.class)))
				.thenReturn(checkedFuture);
		when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
	}

	@Test
	public void testAddStaticMap() throws ExecutionException, Exception {

		StaticNatBuilder staticNatBuilder = new StaticNatBuilder();
		List<String> globalIPList = new ArrayList<String>();
		globalIPList.add("172.0.0.1/32");
		staticNatBuilder.setGlobalIP(globalIPList);
		Optional<StaticNat> optional = Optional.of(staticNatBuilder.build());
		when(checkedFuture.checkedGet()).thenReturn(optional);
		assertTrue(optional.isPresent());

		doReturn(checkedFuture).when(writeTransaction).submit();
		natYangStore.addStaticMap("10.0.0.1/32");
		verify(writeTransaction, times(1)).merge(any(LogicalDatastoreType.class), any(InstanceIdentifier.class),
				any(StaticMappingInfo.class), anyBoolean());
		verify(writeTransaction, times(1)).commit();
		verify(checkedFuture, times(0)).get();
	}

	@Test
	public void testAddDynamicMap() throws ExecutionException, Exception {
		DynamicNatBuilder dynamicNatBuilder = new DynamicNatBuilder();
		List<String> globalIPList = new ArrayList<String>();
		globalIPList.add("172.0.0.1/32");
		dynamicNatBuilder.setGlobalIP(globalIPList);
		Optional<DynamicNat> optional = Optional.of(dynamicNatBuilder.build());
		when(checkedFuture.checkedGet()).thenReturn(optional);
		assertTrue(optional.isPresent());

		doReturn(checkedFuture).when(writeTransaction).submit();

		natYangStore.addDynamicMap("10.0.0.1/32");
		verify(writeTransaction, times(1)).merge(any(LogicalDatastoreType.class), any(InstanceIdentifier.class),
				any(DynamicMappingInfo.class), anyBoolean());
		verify(writeTransaction, times(1)).commit();
		verify(checkedFuture, times(0)).get();
	}

	@Test
	public void testDeleteDynamicIP() throws Exception {
		natYangStore.deleteDynamicIP("172.0.0.1/32");
		verify(writeTransaction, times(1)).delete(any(LogicalDatastoreType.class), any(InstanceIdentifier.class));
		verify(writeTransaction, times(1)).commit();
	}
}
