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
package ch.sourcepond.commons.smartswitch.testing;

import ch.sourcepond.commons.smartswitch.api.SmartSwitchFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.Statement;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test for {@link SmartSwitchRule}
 */
public class SmartSwitchRuleTest {
    private static final String ANY_FILTER = "anyFilter";
    private final ExecutorService defaultService = mock(ExecutorService.class);
    private final Supplier<ExecutorService> supplier = mock(Supplier.class);
    private final Consumer<ExecutorService> availabilityHook = mock(Consumer.class);
    private final Statement statement = mock(Statement.class);
    private final SmartSwitchRule rule = new SmartSwitchRule();
    private final SmartSwitchFactory factory = rule.getTestFactory();

    @Before
    public void setup() {
        when(supplier.get()).thenReturn(defaultService);
    }

    @Test(expected = AssertionError.class)
    public void noServiceRegistered() {
        factory.whenService(ExecutorService.class).isUnavailableThenUse(() -> defaultService);
    }

    @Test(expected = AssertionError.class)
    public void noServiceRegisteredWithFilter() {
        factory.whenService(ExecutorService.class).
                withFilter(ANY_FILTER).
                isUnavailableThenUse(() -> defaultService);
    }

    @Test
    public void whenService() {
        assertNotNull(rule.useOsgiService(ExecutorService.class));

        // Should not cause an exception
        assertNotNull(factory.whenService(ExecutorService.class).isUnavailableThenUse(() -> defaultService));
    }


    @Test
    public void whenServiceWithFilter() {
        assertNotNull(rule.useOsgiService(ExecutorService.class, ANY_FILTER));

        // Should not cause an exception
        assertNotNull(factory.whenService(ExecutorService.class).withFilter(ANY_FILTER).isUnavailableThenUse(() -> defaultService));
    }

    @Test
    public void getSupplier() {
        rule.useOsgiService(ExecutorService.class);
        factory.whenService(ExecutorService.class).isUnavailableThenUse(supplier);
        assertSame(supplier, rule.getSupplier(ExecutorService.class));
    }

    @Test
    public void getSupplierWithFilter() {
        rule.useOsgiService(ExecutorService.class, ANY_FILTER);
        factory.whenService(ExecutorService.class).withFilter(ANY_FILTER).isUnavailableThenUse(supplier);
        assertSame(supplier, rule.getSupplier(ExecutorService.class, ANY_FILTER));
    }

    @Test
    public void getAvailabilityHook() {
        rule.useOsgiService(ExecutorService.class);
        factory.whenService(ExecutorService.class).isUnavailableThenUse(supplier).insteadAndExecuteWhenAvailable(availabilityHook);
        assertSame(availabilityHook, rule.getAvailabilityHook(ExecutorService.class));
    }

    @Test
    public void getAvailabilityHookWithFilter() {
        rule.useOsgiService(ExecutorService.class, ANY_FILTER);
        factory.whenService(ExecutorService.class).withFilter(ANY_FILTER).isUnavailableThenUse(supplier).insteadAndExecuteWhenAvailable(availabilityHook);
        assertSame(availabilityHook, rule.getAvailabilityHook(ExecutorService.class, ANY_FILTER));
    }

    @Test
    public void verifyExecuteAvailabilityHooks() throws Throwable {
        rule.useDefaultService(ExecutorService.class, ANY_FILTER);
        factory.whenService(ExecutorService.class).withFilter(ANY_FILTER).isUnavailableThenUse(supplier).insteadAndExecuteWhenAvailable(availabilityHook);
        rule.apply(statement, null).evaluate();
        verify(statement).evaluate();
        verify(availabilityHook).accept(defaultService);
    }
}
