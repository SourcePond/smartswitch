package ch.sourcepond.commons.smartswitch.impl;

import ch.sourcepond.commons.smartswitch.api.SmartSwitchFactory;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by rolandhauser on 03.01.17.
 */
public class ActivatorTest {
    private final BundleContext context = mock(BundleContext.class);
    private final Bundle bundle = mock(Bundle.class);
    private final Activator activator = new Activator();

    @Test
    public void start() throws Exception {
        activator.start(context);
        verify(context).registerService(SmartSwitchFactory.class, activator, null);
    }

    @Test
    public void stop() throws Exception {
        activator.stop(context);
        verifyZeroInteractions(bundle);
    }

    @Test
    public void getService() {
        final SmartSwitchFactory fac1 = activator.getService(bundle, null);
        final SmartSwitchFactory fac2 = activator.getService(bundle, null);
        assertNotNull(fac1);
        assertNotNull(fac2);
        assertNotSame(fac1, fac2);
    }

    @Test
    public void ungetService() {
        activator.ungetService(bundle, null, null);
        verifyZeroInteractions(bundle);
    }
}
