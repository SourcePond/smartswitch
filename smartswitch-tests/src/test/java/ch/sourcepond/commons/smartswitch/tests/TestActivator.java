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
package ch.sourcepond.commons.smartswitch.tests;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.apache.felix.dm.ServiceDependency;
import org.osgi.framework.BundleContext;

import java.util.concurrent.ExecutorService;

import static ch.sourcepond.commons.smartswitch.lib.SmartSwitchBuilder.create;
import static java.lang.Thread.currentThread;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class TestActivator extends DependencyActivatorBase {
    private static final Object MUTEX = new Object();
    static volatile ExecutorService smartSwitchService;
    static volatile ExecutorService defaultService;
    private static ExecutorService smartSwitchedService;
    private volatile ServiceDependency delegateDependency;

    @Override
    public void init(final BundleContext bundleContext, final DependencyManager dependencyManager) throws Exception {
        // This is necessary to make Mockito work
        currentThread().setContextClassLoader(getClass().getClassLoader());
        defaultService = mock(ExecutorService.class);

        dependencyManager.add(
                createComponent().add(
                        create(this, ExecutorService.class).
                                setObserver((p, c) -> setSmartSwitchedService(c)).
                                setFilter("(testexecutor=*)").
                                setShutdownHook(e -> e.shutdown()).
                                build(() -> defaultService)
                ));
    }

    public static void setSmartSwitchedService(final ExecutorService pService) {
        synchronized (MUTEX) {
            smartSwitchedService = pService;
            MUTEX.notifyAll();
        }
    }

    public static ExecutorService getSmartSwitchedService(int pTimeout) throws InterruptedException {
        synchronized (MUTEX) {
            long timeout = System.currentTimeMillis() + pTimeout;
            while (timeout > System.currentTimeMillis()) {
                MUTEX.wait(1000);
            }
            return smartSwitchedService;
        }
    }
}
