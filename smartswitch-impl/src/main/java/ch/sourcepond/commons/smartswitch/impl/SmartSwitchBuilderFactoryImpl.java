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

import ch.sourcepond.commons.smartswitch.api.SmartSwitchBuilder;
import ch.sourcepond.commons.smartswitch.api.SmartSwitchBuilderFactory;
import org.osgi.framework.Bundle;

import java.util.concurrent.Executor;

/**
 *
 */
class SmartSwitchBuilderFactoryImpl implements SmartSwitchBuilderFactory {
    private final Bundle bundle;
    private final Executor executor;
    private final SmartSwitchFactory smartSwitchFactory;

    SmartSwitchBuilderFactoryImpl(final Bundle pBundle,
                                  final Executor pExecutor,
                                  final SmartSwitchFactory pSmartSwitchFactory) {
        bundle = pBundle;
        executor = pExecutor;
        smartSwitchFactory = pSmartSwitchFactory;
    }

    @Override
    public <T> SmartSwitchBuilder<T> newBuilder(final Class<T> pInterface) {
        return new SmartSwitchBuilderImpl<>(smartSwitchFactory, executor, bundle.getBundleContext(), pInterface);
    }
}
