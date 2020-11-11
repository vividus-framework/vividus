/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.ui.web.action.storage;

import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.html5.LocalStorage;
import org.vividus.ui.web.action.WebJavascriptActions;

public class JavascriptLocalStorage implements LocalStorage
{
    private final WebJavascriptActions javascriptActions;

    JavascriptLocalStorage(WebJavascriptActions javascriptActions)
    {
        this.javascriptActions = javascriptActions;
    }

    @Override
    public String getItem(String key)
    {
        return javascriptActions.executeScript("return window.localStorage.getItem(arguments[0]);", key);
    }

    @Override
    public Set<String> keySet()
    {
        return new HashSet<>(javascriptActions.executeScript("return Object.keys(window.localStorage)"));
    }

    @Override
    public void setItem(String key, String value)
    {
        javascriptActions.executeScript("window.localStorage.setItem(arguments[0], arguments[1]);", key, value);
    }

    @Override
    public String removeItem(String key)
    {
        return javascriptActions.executeScript("window.localStorage.removeItem(arguments[0]);", key);
    }

    @Override
    public void clear()
    {
        javascriptActions.executeScript("window.localStorage.clear();");
    }

    @Override
    public int size()
    {
        return (int) javascriptActions.executeScript("return window.localStorage.length");
    }
}
