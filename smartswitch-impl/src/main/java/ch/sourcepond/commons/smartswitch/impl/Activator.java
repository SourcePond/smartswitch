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

/**
 * The bundle activator which exports the {@link SmartSwitchFactory} service. Because the classloaders from the
 * the consuming bundles are needed, we export a {@link ServiceFactory} instead. This allows to export a customized
 * service for each requesting bundle.
 */
@SuppressWarnings("WeakerAccess")
public class Activator implements BundleActivator, ServiceFactory<SmartSwitchFactory> {

    @Override
    public void start(final BundleContext context) throws Exception {
        context.registerService(SmartSwitchFactory.class, this, null);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        // noop, service unregistration is done automatically
    }

    @Override
    public SmartSwitchFactory getService(final Bundle bundle, final ServiceRegistration<SmartSwitchFactory> registration) {
        return new DefaultSmartSwitchFactory(bundle);
    }

    @Override
    public void ungetService(final Bundle bundle, final ServiceRegistration<SmartSwitchFactory> registration, final SmartSwitchFactory service) {
        // noop
    }
}
