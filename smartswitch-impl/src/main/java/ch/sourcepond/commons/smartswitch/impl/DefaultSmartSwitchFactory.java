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
import org.osgi.framework.Bundle;

/**
 * Implementation class of the {@code whenService} part of the fluent configuration API.
 */
final class DefaultSmartSwitchFactory implements  SmartSwitchFactory {
    private final Bundle clientBundle;

    DefaultSmartSwitchFactory(final Bundle pClientBundle) {
        clientBundle = pClientBundle;
    }

    @Override
    public <T> FilteredFallbackSupplierRegistrar<T> whenService(final Class<T> pInterface) {
        if (pInterface == null) {
            throw new NullPointerException("Interface specified is null!");
        }
        if (!pInterface.isInterface()) {
            throw new IllegalArgumentException(String.format("%s is not an interface!", pInterface.getName()));
        }

        final ConfigurationVisitor<T> visitor = new ConfigurationVisitor<>(new DefaultInvocationHandlerFactory(), e -> {});
        visitor.setClientBundle(clientBundle);
        visitor.setServiceInterface(pInterface);
        return new DefaultFilteredFallbackSupplierRegistrar<>(visitor);
    }
}
