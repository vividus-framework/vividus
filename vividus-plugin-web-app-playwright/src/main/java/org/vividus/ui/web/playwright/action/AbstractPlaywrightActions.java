/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.action;

import java.lang.reflect.Type;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public abstract class AbstractPlaywrightActions
{
    private final String name;
    private final boolean argumentRequired;
    private final Type argumentType;
    private Object argument;

    protected AbstractPlaywrightActions(String name, boolean argumentRequired, Type argumentType)
    {
        this.name = name;
        this.argumentRequired = argumentRequired;
        this.argumentType = argumentType;
    }

    public String getName()
    {
        return name;
    }

    public boolean isArgumentRequired()
    {
        return argumentRequired;
    }

    public Type getArgumentType()
    {
        return argumentType;
    }

    public Object getArgument()
    {
        return argument;
    }

    public void setArgument(Object argument)
    {
        this.argument = argument;
    }

    public abstract void execute(Locator locator, Page page);

    public abstract AbstractPlaywrightActions createAction();
}
