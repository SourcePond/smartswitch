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
package ch.sourcepond.commons.smartswitch.tests;

import ch.sourcepond.testing.BundleContextClassLoaderRule;
import ch.sourcepond.testing.OptionsHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;

import static ch.sourcepond.commons.smartswitch.tests.TestActivator.COMPONENT;
import static ch.sourcepond.commons.smartswitch.tests.TestActivator.defaultService;
import static ch.sourcepond.commons.smartswitch.tests.TestActivator.observer;
import static ch.sourcepond.testing.OptionsHelper.mockitoBundles;
import static ch.sourcepond.testing.OptionsHelper.tinyBundles;
import static java.lang.Thread.sleep;
import static org.mockito.Mockito.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;

/**
 * Integration test for SmartSwitch bundle.
 */
@RunWith(PaxExam.class)
public class SmartSwitchTest {
    private static final String TEST_BUNDLE_KEY = "testBundle";

    @Rule
    public final BundleContextClassLoaderRule rule = new BundleContextClassLoaderRule(this);

    private final ExecutorService realExecutor1 = mock(ExecutorService.class);
    private final ExecutorService realExecutor2 = mock(ExecutorService.class);
    private final ExecutorService realExecutor3 = mock(ExecutorService.class);
    private final Runnable runnable = mock(Runnable.class);

    @SuppressWarnings("CanBeFinal")
    @Inject
    private BundleContext context;

    private Integer ranking = 0;

    @ProbeBuilder
    public TestProbeBuilder probeBuilder(final TestProbeBuilder pDefaultProbeBuilder) {
        return new TestProbeBuilderAdapter(pDefaultProbeBuilder);
    }

    @Configuration
    public Option[] configure() throws IOException {
        return new Option[]{composite(OptionsHelper.karafContainer(features(maven()
                        .groupId("ch.sourcepond.commons")
                        .artifactId("smartswitch-feature")
                        .classifier("features")
                        .type("xml")
                        .versionAsInProject(), "smartswitch-feature"
                )),
                mockitoBundles(),
                tinyBundles(),
                mavenBundle().groupId("ch.sourcepond.testing").artifactId("bundle-test-support").versionAsInProject()
        )};
    }

    private ServiceRegistration<ExecutorService> registerService(final ExecutorService pExpected) throws InterruptedException {
        final Hashtable<String, Object> props = new Hashtable<>();
        props.put("testexecutor", "true");
        props.put(Constants.SERVICE_RANKING, ranking++);
        final ServiceRegistration<ExecutorService> reg = context.registerService(ExecutorService.class, pExpected, props);
        sleep(1000);
        COMPONENT.execute(runnable);
        verify(pExpected).execute(runnable);
        return reg;
    }

    @Test
    public void verifyInformObserver() throws Exception {
        COMPONENT.execute(runnable);
        verify(defaultService, times(1)).execute(runnable);
        verify(observer, timeout(1000)).defaultInitialized(defaultService);
    }

    @Test
    public void verifySmartSwitch() throws Exception {
        COMPONENT.execute(runnable);
        final ServiceRegistration reg1 = registerService(realExecutor1);
        verify(defaultService).shutdown();
        final ServiceRegistration reg2 = registerService(realExecutor2);
        final ServiceRegistration reg3 = registerService(realExecutor3);
        reg3.unregister();
        reg2.unregister();
        reg1.unregister();
        COMPONENT.execute(runnable);
        verify(defaultService, times(2)).execute(runnable);
        verifyNoMoreInteractions(defaultService);
    }
}
