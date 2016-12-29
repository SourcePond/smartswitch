package ch.sourcepond.commons.smartswitch.impl;

import ch.sourcepond.commons.smartswitch.api.SmartSwitchFactory;
import org.osgi.framework.*;

/**
 * Created by rolandhauser on 23.12.16.
 */
public class Activator implements BundleActivator, ServiceFactory<SmartSwitchFactory> {

    public void start(final BundleContext context) throws Exception {
        context.registerService(SmartSwitchFactory.class, this, null);
    }

    public void stop(final BundleContext context) throws Exception {
        // noop, service unregistration is done automatically
    }

    public SmartSwitchFactory getService(final Bundle bundle, final ServiceRegistration<SmartSwitchFactory> registration) {
        return new DefaultSmartSwitchFactory(bundle);
    }

    public void ungetService(final Bundle bundle, final ServiceRegistration<SmartSwitchFactory> registration, final SmartSwitchFactory service) {
        // noop
    }
}
