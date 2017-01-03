/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.commons.smartswitch.api;


import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Factory to create a smart-switch between a regular OSGi service and a fallback service if the former service
 * is not available.
 */
@SuppressWarnings("unused")
public interface SmartSwitchFactory {

    @SuppressWarnings("unused")
    interface ProxyFactory<T> {

        T instead();

        T insteadAndObserveAvailability(Consumer<T> pConsumer);
    }

    /**
     * Instances of this interface are capable to register a supplier of a default service.
     *
     * @param <T> The service type
     */
    interface FallbackSupplierRegistrar<T> {

        /**
         * Registers the supplier which provides the actual fallback service.
         *
         * @param pSupplier Supplier object, must not be {@code null}
         * @return ProxyFactory used to configure smart switch fluently, never {@code null}
         * @throws NullPointerException Thrown, if the supplier specified is {@code null}
         */
        ProxyFactory<T> isUnavailableThenUse(Supplier<T> pSupplier);
    }

    /**
     * Instances of this interface are capable to register a supplier of a fallback service. Additionally, an
     * OSGi service filter can be specified for more precises service matching.
     *
     * @param <T> The service type
     */
    @SuppressWarnings("unused")
    interface FilteredFallbackSupplierRegistrar<T> extends FallbackSupplierRegistrar<T> {

        /**
         * Registers the OSGi service filter to be used for OSGi service matching.
         *
         * @param pFilter Valid OSGi service filter, must be not {@code null}
         * @return FallbackSupplierRegistrar object used to configure smart switch fluently, never {@code null}
         * @throws NullPointerException     Thrown, if the filter specified is {@code null}
         * @throws IllegalArgumentException Thrown, if the filter specified is not a valid OSGi service filter
         */
        FallbackSupplierRegistrar<T> withFilter(String pFilter);

    }

    /**
     * Enables smart switching between an available OSGi service or a fallback service. Use it when you want to
     * transparently switch between an OSGi service or a fallback service when the OSGi service is not available.
     *
     * @param pInterface The service interface, must not be {@code null}
     * @param <T>        The service type
     * @return FilteredFallbackSupplierRegistrar object used to configure smart switch fluently, never {@code null}
     * @throws NullPointerException Thrown, if the class specified is {@code null}
     * @throws IllegalArgumentException Thrown, if the class specified is not an interface.
     */
    <T> FilteredFallbackSupplierRegistrar<T> whenService(Class<T> pInterface);
}
