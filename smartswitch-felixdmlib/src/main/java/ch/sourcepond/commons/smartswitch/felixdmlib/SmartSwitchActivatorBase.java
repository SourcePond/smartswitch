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
package ch.sourcepond.commons.smartswitch.felixdmlib;

import ch.sourcepond.commons.smartswitch.api.SmartSwitchBuilderFactory;
import org.apache.felix.dm.DependencyActivatorBase;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import java.time.Instant;

import static java.lang.String.format;
import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;
import static org.osgi.framework.Constants.OBJECTCLASS;
import static org.osgi.framework.ServiceEvent.*;

/**
 *
 */
public abstract class SmartSwitchActivatorBase extends DependencyActivatorBase implements ServiceListener {
    private static final long DEFAULT_TIMEOUT = 30000L;
    private SmartSwitchBuilderFactory factory;
    private final long timeout;

    public SmartSwitchActivatorBase() {
        this(DEFAULT_TIMEOUT);
    }

    SmartSwitchActivatorBase(final long pTimeout) {
        timeout = pTimeout;
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        context.addServiceListener(this, format("(%s=%s)", OBJECTCLASS, SmartSwitchBuilderFactory.class.getName()));
        final ServiceReference<SmartSwitchBuilderFactory> factoryRef =
                context.getServiceReference(SmartSwitchBuilderFactory.class);
        if (factoryRef != null) {
            factory = context.getService(factoryRef);
        }

        try {
            super.start(context);
        } finally {
            context.removeServiceListener(this);
        }
    }

    synchronized SmartSwitchBuilderFactory getFactory() throws InterruptedException {
        if (factory == null) {
            final Instant end = now().plusMillis(timeout);
            while (factory == null && now().isBefore(end)) {
                wait(timeout);
            }
            requireNonNull(factory, () -> format("No service found with interface %s",
                    SmartSwitchBuilderFactory.class));
        }
        return factory;
    }

    @Override
    public synchronized void serviceChanged(final ServiceEvent serviceEvent) {
        final ServiceReference<?> ref = serviceEvent.getServiceReference();
        switch (serviceEvent.getType()) {
            case UNREGISTERING:
            case MODIFIED_ENDMATCH: {
                factory = null;
                break;
            }
            case REGISTERED: {
                final BundleContext context = ref.getBundle().getBundleContext();
                factory = context.getService((ServiceReference<SmartSwitchBuilderFactory>) ref);
                notifyAll();
            }
        }
    }

    protected <T> ServiceDependencyBuilder<T> createSmartSwitchBuilder(final Class<T> pServiceInterface) throws InterruptedException {
        return new ServiceDependencyBuilder<T>(getFactory().newBuilder(pServiceInterface), this, pServiceInterface);
    }
}
