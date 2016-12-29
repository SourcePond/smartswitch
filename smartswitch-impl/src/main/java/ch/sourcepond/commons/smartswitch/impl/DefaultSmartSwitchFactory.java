package ch.sourcepond.commons.smartswitch.impl;

import ch.sourcepond.commons.smartswitch.api.SmartSwitchFactory;
import org.osgi.framework.Bundle;

/**
 * Created by rolandhauser on 23.12.16.
 */
public class DefaultSmartSwitchFactory implements  SmartSwitchFactory {
    private final Bundle clientBundle;

    public DefaultSmartSwitchFactory(final Bundle pClientBundle) {
        clientBundle = pClientBundle;
    }

    public <T> FilteredFallbackSupplierRegistrar<T> whenService(final Class<T> pInterface) {
        if (pInterface == null) {
            throw new NullPointerException("Interface specified is null!");
        }
        if (!pInterface.isInterface()) {
            throw new IllegalArgumentException(String.format("%s is not an interface!", pInterface.getName()));
        }

        final ConfigurationVisitor<T> visitor = new ConfigurationVisitor<T>();
        visitor.setClientBundle(clientBundle);
        visitor.setServiceInterface(pInterface);
        return new DefaultFilteredFallbackSupplierRegistrar<T>(visitor);
    }
}
