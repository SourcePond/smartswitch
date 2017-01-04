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
package ch.sourcepond.commons.smartswitch.impl;

import ch.sourcepond.commons.smartswitch.api.SmartSwitchFactory;

import java.util.function.Supplier;

/**
 * Implementation class for the {@code isUnavailableThenUse} part of the fluent configuration API.
 */
class DefaultFallbackSupplierRegistrar<T> implements SmartSwitchFactory.FallbackSupplierRegistrar<T> {
    final ConfigurationVisitor<T> visitor;

    DefaultFallbackSupplierRegistrar(final ConfigurationVisitor<T> pVisitor) {
        assert pVisitor != null : "pVisitor cannot be null";
        visitor = pVisitor;
    }

    @Override
    public SmartSwitchFactory.ProxyFactory<T> isUnavailableThenUse(final Supplier<T> pSupplier) {
        if (pSupplier == null) {
            throw new NullPointerException("Supplier specified is null");
        }
        visitor.setSupplier(pSupplier);
        return visitor;
    }
}
