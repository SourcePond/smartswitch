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

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by rolandhauser on 13.01.17.
 */
final class Entry<T> {
    private Class<T> serviceInterface;
    private String filterOrNull;
    private Supplier<T> supplier;
    private Consumer<T> availabilityHook;
    private T osgiService;
    private T defaultService;

    public Class<T> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(final Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public String getFilterOrNull() {
        return filterOrNull;
    }

    public void setFilterOrNull(final String filterOrNull) {
        this.filterOrNull = filterOrNull;
    }

    public Supplier<T> getSupplier() {
        return supplier;
    }

    public void setSupplier(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public Consumer<T> getAvailabilityHook() {
        return availabilityHook;
    }

    public void setAvailabilityHook(final Consumer<T> availabilityHook) {
        this.availabilityHook = availabilityHook;
    }

    public T getOsgiService() {
        return osgiService;
    }

    public void setOsgiService(final T osgiService) {
        this.osgiService = osgiService;
    }

    public T getDefaultService() {
        return defaultService;
    }

    public void setDefaultService(final T defaultService) {
        this.defaultService = defaultService;
    }
}
