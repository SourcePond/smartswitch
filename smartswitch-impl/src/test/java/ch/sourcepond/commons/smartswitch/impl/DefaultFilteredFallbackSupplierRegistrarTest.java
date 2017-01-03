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

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by rolandhauser on 03.01.17.
 */
@SuppressWarnings({"unchecked", "ConstantConditions"})
public class DefaultFilteredFallbackSupplierRegistrarTest {
    private static final String ANY_FILTER = "anyFilter";
    private final BundleContext context = mock(BundleContext.class);
    private final ConfigurationVisitor<ExecutorService> visitor = mock(ConfigurationVisitor.class);
    private final DefaultFilteredFallbackSupplierRegistrar<ExecutorService> registrar = new DefaultFilteredFallbackSupplierRegistrar<>(visitor);

    @Before
    public void setup() {
        when(visitor.getBundleContext()).thenReturn(context);
    }

    @Test
    public void withFilter() {
        assertNotNull(registrar.withFilter(ANY_FILTER));
        verify(visitor).setFilterOrNull(ANY_FILTER);
    }

    @Test(expected = NullPointerException.class)
    public void filterIsNull() {
        registrar.withFilter(null);
    }

    @Test
    public void filterIsInvalid() throws Exception {
        final InvalidSyntaxException expected = new InvalidSyntaxException("", "");
        doThrow(expected).when(context).createFilter(ANY_FILTER);
        try {
            registrar.withFilter(ANY_FILTER);
            fail("Exception expected here");
        } catch (IllegalArgumentException e) {
            assertSame(expected, e.getCause());
        }
    }
}
