/*
 * Copyright © 2016 Tata Consultancy Services and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.natapp.impl;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.natapp.rev160125.NatappService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.annotations.VisibleForTesting;

public class NatappProvider implements BindingAwareProvider, AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(NatappProvider.class);
	static DataBroker dataBroker;

	@Override
	public void onSessionInitiated(ProviderContext session) {
		LOG.info("NatappProvider Session Initiated");
		dataBroker = session.getSALService(DataBroker.class);
		NatPacketHandler natPacketHandler = new NatPacketHandler();
		session.addRpcImplementation(NatappService.class, natPacketHandler);
		NatFlowHandler natFlowHandler = new NatFlowHandler(dataBroker);
		natPacketHandler.setdataBroker(dataBroker);
		natPacketHandler.setPacketProcessingService(session.getRpcService(PacketProcessingService.class));
		LOG.info("Registering NotificationListener");
		NotificationService notificationService = session.getSALService(NotificationService.class);
		notificationService.registerNotificationListener(natPacketHandler);
	}

	@Override
	public void close() throws Exception {
		LOG.info("NatappProvider Closed");
	}
	 
	@VisibleForTesting
	   protected DataBroker getDataBroker() {
	       return dataBroker;
	}
}
