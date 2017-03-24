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

import ch.sourcepond.commons.smartswitch.api.ToDefaultSwitchObserver;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 */
class SmartSwitchFactory {

    <T> SmartSwitch<T> create(final Executor pExecutor,
                              final Supplier<T> pSupplier,
                              final Consumer<T> pShutdownHookOrNull,
                              final ToDefaultSwitchObserver<T> pObserverOrNull) {
        return new SmartSwitch<>(pExecutor, pSupplier, pShutdownHookOrNull, pObserverOrNull);
    }
}
