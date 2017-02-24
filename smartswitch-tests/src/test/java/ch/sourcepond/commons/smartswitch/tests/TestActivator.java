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

import ch.sourcepond.commons.smartswitch.lib.SmartSwitchActivatorBase;
import ch.sourcepond.commons.smartswitch.lib.ToDefaultSwitchObserver;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import java.util.concurrent.ExecutorService;

import static java.lang.Thread.currentThread;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class TestActivator extends SmartSwitchActivatorBase {
    private static final Object MUTEX = new Object();
    static final TestComponent COMPONENT = new TestComponent();
    static volatile ExecutorService defaultService;
    static volatile ToDefaultSwitchObserver<ExecutorService> observer;

    @Override
    public void init(final BundleContext bundleContext, final DependencyManager dependencyManager) throws Exception {
        // Necessary for Mockito
        currentThread().setContextClassLoader(getClass().getClassLoader());
        defaultService = mock(ExecutorService.class);
        observer = mock(ToDefaultSwitchObserver.class);

        dependencyManager.add(
                createComponent().
                        setImplementation(COMPONENT).add(
                        createSmartSwitchBuilder(ExecutorService.class).
                                setObserver(observer).
                                setFilter("(testexecutor=*)").
                                setShutdownHook(e -> e.shutdown()).
                                build(() -> defaultService)
                ));
    }
}
