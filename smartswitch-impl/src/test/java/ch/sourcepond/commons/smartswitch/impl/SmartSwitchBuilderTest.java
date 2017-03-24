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

import ch.sourcepond.commons.smartswitch.api.SmartSwitchBuilder;
import ch.sourcepond.commons.smartswitch.api.ToDefaultSwitchObserver;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.wiring.BundleWiring;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.reflect.Proxy.isProxyClass;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 */
public class SmartSwitchBuilderTest {
    private static final String ANY_FILTER = "(someproperty=*)";
    private final ExecutorService executorService = mock(ExecutorService.class);
    private final ToDefaultSwitchObserver<TestService> observer = mock(ToDefaultSwitchObserver.class);
    private final BundleWiring wiring = mock(BundleWiring.class);
    private final Bundle bundle = mock(Bundle.class);
    private final BundleContext context = mock(BundleContext.class);
    private final Consumer<TestService> shutdownHook = mock(Consumer.class);
    private final SmartSwitchFactory factory = mock(SmartSwitchFactory.class);
    private final TestService testService = mock(TestService.class);
    private final Supplier<TestService> supplier = mock(Supplier.class);
    private final SmartSwitch<TestService> smartSwitch = mock(SmartSwitch.class);
    private final SmartSwitchBuilder<TestService> builder = new SmartSwitchBuilderImpl<>(factory, executorService, context, TestService.class);

    @Before
    public void setup() {
        when(context.getBundle()).thenReturn(bundle);
        when(bundle.adapt(BundleWiring.class)).thenReturn(wiring);
        when(wiring.getClassLoader()).thenReturn(getClass().getClassLoader());
        when(factory.create(executorService, supplier, null, observer)).thenReturn(smartSwitch);
        builder.setObserver(observer);
    }

    @Test(expected = NullPointerException.class)
    public void buildSupplierIsNull() {
        builder.build(null);
    }

    @Test(expected = NullPointerException.class)
    public void setNullShutdownHook() {
        builder.setShutdownHook(null);
    }

    @Test
    public void setInvalidFilter() throws Exception {
        final InvalidSyntaxException expected = new InvalidSyntaxException("", ANY_FILTER);
        doThrow(expected).when(context).createFilter(ANY_FILTER);
        try {
            builder.setFilter(ANY_FILTER);
            fail("Exception expected here");
        } catch (final IllegalArgumentException e) {
            assertSame(expected, e.getCause());
        }
    }

    // This case should never happen because the filter has been
    // validated before build can be called.
    @Test
    public void buildWithInvalidFilter() throws Exception {
        final InvalidSyntaxException expected = new InvalidSyntaxException("", ANY_FILTER);
        doThrow(expected).when(context).addServiceListener(same(smartSwitch), anyString());
        try {
            builder.build(supplier);
            fail("Exception expected here");
        } catch (final IllegalStateException e) {
            assertSame(expected, e.getCause());
        }
    }

    private void verifyProxy() {
        final TestService srv = builder.build(supplier);
        assertNotNull(srv);
        assertTrue(isProxyClass(srv.getClass()));
    }

    @Test
    public void buildWithShutdownHook() {
        when(factory.create(executorService, supplier, shutdownHook, observer)).thenReturn(smartSwitch);
        builder.setShutdownHook(shutdownHook);
        verifyProxy();
        verify(factory).create(executorService, supplier, shutdownHook, observer);
    }

    @Test
    public void build() throws Exception {
        builder.setFilter(ANY_FILTER);
        verifyProxy();
        verify(context).addServiceListener(smartSwitch, "(&(objectClass=ch.sourcepond.commons.smartswitch.impl.TestService)(someproperty=*))");
    }

    @Test
    public void buildWithoutFilter() throws Exception {
        verifyProxy();
        verify(context).addServiceListener(smartSwitch, "(objectClass=ch.sourcepond.commons.smartswitch.impl.TestService)");
    }
}
