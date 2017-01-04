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
package ch.sourcepond.commons.smartswitch.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleWiring;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test for {@link ConfigurationVisitor}
 */
@SuppressWarnings("unchecked")
public class ConfigurationVisitorTest {
    private static final String ANY_FILTER = "anyFilter";
    private static final String EXPECTED_COMPOUND_FILTER = "(&(objectClass=java.util.concurrent.ExecutorService)anyFilter)";
    private final Bundle clientBundle = mock(Bundle.class);
    private final BundleContext context = mock(BundleContext.class);
    private final BundleWiring wiring = mock(BundleWiring.class);
    private final Supplier<ExecutorService> supplier = mock(Supplier.class);
    private final Consumer<ExecutorService> availabilityHook = mock(Consumer.class);
    private final DefaultInvocationHandlerFactory factory = mock(DefaultInvocationHandlerFactory.class);
    private final DefaultInvocationHandler<ExecutorService> handler = mock(DefaultInvocationHandler.class);
    private final Consumer<ExecutorService> defaultConsumer = mock(Consumer.class);
    private final ConfigurationVisitor<ExecutorService> visitor = new ConfigurationVisitor<>(factory, defaultConsumer);

    @Before
    public void setup() {
        when(clientBundle.getBundleContext()).thenReturn(context);
        when(clientBundle.adapt(BundleWiring.class)).thenReturn(wiring);
        when(wiring.getClassLoader()).thenReturn(getClass().getClassLoader());
        visitor.setFilterOrNull(ANY_FILTER);
        visitor.setSupplier(supplier);
        visitor.setClientBundle(clientBundle);
        visitor.setServiceInterface(ExecutorService.class);
    }

    private void verifyInit(final String pFilter) throws Exception {
        final InOrder order = inOrder(context, handler);
        order.verify(context).addServiceListener(handler, pFilter);
        order.verify(handler).initService(null);
    }

    private void verifyInit() throws Exception {
        verifyInit(EXPECTED_COMPOUND_FILTER);
    }

    @Test
    public void createProxy() throws Exception {
        when(factory.createHandler(supplier, defaultConsumer)).thenReturn(handler);
        final ExecutorService proxy = visitor.instead();
        assertNotNull(proxy);
        verifyInit();
    }

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    @Test
    public void createProxyServiceAlreadyAvailable() throws Exception {
        when(factory.createHandler(supplier, defaultConsumer)).thenReturn(handler);
        final ServiceReference<ExecutorService> ref = mock(ServiceReference.class);
        when(context.getServiceReferences(ExecutorService.class, EXPECTED_COMPOUND_FILTER)).thenReturn(asList(ref));
        final ExecutorService proxy = visitor.instead();
        assertNotNull(proxy);
        final InOrder order = inOrder(context, handler);
        order.verify(context).addServiceListener(handler, EXPECTED_COMPOUND_FILTER);
        order.verify(handler).initService(ref);
    }

    @Test
    public void createProxyNoCustomFilterSet() throws Exception {
        visitor.setFilterOrNull(null);
        when(factory.createHandler(supplier, defaultConsumer)).thenReturn(handler);
        final ExecutorService proxy = visitor.instead();
        assertNotNull(proxy);
        verifyInit("(objectClass=java.util.concurrent.ExecutorService)");
    }

    @Test
    public void createProxyWithCustomAvailabilityHook() throws Exception {
        when(factory.createHandler(supplier, availabilityHook)).thenReturn(handler);
        final ExecutorService proxy = visitor.insteadAndObserveAvailability(availabilityHook);
        assertNotNull(proxy);
        verifyInit();
    }

    @Test(expected = NullPointerException.class)
    public void hookIsNull() {
        visitor.insteadAndObserveAvailability(null);
    }

    @Test
    public void filterSuppliedIsNotValid() throws Exception {
        when(factory.createHandler(supplier, defaultConsumer)).thenReturn(handler);
        final InvalidSyntaxException expected = new InvalidSyntaxException("", "");
        doThrow(expected).when(context).addServiceListener(same(handler), anyString());
        try {
            visitor.instead();
            fail("Exception expected here");
        } catch (final IllegalStateException e) {
            assertSame(expected, e.getCause());
        }
    }

    @Test
    public void getBundleContext() {
        assertSame(context, visitor.getBundleContext());
    }
}
