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
import org.osgi.framework.InvalidSyntaxException;

/**
 * Created by rolandhauser on 23.12.16.
 */
final class DefaultFilteredFallbackSupplierRegistrar<T> extends DefaultFallbackSupplierRegistrar<T>
        implements SmartSwitchFactory.FilteredFallbackSupplierRegistrar<T> {

    DefaultFilteredFallbackSupplierRegistrar(final ConfigurationVisitor<T> pVisitor) {
        super(pVisitor);
    }

    public SmartSwitchFactory.FallbackSupplierRegistrar<T> withFilter(final String pFilter) {
        if (pFilter == null) {
            throw new NullPointerException("Filter specified is null");
        }
        try {
            visitor.getBundleContext().createFilter(pFilter);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        visitor.setFilterOrNull(pFilter);
        return new DefaultFallbackSupplierRegistrar<>(visitor);
    }
}
