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

import ch.sourcepond.commons.smartswitch.api.SmartSwitchFactory;
import org.osgi.framework.*;
import org.osgi.framework.wiring.BundleWiring;

import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Visitor which is passed around to gather all information necessary for building the SmartSwitch proxy.
 * This class is also the default implementation of the {@link SmartSwitchFactory.ProxyFactory} interface.
 */
class ConfigurationVisitor<T> implements SmartSwitchFactory.ProxyFactory<T> {
    private static final String TYPE_FILTER = "(" + Constants.OBJECTCLASS + "=%s)";
    private static final String COMPOUND_FILTER = "(&%s%s)";
    private final Consumer<T> defaultConsumer;
    private final DefaultInvocationHandlerFactory factory;
    private Bundle clientBundle;
    private Supplier<T> supplier;
    private String filterOrNull;
    private Class<T> serviceInterface;

    ConfigurationVisitor(final DefaultInvocationHandlerFactory pFactory, final Consumer<T> pDefaultConsumer) {
        assert pFactory != null : "pFactory is null";
        assert pDefaultConsumer != null : "pDefaultConsumer is null";
        factory = pFactory;
        defaultConsumer = pDefaultConsumer;
    }

    void setClientBundle(final Bundle clientBundle) {
        this.clientBundle = clientBundle;
    }

    void setSupplier(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    void setFilterOrNull(final String filterOrNull) {
        this.filterOrNull = filterOrNull;
    }

    void setServiceInterface(final Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    BundleContext getBundleContext() {
        return clientBundle.getBundleContext();
    }

    @SuppressWarnings("unchecked")
    private T createProxy(final Consumer<T> pHook) {
        final DefaultInvocationHandler<T> handler = factory.createHandler(supplier, pHook);
        try {
            final BundleContext context = clientBundle.getBundleContext();
            final String serviceFilter = String.format(TYPE_FILTER, serviceInterface.getName());
            final String filter = filterOrNull == null ? serviceFilter : String.format(COMPOUND_FILTER, serviceFilter, filterOrNull);
            context.addServiceListener(handler, filter);

            ServiceReference<T> refOrNull = null;
            if (filterOrNull != null) {
                final Iterator<ServiceReference<T>> refs = context.getServiceReferences(serviceInterface, filter).iterator();
                if (refs.hasNext()) {
                    refOrNull = refs.next();
                }
            }

            handler.initService(refOrNull);
        } catch (final InvalidSyntaxException e) {
            // This should never happen because DefaultFilteredFallbackSupplierRegistrar#withFilter
            // had validated that the filter supplied is valid
            throw new IllegalStateException(e.getMessage(), e);
        }
        return (T) Proxy.newProxyInstance(clientBundle.adapt(BundleWiring.class).getClassLoader(),
                new Class<?>[]{serviceInterface}, handler);
    }

    @Override
    public T instead() {
        return createProxy(defaultConsumer);
    }

    @Override
    public T insteadAndExecuteWhenAvailable(final Consumer<T> pConsumer) {
        if (pConsumer == null) {
            throw new NullPointerException("Consumer specified is null!");
        }
        return createProxy(pConsumer);
    }
}
