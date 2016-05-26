/*
 * Copyright © 2016 Tata Consultancy Services and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.natapp.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.StaticNat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.DynamicNat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.StaticMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.DynamicMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.staticmap.StaticMappingInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.staticmap.StaticMappingInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.dynamicmap.DynamicMappingInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.dynamicmap.DynamicMappingInfoBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NatYangStore {
	private static final Logger LOG = LoggerFactory.getLogger(NatYangStore.class);
	private DataBroker dataBroker;
	private Map<String, String> staticGlobalIpMap = new HashMap<String, String>();
	private Map<String, String> dynamicGlobalIpMap = new HashMap<String, String>();

	public NatYangStore(DataBroker dataBroker) {
		this.dataBroker = dataBroker;
	}
	public void setDataBroker(DataBroker dataBroker) {
		this.dataBroker = dataBroker;
	}

	private <T extends DataObject> T getDataObject(final InstanceIdentifier<T> instanceIdentifier) {
		ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();

		Optional<T> optionalData = null;
		try {
			CheckedFuture<Optional<T>, ReadFailedException> readFuture = readOnlyTransaction
					.read(LogicalDatastoreType.CONFIGURATION, instanceIdentifier);

			optionalData = readFuture.checkedGet();
			if (optionalData.isPresent()) {
				return optionalData.get();
			}
		} catch (Exception e) {
			LOG.info("Exception while executing getDataObject: " + e.getMessage());
		}
		return null;
	}

	public String addStaticMap(String localIP) {

		InstanceIdentifier<StaticNat> staticNatInstance = InstanceIdentifier.create(StaticNat.class);
		String staticGlobalIP =null;
		StaticNat staticNat = getDataObject(staticNatInstance);
		List<String> globalIPList = staticNat.getGlobalIP();
		LOG.info("Static GlobalIP list {} Map {}", globalIPList, staticGlobalIpMap);
		for (String globalIP : globalIPList) {
			if (!staticGlobalIpMap.containsKey(globalIP)) {
				if (!staticGlobalIpMap.containsValue(localIP)) {
					staticGlobalIpMap.put(globalIP,localIP);
					LOG.info("Map entry added {}", staticGlobalIpMap);
					staticGlobalIP = globalIP;
				} else {
					LOG.info("Local Ip {} is used in map {}", localIP,staticGlobalIpMap);
				}

			} else {
				LOG.info("Global IP already used in map {}",staticGlobalIpMap.get(localIP));
				staticGlobalIP = staticGlobalIpMap.get(localIP);
			}
		}
		
		StaticMappingInfo staticMappingInfoBuilder = new StaticMappingInfoBuilder().setGlobalIP(staticGlobalIP)
				.setLocalIP(localIP).build();
		InstanceIdentifier staticMapInstance = InstanceIdentifier.create(StaticMap.class).child(StaticMappingInfo.class,
				staticMappingInfoBuilder.getKey());
		WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
		writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, staticMapInstance, staticMappingInfoBuilder, true);
		writeTransaction.commit();
		LOG.info("Static mapping info added into yang data store");
		return staticGlobalIP;
	}

	public String addDynamicMap(String localIP) {

		InstanceIdentifier<DynamicNat> dynamicNatInstance = InstanceIdentifier.create(DynamicNat.class);
		String dynamicGlobalIP = null;
		DynamicNat dynamicNat = getDataObject(dynamicNatInstance);
		List<String> globalIPList = dynamicNat.getGlobalIP();
		LOG.info("Dynamic GlobalIP list {} Map {}", globalIPList, dynamicGlobalIpMap);
		for (String globalIP : globalIPList) {
			if (!dynamicGlobalIpMap.containsKey(globalIP)) {
				if (!dynamicGlobalIpMap.containsValue(localIP)) {
					dynamicGlobalIpMap.put(globalIP,localIP);
					LOG.info("Map entry added {}", dynamicGlobalIpMap);
					dynamicGlobalIP = globalIP;
				} else {
					LOG.info("Local Ip {} is used in map {}", localIP,dynamicGlobalIpMap);
				}

			} else {
				LOG.info("Global IP already used in map {}",dynamicGlobalIpMap.get(localIP));
				dynamicGlobalIP = dynamicGlobalIpMap.get(localIP);
			}
		}
		DynamicMappingInfo dynamicMappingInfoBuilder = new DynamicMappingInfoBuilder().setGlobalIP(dynamicGlobalIP)
				.setLocalIP(localIP).build();
		InstanceIdentifier dynamicMapInstance = InstanceIdentifier.create(DynamicMap.class)
				.child(DynamicMappingInfo.class, dynamicMappingInfoBuilder.getKey());
		WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

		writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, dynamicMapInstance, dynamicMappingInfoBuilder, true);
		writeTransaction.commit();
		LOG.info("Dynamic mapping info added into yang data store");
		return dynamicGlobalIP;
	}	

	public void deleteDynamicIP(String globalIP) {

		DynamicMappingInfo dynamicMappingInfoBuilder = new DynamicMappingInfoBuilder().setGlobalIP(globalIP).build();
		InstanceIdentifier dynamicMapInstance = InstanceIdentifier.create(DynamicMap.class)
				.child(DynamicMappingInfo.class, dynamicMappingInfoBuilder.getKey());

		dynamicGlobalIpMap.remove(globalIP);
		WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
		writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, dynamicMapInstance);
		writeTransaction.commit();
		LOG.info("Dynamic mapping info deleted into yang data store");
	}
}
