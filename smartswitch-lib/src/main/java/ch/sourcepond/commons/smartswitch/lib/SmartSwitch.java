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
package ch.sourcepond.commons.smartswitch.lib;

import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.slf4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
class SmartSwitch<T> implements InvocationHandler, ServiceListener {
    private static final Logger LOG = getLogger(SmartSwitch.class);
    private final Supplier<T> supplier;
    private final Consumer<T> shutdownHookOrNull;
    private final ToDefaultSwitchObserver<T> observerOrNull;
    private final ExecutorService executorService;
    private volatile T defaultService;

    SmartSwitch(final ExecutorService pExecutorService,
                final Supplier<T> pSupplier,
                final Consumer<T> pShutdownHookOrNull,
                final ToDefaultSwitchObserver<T> pObserverOrNull) {
        executorService = pExecutorService;
        supplier = pSupplier;
        shutdownHookOrNull = pShutdownHookOrNull;
        observerOrNull = pObserverOrNull;
    }

    private void informObserver() {
        if (observerOrNull != null) {
            executorService.execute(() -> observerOrNull.defaultInitialized(defaultService));
        }
    }

    @Override
    public void serviceChanged(final ServiceEvent event) {
        if (REGISTERED == event.getType()) {
            shutdownDefaultService();
        }
    }

    private synchronized void shutdownDefaultService() {
        if (defaultService != null) {
            if (shutdownHookOrNull != null) {
                final T toBeShutdown = defaultService;
                executorService.execute(() -> {
                    try {
                        shutdownHookOrNull.accept(toBeShutdown);
                    } catch (final Exception e) {
                        LOG.warn(e.getMessage(), e);
                    }
                });
            }
            defaultService = null;
        }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        T obj = defaultService;

        // Double-check is working properly since Java 5.0
        if (obj == null) { // First check (no locking)
            synchronized (this) {
                obj = defaultService;
                if (obj == null) { // Second check (with locking)
                    defaultService = obj = supplier.get();
                    informObserver();
                }
            }
        }

        return method.invoke(obj, args);
    }
}
