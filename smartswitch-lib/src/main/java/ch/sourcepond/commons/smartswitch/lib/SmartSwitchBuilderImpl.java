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

import java.lang.reflect.Proxy;
import java.util.function.Supplier;

/**
 *
 */
final class SmartSwitchBuilderImpl<T> implements SmartSwitchBuilder<T> {
    static final String SERVICE_ADDED = "serviceAdded";
    static final String SERVICE_REMOVED = "serviceRemoved";
    private final SmartSwitchFactory smartSwitchFactory;
    private final DependencyActivatorBase activator;
    private final Class<T> serviceInterface;
    private volatile String filter;
    private volatile ServiceChangeObserver<T> observerOrNull;
    private volatile ShutdownHook shutdownHookOrNull;

    SmartSwitchBuilderImpl(final SmartSwitchFactory pSmartSwitchFactory, final DependencyActivatorBase pActivator, final Class<T> pServiceInterface) {
        smartSwitchFactory = pSmartSwitchFactory;
        activator = pActivator;
        serviceInterface = pServiceInterface;
    }

    private T createProxy(final Supplier<T> pSupplier, final SmartSwitch<T> pSmartSwitch) {
        return (T) Proxy.newProxyInstance(
                activator.getClass().getClassLoader(),
                new Class<?>[]{serviceInterface},
                pSmartSwitch);
    }

    @Override
    public SmartSwitchBuilder<T> setObserver(final ServiceChangeObserver<T> pObserver) {
        observerOrNull = pObserver;
        return this;
    }

    @Override
    public SmartSwitchBuilder<T> setFilter(final String pFilter) {
        filter = pFilter;
        return this;
    }

    @Override
    public SmartSwitchBuilder<T> setShutdownHook(final ShutdownHook<T> pShutdownHook) {
        if (pShutdownHook == null) {
            throw new NullPointerException("ShutdownHook is null");
        }
        shutdownHookOrNull = pShutdownHook;
        return this;
    }

    @Override
    public ServiceDependency build(final Supplier<T> pSupplier) {
        // The supplier cannot be null otherwise fail here
        if (pSupplier == null) {
            throw new NullPointerException("Supplier is null");
        }
        final ServiceDependency result = activator.createServiceDependency();
        if (filter == null) {
            result.setService(serviceInterface);
        } else {
            result.setService(serviceInterface, filter);
        }
        final SmartSwitch<T> smartSwitch = smartSwitchFactory.create(pSupplier, shutdownHookOrNull, observerOrNull);
        return result.setCallbacks(smartSwitch, SERVICE_ADDED, SERVICE_REMOVED).
                setDefaultImplementation(createProxy(pSupplier, smartSwitch));
    }
}
