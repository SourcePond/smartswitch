package ch.sourcepond.commons.smartswitch.impl;

import ch.sourcepond.commons.smartswitch.api.SmartSwitchFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.wiring.BundleWiring;

import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by rolandhauser on 23.12.16.
 */
final class ConfigurationVisitor<T> implements SmartSwitchFactory.ProxyFactory<T> {
    private static final String TYPE_FILTER = "(" + Constants.OBJECTCLASS + "=%s)";
    private static final String COMPOUND_FILTER = "(&%s%s)";
    private Bundle clientBundle;
    private Supplier<T> supplier;
    private String filterOrNull;
    private Class<?> serviceInterface;

    public void setClientBundle(final Bundle clientBundle) {
        this.clientBundle = clientBundle;
    }

    public void setSupplier(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public void setFilterOrNull(final String filterOrNull) {
        this.filterOrNull = filterOrNull;
    }

    public void setServiceInterface(final Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public BundleContext getBundleContext() {
        return clientBundle.getBundleContext();
    }

    private T createProxy(final Consumer<T> pHook) {
        final SmartSwitch<T> smartSwitch = new SmartSwitch<T>(supplier, pHook);
        try {
            final String serviceFilter = String.format(TYPE_FILTER, serviceInterface.getName());
            final String filter = filterOrNull == null ? serviceFilter : String.format(COMPOUND_FILTER, serviceFilter, filterOrNull);
            clientBundle.getBundleContext().addServiceListener(smartSwitch, filter);
        } catch (InvalidSyntaxException e) {
            // This should never happen because DefaultFilteredFallbackSupplierRegistrar#withFilter
            // had validated that the filter supplied is valid
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return (T) Proxy.newProxyInstance(clientBundle.adapt(BundleWiring.class).getClassLoader(),
                new Class<?>[]{serviceInterface}, smartSwitch);
    }

    public T instead() {
        return createProxy(h -> {
        });
    }

    public T insteadAndObserveAvailability(final Consumer<T> pConsumer) {
        if (pConsumer == null) {
            throw new NullPointerException("Consumer specified is null!");
        }
        return createProxy(pConsumer);
    }
}
