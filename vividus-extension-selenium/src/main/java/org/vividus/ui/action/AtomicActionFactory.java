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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.openqa.selenium.interactions.Actions;

public class AtomicActionFactory<T extends Actions, U>
{
    private final String name;
    private final Type argumentType;
    private final BiConsumer<T, U> argumentBasedActionFactory;
    private final Consumer<T> noArgumentActionFactory;

    protected AtomicActionFactory(String name, Type argumentType, BiConsumer<T, U> argumentBasedActionFactory)
    {
        this(name, argumentType, argumentBasedActionFactory, null);
    }

    protected AtomicActionFactory(String name, Type argumentType, BiConsumer<T, U> argumentBasedActionFactory,
            Consumer<T> noArgumentActionFactory)
    {
        this.name = name;
        this.argumentType = argumentType;
        this.argumentBasedActionFactory = argumentBasedActionFactory;
        this.noArgumentActionFactory = noArgumentActionFactory;
    }

    public String getName()
    {
        return name;
    }

    public Type getArgumentType()
    {
        return argumentType;
    }

    public boolean isArgumentRequired()
    {
        return noArgumentActionFactory == null;
    }

    @SuppressWarnings("unchecked")
    public void addAction(T actions, Object argument)
    {
        if (argument == null)
        {
            noArgumentActionFactory.accept(actions);
        }
        else
        {
            isTrue(TypeUtils.isAssignable(argument.getClass(), getArgumentType()),
                    "Argument for %s action must be of %s", getName(), getArgumentType());
            argumentBasedActionFactory.accept(actions, (U) argument);
        }
    }
}
