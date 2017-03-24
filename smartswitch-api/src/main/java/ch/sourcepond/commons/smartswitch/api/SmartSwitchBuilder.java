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
package ch.sourcepond.commons.smartswitch.api;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 */
public interface SmartSwitchBuilder<T> {

    /**
     * Sets the observer specified on the service-proxy. When a service gets added or removed
     * the observer will be informed <em>asychronously</em>. If the observer specified is
     * {@code null}, nothing happens
     *
     * @param pObserver Observer to set on the service proxy.
     * @return This builder, never {@code null}.
     */
    SmartSwitchBuilder<T> setObserver(ToDefaultSwitchObserver<T> pObserver);

    /**
     * If specified, the service-proxy will filter potential services. If the filter
     * specified is {@code null}, nothing happens.
     *
     * @param pFilter Valid OSGi service filter.
     * @return This builder, never {@code null}.
     */
    SmartSwitchBuilder<T> setFilter(String pFilter);

    SmartSwitchBuilder<T> setShutdownHook(Consumer<T> pShutdownHook);

    T build(Supplier<T> pSupplier);
}
