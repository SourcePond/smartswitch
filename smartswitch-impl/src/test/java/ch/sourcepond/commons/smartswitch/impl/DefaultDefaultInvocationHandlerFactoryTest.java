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
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

import java.util.Collections;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

/**
 * Created by rolandhauser on 03.01.17.
 */
@SuppressWarnings("ConstantConditions")
public class DefaultDefaultInvocationHandlerFactoryTest {
    private final BundleWiring wiring = mock(BundleWiring.class);
    private final BundleContext context = mock(BundleContext.class);
    private final Bundle clientBundle = mock(Bundle.class);
    private final ExecutorService defaultService = mock(ExecutorService.class);
    private final DefaultSmartSwitchFactory factory = new DefaultSmartSwitchFactory(clientBundle);

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws Exception {
        when(clientBundle.adapt(BundleWiring.class)).thenReturn(wiring);
        when(wiring.getClassLoader()).thenReturn(getClass().getClassLoader());
        when(clientBundle.getBundleContext()).thenReturn(context);
        when(context.getServiceReferences((Class)Mockito.any(), Mockito.anyString())).thenReturn(Collections.emptyList());
    }

    @Test(expected = NullPointerException.class)
    public void suppliedInterfaceIsNull() {
        factory.whenService(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void suppliedClassIsNotAnInterface() {
        factory.whenService(Object.class);
    }

    @Test
    public void verifyVisitor() {
        final ExecutorService interceptor = factory.whenService(
                ExecutorService.class).isUnavailableThenUse(() -> defaultService).instead();
        interceptor.isTerminated();
        verify(defaultService).isTerminated();
    }
}
