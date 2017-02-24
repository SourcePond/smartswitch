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

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;
import static org.osgi.framework.ServiceEvent.REGISTERED;

/**
 *
 */
public class SmartSwitchTest {
    private static final Object[] ARGS = new Object[0];
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ServiceReference<ExecutorService> ref = mock(ServiceReference.class);
    private final ToDefaultSwitchObserver<TestService> observer = mock(ToDefaultSwitchObserver.class);
    private final Supplier<TestService> supplier = mock(Supplier.class);
    private final Consumer<TestService> shutdownHook = mock(Consumer.class);
    private final TestService suppliedService = mock(TestService.class);
    private  SmartSwitch<TestService> smartSwitch = new SmartSwitchFactory(executorService).create(supplier, null, observer);
    private Method method;

    @Before
    public void setup() throws Exception {
        when(supplier.get()).thenReturn(suppliedService);
        method = TestService.class.getMethod("testMethod");
    }

    public void tearDown() {
        executorService.shutdown();
    }

    @Test
    public void callShutdownHookIfAvailable() throws Throwable {
        smartSwitch = new SmartSwitchFactory(executorService).create(supplier, shutdownHook, observer);
        smartSwitch.invoke(null, method, ARGS);
        smartSwitch.serviceChanged(new ServiceEvent(REGISTERED, ref));
        verify(shutdownHook, timeout(1000)).accept(suppliedService);
    }

    @Test
    public void verifyServiceRemovedButInvocationHandlerNeverCalled() {
        // This should not cause an exception to be thrown
        smartSwitch.serviceChanged(new ServiceEvent(REGISTERED, ref));
    }

    @Test
    public void verifyNoExceptionWhenShutdownFails() throws Throwable {
        final RuntimeException e = new RuntimeException();
        doThrow(e).when(shutdownHook).accept(suppliedService);
        smartSwitch = new SmartSwitchFactory(executorService).create(supplier, shutdownHook, observer);
        smartSwitch.invoke(null, method, ARGS);

        // This should not throw an exception
        smartSwitch.serviceChanged(new ServiceEvent(REGISTERED, ref));

        verify(shutdownHook, timeout(1000)).accept(suppliedService);
    }
}
