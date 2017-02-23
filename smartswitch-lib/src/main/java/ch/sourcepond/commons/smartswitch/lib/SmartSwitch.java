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

import org.slf4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
class SmartSwitch<T> implements InvocationHandler {
    private static final Logger LOG = getLogger(SmartSwitch.class);
    private final Deque<T> stack = new ArrayDeque<>();
    private final Supplier<T> supplier;
    private final ShutdownHook<T> shutdownHookOrNull;
    private final ServiceChangeObserver<T> observerOrNull;
    private final ExecutorService executorService;
    private boolean defaultInitialized;
    private volatile T current;

    SmartSwitch(final ExecutorService pExecutorService,
                final Supplier<T> pSupplier,
                final ShutdownHook<T> pShutdownHookOrNull,
                final ServiceChangeObserver<T> pObserverOrNull) {
        executorService = pExecutorService;
        supplier = pSupplier;
        shutdownHookOrNull = pShutdownHookOrNull;
        observerOrNull = pObserverOrNull;
    }

    private void informObserver(final T pPrevious, final T pCurrent) {
        if (observerOrNull != null) {
            executorService.execute(() -> observerOrNull.serviceChanged(pPrevious, pCurrent));
        }
    }

    public synchronized void serviceAdded(final T pService) {
        stack.offer(pService);
        final T previous = current;
        current = pService;

        if (defaultInitialized && shutdownHookOrNull != null) {
            executorService.execute(() -> {
                try {
                    shutdownHookOrNull.shutdown(stack.removeFirst());
                } catch (final Exception e) {
                    LOG.warn(e.getMessage(), e);
                } finally {
                    defaultInitialized = false;
                }
            });
        }

        informObserver(previous, pService);
    }

    public synchronized void serviceRemoved(final T pService) {
        while (stack.removeFirstOccurrence(pService)) ;
        if (stack.isEmpty()) {
            current = null;
        } else {
            current = stack.getLast();
        }

        informObserver(pService, current);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        T obj = current;

        // Double-check is working properly since Java 5.0
        if (obj == null) { // First check (no locking)
            synchronized (this) {
                obj = current;
                if (obj == null) { // Second check (with locking)
                    current = obj = supplier.get();
                    defaultInitialized = true;
                    stack.addFirst(obj);

                    informObserver(null, obj);
                }
            }
        }

        return method.invoke(obj, args);
    }
}
