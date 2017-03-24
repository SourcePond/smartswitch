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

import ch.sourcepond.commons.smartswitch.api.SmartSwitchBuilderFactory;
import org.osgi.framework.*;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;

/**
 *
 */
public class Activator implements BundleActivator, ServiceFactory<SmartSwitchBuilderFactory> {
    private final ExecutorService executor;
    private final SmartSwitchFactory smartSwitchFactory;

    // Constructor for OSGi framework
    public Activator() {
        this(newCachedThreadPool(), new SmartSwitchFactory());
    }

    // Constructor for testing
    Activator(final ExecutorService pExecutor, final SmartSwitchFactory pSmartSwitchFactory) {
        executor = pExecutor;
        smartSwitchFactory = pSmartSwitchFactory;
    }

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        bundleContext.registerService(SmartSwitchBuilderFactory.class, this, null);
    }

    @Override
    public void stop(final BundleContext bundleContext) throws Exception {
        executor.shutdown();
    }

    @Override
    public SmartSwitchBuilderFactory getService(final Bundle bundle, final ServiceRegistration<SmartSwitchBuilderFactory> serviceRegistration) {
        return new SmartSwitchBuilderFactoryImpl(bundle, executor, smartSwitchFactory);
    }

    @Override
    public void ungetService(final Bundle bundle, final ServiceRegistration<SmartSwitchBuilderFactory> serviceRegistration, final SmartSwitchBuilderFactory smartSwitchBuilderFactory) {
        // noop
    }
}
