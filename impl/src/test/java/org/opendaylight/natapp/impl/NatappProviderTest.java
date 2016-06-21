/*
 * Copyright © 2016 Tata Consultancy Services and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.natapp.impl;

import org.junit.Test;
import org.junit.Before;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class NatappProviderTest {

	@MockitoAnnotations.Mock
	private DataBroker dataBroker;
	private NatappProvider provider;

	@Before
	public void initMocks() throws Exception {
		MockitoAnnotations.initMocks(this);
		provider = new NatappProvider();
	}

	@Test
	public void testOnSessionInitiated() {
		NatPacketHandler natPacketHandler = new NatPacketHandler();
		ProviderContext session = mock(ProviderContext.class);
		NotificationService notificationService = mock(NotificationService.class);

		when(session.getSALService(DataBroker.class)).thenReturn(dataBroker);
		when(session.getSALService(NotificationService.class)).thenReturn(notificationService);
		provider.onSessionInitiated(session);

	}

	@Test
	public void testClose() throws Exception {

		provider.close();
	}
}
