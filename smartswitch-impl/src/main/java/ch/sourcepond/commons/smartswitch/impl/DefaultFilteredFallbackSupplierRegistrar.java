package ch.sourcepond.commons.smartswitch.impl;

import ch.sourcepond.commons.smartswitch.api.SmartSwitchFactory;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Created by rolandhauser on 23.12.16.
 */
final class DefaultFilteredFallbackSupplierRegistrar<T> extends DefaultFallbackSupplierRegistrar<T>
        implements SmartSwitchFactory.FilteredFallbackSupplierRegistrar<T> {

    DefaultFilteredFallbackSupplierRegistrar(final ConfigurationVisitor<T> pVisitor) {
        super(pVisitor);
    }

    public SmartSwitchFactory.FallbackSupplierRegistrar withFilter(final String pFilter) {
        if (pFilter == null) {
            throw new NullPointerException("Filter specified is null");
        }
        try {
            visitor.getBundleContext().createFilter(pFilter);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        visitor.setFilterOrNull(pFilter);
        return new DefaultFallbackSupplierRegistrar<T>(visitor);
    }
}
