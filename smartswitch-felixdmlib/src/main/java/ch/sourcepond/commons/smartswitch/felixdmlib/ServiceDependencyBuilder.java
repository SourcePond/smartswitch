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

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 */
public class ServiceDependencyBuilder<T> {
    private final SmartSwitchBuilder<T> builder;
    private final DependencyActivatorBase activator;
    private final Class<T> serviceInterface;

    ServiceDependencyBuilder(final SmartSwitchBuilder<T> pBuilder,
                             final DependencyActivatorBase pActivator,
                             final Class<T> pServiceInterface) {
        builder = pBuilder;
        activator = pActivator;
        serviceInterface = pServiceInterface;
    }

    public ServiceDependencyBuilder<T> setObserver(final ToDefaultSwitchObserver<T> pObserver) {
        builder.setObserver(pObserver);
        return this;
    }

    public ServiceDependencyBuilder<T> setFilter(final String pFilter) {
        builder.setFilter(pFilter);
        return this;
    }

    public ServiceDependencyBuilder<T> setShutdownHook(final Consumer<T> pShutdownHook) {
        builder.setShutdownHook(pShutdownHook);
        return this;
    }

    public ServiceDependency build(final Supplier<T> pSupplier) {
        final ServiceDependency result = activator.createServiceDependency();
        result.setService(serviceInterface);
        result.setDereference(false);
        return result.setDefaultImplementation(builder.build(pSupplier));
    }
}
