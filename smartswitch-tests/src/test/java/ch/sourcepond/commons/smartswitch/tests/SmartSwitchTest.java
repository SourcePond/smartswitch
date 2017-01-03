package ch.sourcepond.commons.smartswitch.tests;

import ch.sourcepond.commons.smartswitch.api.SmartSwitchFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Created by rolandhauser on 03.01.17.
 */
@RunWith(PaxExam.class)
public class SmartSwitchTest {
    private static final String FILTER = "(testService=true)";

    private ClassLoader loader;
    private ExecutorService osgiService;
    private ExecutorService defaultService;

    private final CountDownLatch latch = new CountDownLatch(1);

    @Inject
    private SmartSwitchFactory smartSwitchFactory;

    @Inject
    private BundleContext context;

    @Configuration
    public Option[] configure() {
        return options(mavenBundle("ch.sourcepond.commons", "smartswitch-api").versionAsInProject(),
                mavenBundle("ch.sourcepond.commons", "smartswitch-impl"),
                mavenBundle("net.bytebuddy", "byte-buddy").versionAsInProject(),
                mavenBundle("net.bytebuddy", "byte-buddy-agent").versionAsInProject(),
                mavenBundle("org.mockito", "mockito-core").versionAsInProject(),
                mavenBundle("org.objenesis", "objenesis").versionAsInProject(),
                frameworkProperty("felix.bootdelegation.implicit").value("false"),
                junitBundles());
    }

    @Before
    public void setup() {
        // --- This is important to make Mockito work with Pax-Exam
        loader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        // ---

        osgiService = mock(ExecutorService.class);
        defaultService = mock(ExecutorService.class);
    }

    @After
    public void tearDown() {
        Thread.currentThread().setContextClassLoader(loader);
    }

    @Test
    public void verifyServiceSwitch() throws Exception {
        ExecutorService service = smartSwitchFactory.whenService(ExecutorService.class).withFilter(
                FILTER).isUnavailableThenUse(
                () -> defaultService).insteadAndObserveAvailability(
                e -> e.shutdown());

        service.isTerminated();
        verify(defaultService).isTerminated();
        reset(defaultService);

        final Hashtable<String, String> props = new Hashtable<>();
        props.put("testService", "true");
        final ServiceRegistration<ExecutorService> registration = context.registerService(ExecutorService.class, osgiService, props);

        Thread.sleep(500);
        service.isTerminated();
        verify(defaultService).shutdown();
        verify(osgiService).isTerminated();

        registration.unregister();

        Thread.sleep(500);
        verify(osgiService, never()).shutdown();

        service.isTerminated();
        verify(defaultService).isTerminated();
    }
}
