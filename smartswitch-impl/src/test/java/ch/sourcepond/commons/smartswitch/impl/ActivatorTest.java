package ch.sourcepond.commons.smartswitch.impl;

import ch.sourcepond.commons.smartswitch.api.SmartSwitchBuilderFactory;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created by rolandhauser on 24.03.17.
 */
public class ActivatorTest {
    private final ExecutorService executor = mock(ExecutorService.class);
    private final SmartSwitchFactory factory = mock(SmartSwitchFactory.class);
    private final BundleContext context = mock(BundleContext.class);
    private final Bundle bundle = mock(Bundle.class);
    private final Activator activator = new Activator(executor, factory);

    @Test
    public void startStop() throws Exception {
        activator.start(context);
        activator.stop(context);
        verify(executor).shutdown();
    }

    @Test
    public void verifyAlwaysNewInstance() {
        final SmartSwitchBuilderFactory fac1 = activator.getService(bundle, null);
        final SmartSwitchBuilderFactory fac2 = activator.getService(bundle, null);
        assertNotNull(fac1);
        assertNotNull(fac2);
        assertNotSame(fac1, fac2);
    }

    @Test
    public void ungetService() {
        activator.ungetService(null, null, null);
        verifyNoMoreInteractions(context);
    }
}
