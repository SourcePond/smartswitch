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
import org.mockito.InOrder;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

/**
 *
 */
public class SmartSwitchTest {
    private static final Object[] ARGS = new Object[0];
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ServiceChangeObserver<TestService> observer = mock(ServiceChangeObserver.class);
    private final Supplier<TestService> supplier = mock(Supplier.class);
    private final ShutdownHook<TestService> shutdownHook = mock(ShutdownHook.class);
    private final TestService suppliedService = mock(TestService.class);
    private final TestService testService1 = mock(TestService.class);
    private final TestService testService2 = mock(TestService.class);
    private final TestService testService3 = mock(TestService.class);
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
        smartSwitch.serviceAdded(testService1);
        verify(shutdownHook, timeout(1000)).shutdown(suppliedService);
    }

    @Test
    public void verifyServiceRemovedButInvocationHandlerNeverCalled() {
        // This should not cause an exception to be thrown
        smartSwitch.serviceRemoved(testService1);
    }

    @Test
    public void verifyNoExceptionWhenShutdownFails() throws Throwable {
        final Exception e = new Exception();
        doThrow(e).when(shutdownHook).shutdown(suppliedService);
        smartSwitch = new SmartSwitchFactory(executorService).create(supplier, shutdownHook, observer);
        smartSwitch.invoke(null, method, ARGS);

        // This should not throw an exception
        smartSwitch.serviceAdded(testService1);

        verify(shutdownHook, timeout(1000)).shutdown(suppliedService);
    }

    @Test
    public void addRemoveService() throws Throwable {
        smartSwitch.invoke(null, method, ARGS);
        smartSwitch.serviceAdded(testService1);
        smartSwitch.invoke(null, method, ARGS);
        smartSwitch.serviceAdded(testService2);
        smartSwitch.invoke(null, method, ARGS);
        smartSwitch.serviceAdded(testService3);
        smartSwitch.invoke(null, method, ARGS);

        smartSwitch.serviceRemoved(testService3);
        smartSwitch.invoke(null, method, ARGS);
        smartSwitch.serviceRemoved(testService2);
        smartSwitch.invoke(null, method, ARGS);
        smartSwitch.serviceRemoved(testService1);
        smartSwitch.invoke(null, method, ARGS);


        final InOrder order = inOrder(suppliedService, testService1, testService2, testService3);
        order.verify(suppliedService).testMethod();
        order.verify(testService1).testMethod();
        order.verify(testService2).testMethod();
        order.verify(testService3).testMethod();
        order.verify(testService2).testMethod();
        order.verify(testService1).testMethod();
        order.verify(suppliedService).testMethod();
    }
}
