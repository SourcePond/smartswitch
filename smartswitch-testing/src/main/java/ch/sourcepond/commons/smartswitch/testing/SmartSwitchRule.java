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
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.junit.Assert.*;

/**
 * Rule to create a stub {@link SmartSwitchFactory} factory. It allows to register mocks for services. Using the
 * default service instead is also supported.
 */
public class SmartSwitchRule implements TestRule {

    private final TestSmartSwitchFactory factory = new TestSmartSwitchFactory();
    private final Map<String, Entry<?>> entries = new HashMap<>();

    @Override
    public Statement apply(final Statement statement, final Description description) {
        return new Statement() {
            @SuppressWarnings("unchecked")
            @Override
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();
                } finally {
                    for (final Entry<?> value : entries.values()) {
                        final Entry<Object> entry = (Entry<Object>) value;
                        if (entry.getAvailabilityHook() != null && entry.getDefaultService() != null) {
                            entry.getAvailabilityHook().accept(entry.getDefaultService());
                        }
                    }
                }
            }
        };
    }

    /**
     * {@link SmartSwitchFactory} implementation.
     */
    private class TestSmartSwitchFactory implements SmartSwitchFactory {


        private class TestingFallbackSupplierRegistrar<T> implements FallbackSupplierRegistrar<T> {
            final Class<T> serviceInterface;
            Entry<T> entry;

            TestingFallbackSupplierRegistrar(final Class<T> pServiceInterface) {
                this(null, pServiceInterface);
            }

            TestingFallbackSupplierRegistrar(final Entry<T> pEntryOrNull, final Class<T> pServiceInterface) {
                serviceInterface = pServiceInterface;
                entry = pEntryOrNull;
            }

            @Override
            public ProxyFactory<T> isUnavailableThenUse(final Supplier<T> pSupplier) {
                if (null == entry) {
                    entry = getEntry(serviceInterface, null);
                }
                entry.setSupplier(pSupplier);
                entry.setDefaultService(pSupplier.get());
                return new TestingProxy<>(entry);
            }
        }

        private class TestingFilteredFallbackSupplierRegistrar<T> extends TestingFallbackSupplierRegistrar<T> implements
                FilteredFallbackSupplierRegistrar<T> {

            TestingFilteredFallbackSupplierRegistrar(final Class<T> pServiceInterface) {
                super(pServiceInterface);
            }

            @Override
            public FallbackSupplierRegistrar<T> withFilter(final String pFilterOrNull) {
                entry = getEntry(serviceInterface, pFilterOrNull);
                return new TestingFallbackSupplierRegistrar<>(entry, serviceInterface);
            }
        }

        private class TestingProxy<T> implements ProxyFactory<T> {
            private final Entry<T> entry;

            TestingProxy(final Entry<T> pEntry) {
                entry = pEntry;
            }

            private T getService() {
                T service = entry.getOsgiService();
                if (null == service) {
                    service = entry.getDefaultService();
                }
                assertNotNull(format("Neither an OSGi nor a default service has been specified for %s with filter %s",
                        entry.getServiceInterface().getName(), entry.getFilterOrNull()), service);
                return service;
            }

            @Override
            public T instead() {
                return getService();
            }

            @Override
            public T insteadAndExecuteWhenAvailable(final Consumer<T> pAvailabilityHook) {
                entry.setAvailabilityHook(pAvailabilityHook);
                return getService();
            }
        }

        @Override
        public <T> FilteredFallbackSupplierRegistrar<T> whenService(final Class<T> pServiceInterface) {
            return new TestingFilteredFallbackSupplierRegistrar<>(pServiceInterface);
        }

    }

    private String key(final Class<?> pServiceInterface, final String pFilterOrNull) {
        final StringBuilder builder = new StringBuilder(pServiceInterface.getName());
        if (null != pFilterOrNull) {
            builder.append('-').append(pFilterOrNull);
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private <T> Entry<T> getEntry(final Class<T> pServiceInterface, final String pFilterOrNull) {
        final Entry<T> entry = (Entry<T>) entries.get(key(pServiceInterface, pFilterOrNull));
        assertNotNull(format("No entry registered for interface %s and filter %s", pServiceInterface.getName(), pFilterOrNull), entry);
        return entry;
    }

    @SuppressWarnings("WeakerAccess")
    public <T> T useOsgiService(final Class<T> pServiceInterface) {
        return useOsgiService(pServiceInterface, null);
    }

    @SuppressWarnings("WeakerAccess")
    public <T> T useOsgiService(final Class<T> pServiceInterface, final String pFilterOrNull) {
        final T mock = Mockito.mock(pServiceInterface);
        final Entry<T> entry = new Entry<>();
        entry.setServiceInterface(pServiceInterface);
        entry.setOsgiService(mock);
        entry.setFilterOrNull(pFilterOrNull);
        entries.put(key(pServiceInterface, pFilterOrNull), entry);
        return mock;
    }

    /**
     * Configures that the default service is used for the interface and filter specified.
     *
     * @param pServiceInterface Service interface, must not be {@code null}
     * @param pFilterOrNull     Filter or {@code null}
     */
    @SuppressWarnings({"WeakerAccess", "unchecked"})
    public void useDefaultService(final Class<?> pServiceInterface, final String pFilterOrNull) {
        final Entry<Object> entry = new Entry<>();
        entry.setServiceInterface((Class<Object>) pServiceInterface);
        entry.setFilterOrNull(pFilterOrNull);
        entries.put(key(pServiceInterface, pFilterOrNull), entry);
    }

    @SuppressWarnings("WeakerAccess")
    public <T> Supplier<T> getSupplier(final Class<T> pServiceInterface) {
        return getSupplier(pServiceInterface, null);
    }

    @SuppressWarnings("WeakerAccess")
    public <T> Supplier<T> getSupplier(final Class<T> pServiceInterface, final String pFilterOrNull) {
        return getEntry(pServiceInterface, pFilterOrNull).getSupplier();
    }

    @SuppressWarnings("WeakerAccess")
    public <T> Consumer<T> getAvailabilityHook(final Class<T> pServiceInterface) {
        return getAvailabilityHook(pServiceInterface, null);
    }

    @SuppressWarnings("WeakerAccess")
    public <T> Consumer<T> getAvailabilityHook(final Class<T> pServiceInterface, final String pFilterOrNull) {
        return getEntry(pServiceInterface, pFilterOrNull).getAvailabilityHook();
    }

    @SuppressWarnings("WeakerAccess")
    public SmartSwitchFactory getTestFactory() {
        return factory;
    }
}
