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

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Implementation of the SmartSwitch logic. This class actually switches between a default service and an OSGi service
 * when it becomes available.
 */
class DefaultInvocationHandler<T> implements InvocationHandler, ServiceListener {
    private final AtomicReference<T> reference = new AtomicReference<>();
    private final Supplier<T> supplier;
    private final Consumer<T> serviceAvailableHook;
    private ServiceReference<?> currentServiceReference;

    DefaultInvocationHandler(final Supplier<T> pSupplier, final Consumer<T> pServiceAvailableHook) {
        assert pSupplier != null : "pSupplier cannot be null";
        assert pServiceAvailableHook != null : "pServiceAvailableHook cannot be null";
        supplier = pSupplier;
        serviceAvailableHook = pServiceAvailableHook;
    }

    @SuppressWarnings("unchecked")
    private T getService(final ServiceReference<?> pRef) {
        return (T) pRef.getBundle().getBundleContext().getService(pRef);
    }

    synchronized void initService(final ServiceReference<T> pRefOrNull) {
        if (null != pRefOrNull && null == currentServiceReference) {
            final T serviceOrNull = getService(pRefOrNull);
            if (serviceOrNull != null) {
                currentServiceReference = pRefOrNull;
                reference.set(getService(pRefOrNull));
            }
        }

        if (reference.get() == null) {
            reference.set(supplier.get());
        }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        return method.invoke(reference.get(), args);
    }

    private int getCurrentRanking() {
        return currentServiceReference == null ? Integer.MIN_VALUE : getRanking(currentServiceReference);
    }

    private int getRanking(final ServiceReference<?> pRef) {
        final Object ranking = pRef.getProperty(Constants.SERVICE_RANKING);
        if (null == ranking || !(ranking instanceof Integer)) {
            return 0;
        }
        return (Integer) ranking;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void serviceChanged(final ServiceEvent event) {
        switch (event.getType()) {
            case ServiceEvent.REGISTERED: {
                switchService(event);
                break;
            }
            case ServiceEvent.MODIFIED_ENDMATCH:
            case ServiceEvent.UNREGISTERING: {
                switchToDefault(event);
                break;
            }
            default: {
                // noop
            }
        }
    }

    private void switchToDefault(final ServiceEvent event) {
        if (event.getServiceReference().equals(currentServiceReference)) {
            reference.set(supplier.get());
            currentServiceReference = null;
        }
    }

    private void switchService(final ServiceEvent event) {
        final ServiceReference<?> serviceReference = event.getServiceReference();
        if (getRanking(serviceReference) > getCurrentRanking()) {
            final T previous = reference.getAndSet(getService(serviceReference));
            assert previous != null : "previous cannot be null";

            // If the previously held service was a default service.
            if (currentServiceReference == null) {
                serviceAvailableHook.accept(previous);
            }

            // Remember current reference
            currentServiceReference = serviceReference;
        }
    }
}
