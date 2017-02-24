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
package ch.sourcepond.commons.smartswitch.lib;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.ServiceDependency;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 */
public class SmartSwitchBuilderTest {
    private static final String ANY_FILTER = "(someproperty=*)";
    private final ExecutorService executorService = mock(ExecutorService.class);
    private final ToDefaultSwitchObserver<TestService> observer = mock(ToDefaultSwitchObserver.class);
    private final BundleContext context = mock(BundleContext.class);
    private final DependencyActivatorBase activator = mock(DependencyActivatorBase.class);
    private final Consumer<TestService> shutdownHook = mock(Consumer.class);
    private final ServiceDependency dependency = mock(ServiceDependency.class);
    private final SmartSwitchFactory factory = mock(SmartSwitchFactory.class);
    private final TestService testService = mock(TestService.class);
    private final Supplier<TestService> supplier = mock(Supplier.class);
    private final SmartSwitch<TestService> smartSwitch = mock(SmartSwitch.class);
    private final SmartSwitchBuilder<TestService> builder = new SmartSwitchBuilderImpl<>(executorService, factory, activator, TestService.class);

    @Before
    public void setup() {
        when(activator.getBundleContext()).thenReturn(context);
        when(factory.create(supplier, null, observer)).thenReturn(smartSwitch);
        when(activator.createServiceDependency()).thenReturn(dependency);
        when(dependency.setDefaultImplementation(Mockito.argThat(new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(final Object o) {
                return Proxy.isProxyClass(o.getClass());
            }
        }))).thenReturn(dependency);
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
        final InvalidSyntaxException expected = new InvalidSyntaxException("",ANY_FILTER);
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
        final InvalidSyntaxException expected = new InvalidSyntaxException("",ANY_FILTER);
        doThrow(expected).when(context).addServiceListener(same(smartSwitch), anyString());
        try {
            builder.build(supplier);
            fail("Exception expected here");
        } catch (final IllegalStateException e) {
            assertSame(expected, e.getCause());
        }
    }

    @Test
    public void buildWithShutdownHook() {
        when(factory.create(supplier, shutdownHook, observer)).thenReturn(smartSwitch);
        builder.setShutdownHook(shutdownHook);
        assertSame(dependency, builder.build(supplier));
    }

    @Test
    public void build() throws Exception {
        builder.setFilter(ANY_FILTER);
        assertSame(dependency, builder.build(supplier));
        verify(context).addServiceListener(smartSwitch, "(&(objectClass=ch.sourcepond.commons.smartswitch.lib.TestService)(someproperty=*))");
        verify(dependency).setService(TestService.class, ANY_FILTER);
    }

    @Test
    public void buildWithoutFilter() throws Exception {
        assertSame(dependency, builder.build(supplier));
        verify(context).addServiceListener(smartSwitch, "(objectClass=ch.sourcepond.commons.smartswitch.lib.TestService)");
        verify(dependency).setService(TestService.class);
    }
}
