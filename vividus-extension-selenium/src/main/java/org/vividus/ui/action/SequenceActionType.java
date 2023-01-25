/*
 * Copyright 2019-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.ui.action;

import static org.apache.commons.lang3.Validate.isTrue;

import java.lang.reflect.Type;
import java.util.function.Consumer;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.openqa.selenium.WebElement;

public interface SequenceActionType<T>
{
    void addAction(T action, Object argument);

    Type getArgumentType();

    boolean isNullable();

    String name();

    default <V, U extends WebElement> void performOnWebElement(V argument, Consumer<U> argumentConsumer,
            Runnable noArgumentRunner)
    {
        perform(argument, argumentConsumer, noArgumentRunner);
    }

    default <V, U> void perform(V argument, Consumer<U> argumentConsumer)
    {
        perform(argument, argumentConsumer, () -> { });
    }

    @SuppressWarnings("unchecked")
    default <V, U> void perform(V argument, Consumer<U> argumentConsumer, Runnable noArgumentRunner)
    {
        if (argument == null)
        {
            noArgumentRunner.run();
        }
        else
        {
            isTrue(TypeUtils.isAssignable(argument.getClass(), getArgumentType()),
                    "Argument for %s action must be of type %s", name(), getArgumentType());
            argumentConsumer.accept((U) argument);
        }
    }
}
