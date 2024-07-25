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

package org.vividus.ui.web.playwright.storage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.vividus.ui.web.playwright.action.PlaywrightJavascriptActions;
import org.vividus.ui.web.storage.StorageType;
import org.vividus.ui.web.storage.WebStorage;

public class PlaywrightWebStorage implements WebStorage
{
    private final PlaywrightJavascriptActions javascriptActions;

    public PlaywrightWebStorage(PlaywrightJavascriptActions javascriptActions)
    {
        this.javascriptActions = javascriptActions;
    }

    @Override
    public Set<String> getKeys(StorageType storageType)
    {
        return new HashSet<>(executeScript(storageType, "Object.keys(%s)"));
    }

    @Override
    public String getItem(StorageType storageType, String key)
    {
        return executeScript(storageType, "key => %s.getItem(key)", key);
    }

    @Override
    public void setItem(StorageType storageType, String key, String value)
    {
        executeScript(storageType, "([key, value]) => %s.setItem(key, value)", Arrays.asList(key, value));
    }

    @Override
    public String removeItem(StorageType storageType, String key)
    {
        return executeScript(storageType,
                "key => {\nvar item = %1$s.getItem(key); %1$s.removeItem(key); return item\n}", key);
    }

    @Override
    public void clear(StorageType storageType)
    {
        executeScript(storageType, "%s.clear()");
    }

    @Override
    public int getSize(StorageType storageType)
    {
        return executeScript(storageType, "%s.length");
    }

    private <T> T executeScript(StorageType storageType, String format)
    {
        return javascriptActions.executeScript(String.format(format, storageType.getJavascriptPropertyName()));
    }

    private <T> T executeScript(StorageType storageType, String format, Object arg)
    {
        return javascriptActions.executeScript(String.format(format, storageType.getJavascriptPropertyName()), arg);
    }
}
