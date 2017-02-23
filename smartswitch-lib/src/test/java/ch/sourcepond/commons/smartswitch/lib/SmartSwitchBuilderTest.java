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

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static ch.sourcepond.commons.smartswitch.lib.SmartSwitchBuilderImpl.SERVICE_ADDED;
import static ch.sourcepond.commons.smartswitch.lib.SmartSwitchBuilderImpl.SERVICE_REMOVED;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 */
public class SmartSwitchBuilderTest {
    private static final String ANY_FILTER = "anyFilter";
    private final ExecutorService executorService = mock(ExecutorService.class);
    private final ServiceChangeObserver<TestService> observer = mock(ServiceChangeObserver.class);
    private final DependencyActivatorBase activator = mock(DependencyActivatorBase.class);
    private final ShutdownHook<TestService> shutdownHook = mock(ShutdownHook.class);
    private final ServiceDependency dependency = mock(ServiceDependency.class);
    private final SmartSwitchFactory factory = mock(SmartSwitchFactory.class);
    private final TestService testService = mock(TestService.class);
    private final Supplier<TestService> supplier = mock(Supplier.class);
    private final SmartSwitch<TestService> smartSwitch = mock(SmartSwitch.class);
    private final SmartSwitchBuilder<TestService> builder = new SmartSwitchBuilderImpl<>(executorService, factory, activator, TestService.class);

    @Before
    public void setup() {
        when(factory.create(supplier, null, observer)).thenReturn(smartSwitch);
        when(activator.createServiceDependency()).thenReturn(dependency);
        when(dependency.setCallbacks(smartSwitch, SERVICE_ADDED, SERVICE_REMOVED)).thenReturn(dependency);
        when(dependency.setDefaultImplementation(Mockito.argThat(new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(final Object o) {
                return Proxy.isProxyClass(o.getClass());
            }
        }))).thenReturn(dependency);
        builder.setObserver(observer);
    }

    @Test
    public void verifyCallbacks() throws Exception {
        SmartSwitch.class.getDeclaredMethod(SERVICE_ADDED, Object.class).invoke(smartSwitch, testService);
        SmartSwitch.class.getDeclaredMethod(SERVICE_REMOVED, Object.class).invoke(smartSwitch, testService);
        verify(smartSwitch).serviceAdded(testService);
        verify(smartSwitch).serviceRemoved(testService);
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
    public void buildWithShutdownHook() {
        when(factory.create(supplier, shutdownHook, observer)).thenReturn(smartSwitch);
        builder.setShutdownHook(shutdownHook);
        assertSame(dependency, builder.build(supplier));
    }

    @Test
    public void build() {
        builder.setFilter(ANY_FILTER);
        assertSame(dependency, builder.build(supplier));
        verify(dependency).setService(TestService.class, ANY_FILTER);
    }

    @Test
    public void buildWithoutFilter() {
        assertSame(dependency, builder.build(supplier));
        verify(dependency).setService(TestService.class);
    }
}
