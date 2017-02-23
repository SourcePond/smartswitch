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

import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.tinybundles.core.TinyBundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

/**
 *
 */
final class TestProbeBuilderAdapter implements TestProbeBuilder {
    private final TestProbeBuilder delegate;

    TestProbeBuilderAdapter(final TestProbeBuilder pDelegate) {
        delegate = pDelegate;
    }

    @Override
    public TestAddress addTest(final Class<?> aClass, final String s, final Object... objects) {
        return delegate.addTest(aClass, s, objects);
    }

    @Override
    public TestAddress addTest(final Class<?> aClass, final Object... objects) {
        return delegate.addTest(aClass, objects);
    }

    @Override
    public List<TestAddress> addTests(final Class<?> aClass, final Method... methods) {
        return delegate.addTests(aClass, methods);
    }

    @Override
    public Set<TestAddress> getTests() {
        return delegate.getTests();
    }

    @Override
    public TestProbeBuilder setHeader(final String s, final String s1) {
        return delegate.setHeader(s, s1);
    }

    @Override
    public TestProbeBuilder ignorePackageOf(final Class<?>[] classes) {
        return delegate.ignorePackageOf(classes);
    }

    @Override
    public TestProbeProvider build() {
        final TestProbeProvider provider = delegate.build();
        return new TestProbeProvider() {
            @Override
            public Set<TestAddress> getTests() {
                return provider.getTests();
            }

            @Override
            public InputStream getStream() throws IOException {
                final TinyBundle bundle = bundle().read(provider.getStream());
                bundle.add(TestActivator.class);
                bundle.set("Bundle-Activator", TestActivator.class.getName());
                return bundle.build();
            }
        };
    }

    @Override
    public File getTempDir() {
        return delegate.getTempDir();
    }

    @Override
    public void setTempDir(final File file) {
        delegate.setTempDir(file);
    }
}
