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
 * Created by rolandhauser on 23.12.16.
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

    private T getService(final ServiceReference<T> pRef) {
        return pRef.getBundle().getBundleContext().getService(pRef);
    }

    public synchronized void initService(final ServiceReference<T> pRefOrNull) {
        if (null != pRefOrNull && null == currentServiceReference ) {
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

    @SuppressWarnings("unchecked")
    public synchronized void serviceChanged(final ServiceEvent event) {
        switch (event.getType()) {
            case ServiceEvent.REGISTERED: {
                final ServiceReference<T> sref = (ServiceReference<T>)event.getServiceReference();
                if (getRanking(sref) > getCurrentRanking()) {
                    final T previous = reference.getAndSet(getService(sref));
                    assert previous != null : "previous cannot be null";

                    // If the previously held service was a default service.
                    if (currentServiceReference == null) {
                        serviceAvailableHook.accept(previous);
                    }

                    // Remember current reference
                    currentServiceReference = sref;
                }
                break;
            }
            case ServiceEvent.MODIFIED_ENDMATCH:
            case ServiceEvent.UNREGISTERING: {
                if (event.getServiceReference().equals(currentServiceReference)) {
                    reference.set(supplier.get());
                    currentServiceReference = null;
                }
            }
        }
    }
}
