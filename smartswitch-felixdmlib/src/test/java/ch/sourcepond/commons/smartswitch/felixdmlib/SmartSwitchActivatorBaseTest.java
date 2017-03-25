/*Copyright (C) 2017 Roland Hauser, <sourcepond@gmail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.commons.smartswitch.felixdmlib;

import ch.sourcepond.commons.smartswitch.api.SmartSwitchBuilder;
import ch.sourcepond.commons.smartswitch.api.SmartSwitchBuilderFactory;
import org.apache.felix.dm.DependencyManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.osgi.framework.ServiceEvent.MODIFIED;
import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

/**
 *
 */
public class SmartSwitchActivatorBaseTest {

    private class TestActivator extends SmartSwitchActivatorBase {

        TestActivator() {
            super();
        }

        TestActivator(final long pTimeout) {
            super(pTimeout);
        }

        @Override
        public void init(final BundleContext bundleContext, final DependencyManager dependencyManager) throws Exception {
            initializationVerification.run();
        }
    }

    private final ScheduledExecutorService executor = newScheduledThreadPool(2);
    private final SmartSwitchBuilderFactory builderFactory = mock(SmartSwitchBuilderFactory.class);
    private final SmartSwitchBuilder<ExecutorService> builder = mock(SmartSwitchBuilder.class);
    private final ServiceReference<SmartSwitchBuilderFactory> factoryRef = mock(ServiceReference.class);
    private final Runnable initializationVerification = mock(Runnable.class);
    private final Bundle bundle = mock(Bundle.class);
    private final BundleContext context = mock(BundleContext.class);
    private final TestActivator activator = new TestActivator(1000L);

    @Before
    public void setup() throws Exception {
        when(factoryRef.getBundle()).thenReturn(bundle);
        when(bundle.getBundleContext()).thenReturn(context);
        when(builderFactory.newBuilder(ExecutorService.class)).thenReturn(builder);
    }

    @After
    public void tearDown() {
        executor.shutdown();
    }

    @Test
    public void verifyDefaultConstructor() {
        // Should not cause an exception
        new TestActivator();
    }

    private void verifyStart() throws Exception {
        final InOrder order = inOrder(context, initializationVerification);
        order.verify(context).addServiceListener(activator, "(objectClass=ch.sourcepond.commons.smartswitch.api.SmartSwitchBuilderFactory)");
        order.verify(context).getServiceReference(SmartSwitchBuilderFactory.class);
        order.verify(initializationVerification).run();
        order.verify(context).removeServiceListener(activator);
    }

    private void setupServiceReference() {
        when(context.getServiceReference(SmartSwitchBuilderFactory.class)).thenReturn(factoryRef);
        when(context.getService(factoryRef)).thenReturn(builderFactory);
    }

    @Test
    public void startNoFactoryPresent() throws Exception {
        activator.start(context);
        verifyStart();
        verify(context, never()).getService(any());
        try {
            activator.createSmartSwitchBuilder(ExecutorService.class);
            fail("Exception expected");
        } catch (final NullPointerException e) {
            assertTrue(e.getMessage().startsWith("No service found with interface"));
        }
    }

    @Test
    public void start() throws Exception {
        setupServiceReference();
        activator.start(context);
        verifyStart();
        assertSame(builderFactory, activator.getFactory());
    }

    @Test
    public void ignoreUnhandledEventType() {
        final ServiceEvent event = new ServiceEvent(MODIFIED, factoryRef);
        setupServiceReference();
        activator.serviceChanged(event);
        verifyZeroInteractions(context);
    }

    @Test
    public void startFactoryBecomesAvailable() throws Exception {
        activator.start(context);
        final ServiceEvent event = new ServiceEvent(REGISTERED, factoryRef);
        verifyStart();
        setupServiceReference();
        executor.schedule(() -> activator.serviceChanged(event), 500, MILLISECONDS);
        assertNotNull(activator.createSmartSwitchBuilder(ExecutorService.class));
    }

    @Test
    public void startFactoryBecomesAvailableAndUnavailable() throws Exception {
        activator.start(context);
        final ServiceEvent registeredEvent = new ServiceEvent(REGISTERED, factoryRef);
        verifyStart();
        setupServiceReference();
        executor.schedule(() -> activator.serviceChanged(registeredEvent), 300, MILLISECONDS);
        final ServiceEvent unregisteringEvent = new ServiceEvent(UNREGISTERING, factoryRef);
        executor.schedule(() -> activator.serviceChanged(unregisteringEvent), 300, MILLISECONDS);
        sleep(800);
        try {
            activator.createSmartSwitchBuilder(ExecutorService.class);
            fail("Exception expected");
        } catch (final NullPointerException e) {
            assertTrue(e.getMessage().startsWith("No service found with interface"));
        }
    }
}
