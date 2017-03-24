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
import ch.sourcepond.commons.smartswitch.api.ToDefaultSwitchObserver;
import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.ServiceDependency;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class ServiceDependencyBuilderTest {
    private static final String ANY_FILTER = "anyFilter";
    private final SmartSwitchBuilder<ExecutorService> smartSwitchBuilder = mock(SmartSwitchBuilder.class);
    private final DependencyActivatorBase activator = mock(DependencyActivatorBase.class);
    private final ServiceDependency dependency = mock(ServiceDependency.class);
    private final Consumer<ExecutorService> shutdownHook = mock(Consumer.class);
    private final ToDefaultSwitchObserver<ExecutorService> observer = mock(ToDefaultSwitchObserver.class);
    private final ServiceDependencyBuilder<ExecutorService> builder = new ServiceDependencyBuilder<>(smartSwitchBuilder, activator, ExecutorService.class);
    private final Supplier<ExecutorService> supplier = mock(Supplier.class);
    private final ExecutorService executor = mock(ExecutorService.class);

    @Test
    public void setObserver() {
        assertSame(builder, builder.setObserver(observer));
        verify(smartSwitchBuilder).setObserver(observer);
    }

    @Test
    public void setFilter() {
        assertSame(builder, builder.setFilter(ANY_FILTER));
        verify(smartSwitchBuilder).setFilter(ANY_FILTER);
    }

    @Test
    public void setShutdownHook() {
        assertSame(builder, builder.setShutdownHook(shutdownHook));
        verify(smartSwitchBuilder).setShutdownHook(shutdownHook);
    }

    @Test
    public void build() {
        when(smartSwitchBuilder.build(supplier)).thenReturn(executor);
        when(activator.createServiceDependency()).thenReturn(dependency);
        when(dependency.setDefaultImplementation(executor)).thenReturn(dependency);
        assertSame(dependency, builder.build(supplier));
        verify(dependency).setService(ExecutorService.class);
        verify(dependency).setDereference(false);
    }
}
