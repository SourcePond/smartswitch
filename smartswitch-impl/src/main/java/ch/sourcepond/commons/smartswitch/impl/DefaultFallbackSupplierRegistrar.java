package ch.sourcepond.commons.smartswitch.impl;

import ch.sourcepond.commons.smartswitch.api.SmartSwitchFactory;

import java.util.function.Supplier;

/**
 * Created by rolandhauser on 23.12.16.
 */
public class DefaultFallbackSupplierRegistrar<T> implements SmartSwitchFactory.FallbackSupplierRegistrar<T> {
    protected final ConfigurationVisitor<T> visitor;

    public DefaultFallbackSupplierRegistrar(final ConfigurationVisitor<T> pVisitor) {
        assert pVisitor != null : "pVisitor cannot be null";
        visitor = pVisitor;
    }

    public SmartSwitchFactory.ProxyFactory<T> isUnavailableThenUse(final Supplier<T> pSupplier) {
        if (pSupplier == null) {
            throw new NullPointerException("Supplier specified is null");
        }
        visitor.setSupplier(pSupplier);
        return visitor;
    }
}
