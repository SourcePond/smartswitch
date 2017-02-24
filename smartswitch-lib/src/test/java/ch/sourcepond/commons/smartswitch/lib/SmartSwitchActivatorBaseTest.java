package ch.sourcepond.commons.smartswitch.lib;

import org.apache.felix.dm.DependencyManager;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by rolandhauser on 24.02.17.
 */
public class SmartSwitchActivatorBaseTest {

    private static class TestActivator extends SmartSwitchActivatorBase {

        @Override
        public void init(final BundleContext bundleContext, final DependencyManager dependencyManager) throws Exception {

        }
    }

    private final BundleContext context = mock(BundleContext.class);
    private final TestActivator activator = new TestActivator();

    @Before
    public void setup() throws Exception {
        activator.start(context);
    }

    @Test
    public void verifyStartStop() throws Exception {
        assertFalse(activator.executorService.isShutdown());
        activator.stop(context);
        assertTrue(activator.executorService.isShutdown());
    }

    @Test
    public void createSmartSwitchBuilder() {
        assertNotNull(activator.createSmartSwitchBuilder(ExecutorService.class));
    }
}
