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

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by rolandhauser on 03.01.17.
 */
@SuppressWarnings("unchecked")
public class DefaultFallbackSupplierRegistrarTest {
    private final ConfigurationVisitor<ExecutorService> visitor = mock(ConfigurationVisitor.class);
    private final Supplier<ExecutorService> supplier = mock(Supplier.class);
    private final DefaultFallbackSupplierRegistrar<ExecutorService> registrar = new DefaultFallbackSupplierRegistrar<>(visitor);

    @Test(expected = NullPointerException.class)
    public void supplierIsNull() {
        registrar.isUnavailableThenUse(null);
    }

    @Test
    public void isUnavailableThenUse() {
        assertNotNull(registrar.isUnavailableThenUse(supplier));
        verify(visitor).setSupplier(supplier);
    }
}
