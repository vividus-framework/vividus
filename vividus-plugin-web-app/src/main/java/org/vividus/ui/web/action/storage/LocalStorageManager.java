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

import java.util.Set;

import javax.inject.Inject;

import org.openqa.selenium.html5.LocalStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalStorageManager implements ILocalStorageManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalStorageManager.class);

    @Inject private ILocalStorageProvider localStorageProvider;

    @Override
    public String getItem(String key)
    {
        return getLocalStorage().getItem(key);
    }

    @Override
    public Set<String> getKeys()
    {
        return getLocalStorage().keySet();
    }

    @Override
    public void setItem(String key, String value)
    {
        LOGGER.info("Adding local storage item. Key: {} Value: {}", key, value);
        getLocalStorage().setItem(key, value);
    }

    @Override
    public void removeItem(String key)
    {
        LOGGER.info("Removing local storage item with key: {}", key);
        getLocalStorage().removeItem(key);
    }

    @Override
    public void clear()
    {
        getLocalStorage().clear();
    }

    @Override
    public int getSize()
    {
        return getLocalStorage().size();
    }

    private LocalStorage getLocalStorage()
    {
        return localStorageProvider.getLocalStorage();
    }
}
