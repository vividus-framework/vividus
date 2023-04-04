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

package org.vividus.selenium;

import static java.util.Map.entry;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.EnumUtils;
import org.openqa.selenium.Keys;
import org.vividus.selenium.manager.IWebDriverManager;

public final class KeysManager
{
    private static final String OS_INDEPENDENT_CONTROL = "OS_INDEPENDENT_CONTROL";

    private final IWebDriverManager webDriverManager;

    public KeysManager(IWebDriverManager webDriverManager)
    {
        this.webDriverManager = webDriverManager;
    }

    public CharSequence[] convertToKeys(List<String> keys)
    {
        return keys.stream().map(key -> convertToKey(false, key)).toArray(CharSequence[]::new);
    }

    public CharSequence convertToKey(boolean validate, String key)
    {
        if (OS_INDEPENDENT_CONTROL.equalsIgnoreCase(key))
        {
            return getOsIndependentControlKey().getKey();
        }
        if (EnumUtils.isValidEnum(Keys.class, key))
        {
            return Keys.valueOf(key);
        }
        if (!validate)
        {
            return key;
        }
        throw new IllegalArgumentException(String.format("The '%s' is not allowed as a key", key));
    }

    public Entry<Keys, String> getOsIndependentControlKey()
    {
        return webDriverManager.isMacOs() ? entry(Keys.COMMAND, "Cmd") : entry(Keys.CONTROL, "Ctrl");
    }
}
