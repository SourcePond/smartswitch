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
import org.osgi.framework.InvalidSyntaxException;

import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.osgi.framework.Constants.OBJECTCLASS;

/**
 *
 */
final class SmartSwitchBuilderImpl<T> implements SmartSwitchBuilder<T> {
    private final SmartSwitchFactory smartSwitchFactory;
    private final DependencyActivatorBase activator;
    private final Class<T> serviceInterface;
    private volatile String filterOrNull;
    private volatile ToDefaultSwitchObserver<T> observerOrNull;
    private volatile Consumer<T> shutdownHookOrNull;

    SmartSwitchBuilderImpl(final SmartSwitchFactory pSmartSwitchFactory, final DependencyActivatorBase pActivator, final Class<T> pServiceInterface) {
        smartSwitchFactory = pSmartSwitchFactory;
        activator = pActivator;
        serviceInterface = pServiceInterface;
    }

    private T createProxy(final SmartSwitch<T> pSmartSwitch) {
        return (T) Proxy.newProxyInstance(
                activator.getClass().getClassLoader(),
                new Class<?>[]{serviceInterface},
                pSmartSwitch);
    }

    @Override
    public SmartSwitchBuilder<T> setObserver(final ToDefaultSwitchObserver<T> pObserver) {
        observerOrNull = pObserver;
        return this;
    }

    @Override
    public SmartSwitchBuilder<T> setFilter(final String pFilter) {
        // Check that the filter specified is valid
        try {
            activator.getBundleContext().createFilter(pFilter);
        } catch (final InvalidSyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        filterOrNull = pFilter;
        return this;
    }

    @Override
    public SmartSwitchBuilder<T> setShutdownHook(final Consumer<T> pShutdownHook) {
        if (pShutdownHook == null) {
            throw new NullPointerException("ShutdownHook is null");
        }
        shutdownHookOrNull = pShutdownHook;
        return this;
    }

    private String createServiceFilter() {
        if (filterOrNull == null) {
            return format("(%s=%s)", OBJECTCLASS, serviceInterface.getName());
        }
        return format("(&(%s=%s)%s)", OBJECTCLASS, serviceInterface.getName(), filterOrNull);
    }

    @Override
    public ServiceDependency build(final Supplier<T> pSupplier) {
        // The supplier cannot be null otherwise fail here
        if (pSupplier == null) {
            throw new NullPointerException("Supplier is null");
        }
        final ServiceDependency result = activator.createServiceDependency();
        result.setDereference(false);
        if (filterOrNull == null) {
            result.setService(serviceInterface);
        } else {
            result.setService(serviceInterface, filterOrNull);
        }
        final SmartSwitch<T> smartSwitch = smartSwitchFactory.create(pSupplier, shutdownHookOrNull, observerOrNull);
        try {
            activator.getBundleContext().addServiceListener(smartSwitch, createServiceFilter());
        } catch (final InvalidSyntaxException e) {
            // This should never happen because it has been validated
            // that the filter specified by the user is valid.
            throw new IllegalStateException(e.getMessage(), e);
        }

        return result.setDefaultImplementation(createProxy(smartSwitch));
    }
}
