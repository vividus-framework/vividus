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

package org.vividus.ui.web.storage;

import java.util.HashSet;
import java.util.Set;

import org.vividus.ui.web.action.WebJavascriptActions;

public class WebStorage
{
    private final WebJavascriptActions javascriptActions;

    public WebStorage(WebJavascriptActions javascriptActions)
    {
        this.javascriptActions = javascriptActions;
    }

    public Set<String> getKeys(StorageType storageType)
    {
        return new HashSet<>(executeScript(storageType, "return Object.keys(%s)"));
    }

    public String getItem(StorageType storageType, String key)
    {
        return executeScript(storageType, "return %s.getItem(arguments[0])", key);
    }

    public void setItem(StorageType storageType, String key, String value)
    {
        executeScript(storageType, "%s.setItem(arguments[0], arguments[1])", key, value);
    }

    public String removeItem(StorageType storageType, String key)
    {
        return executeScript(storageType,
                "var item = %1$s.getItem(arguments[0]); %1$s.removeItem(arguments[0]); return item", key);
    }

    public void clear(StorageType storageType)
    {
        executeScript(storageType, "%s.clear()");
    }

    public int getSize(StorageType storageType)
    {
        return executeScript(storageType, "return %s.length");
    }

    private <T> T executeScript(StorageType storageType, String format, Object... args)
    {
        return javascriptActions.executeScript(String.format(format, storageType.getJavascriptPropertyName()), args);
    }
}
