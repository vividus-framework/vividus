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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.action.PlaywrightJavascriptActions;
import org.vividus.ui.web.storage.StorageType;

@ExtendWith(MockitoExtension.class)
class PlaywrightWebStorageTests
{
    private static final String KEY = "key";
    private static final String VALUE = "value";

    @Mock private PlaywrightJavascriptActions javascriptActions;
    @InjectMocks private PlaywrightWebStorage webStorage;

    @Test
    void shouldReturnLocalStorageItemByKey()
    {
        webStorage.getItem(StorageType.LOCAL, KEY);
        verify(javascriptActions).executeScript("key => localStorage.getItem(key)", KEY);
    }

    @Test
    void shouldReturnLocalStorageKeySet()
    {
        when(javascriptActions.executeScript("Object.keys(localStorage)")).thenReturn(List.of(KEY));
        var keys = webStorage.getKeys(StorageType.LOCAL);
        assertEquals(Set.of(KEY), keys);
    }

    @Test
    void shouldSetLocalStorageItem()
    {
        webStorage.setItem(StorageType.LOCAL, KEY, VALUE);
        verify(javascriptActions).executeScript("([key, value]) => localStorage.setItem(key, value)",
                Arrays.asList(KEY, VALUE));
    }

    @Test
    void shouldRemoveLocalStorageItem()
    {
        when(javascriptActions.executeScript(
                "key => {\nvar item = localStorage.getItem(key); localStorage.removeItem(key); return item\n}", KEY))
                .thenReturn(VALUE);
        var actual = webStorage.removeItem(StorageType.LOCAL, KEY);
        assertEquals(VALUE, actual);
    }

    @Test
    void shouldClearLocalStorage()
    {
        webStorage.clear(StorageType.LOCAL);
        verify(javascriptActions).executeScript("localStorage.clear()");
    }

    @Test
    void shouldReturnLocalStorageSize()
    {
        when(javascriptActions.executeScript("localStorage.length")).thenReturn(1);
        assertEquals(1, webStorage.getSize(StorageType.LOCAL));
    }

    @Test
    void shouldReturnSessionStorageItemByKey()
    {
        webStorage.getItem(StorageType.SESSION, KEY);
        verify(javascriptActions).executeScript("key => sessionStorage.getItem(key)", KEY);
    }

    @Test
    void shouldReturnSessionStorageKeySet()
    {
        when(javascriptActions.executeScript("Object.keys(sessionStorage)")).thenReturn(List.of(KEY));
        var keys = webStorage.getKeys(StorageType.SESSION);
        assertEquals(Set.of(KEY), keys);
    }

    @Test
    void shouldSetSessionStorageItem()
    {
        webStorage.setItem(StorageType.SESSION, KEY, VALUE);
        verify(javascriptActions).executeScript("([key, value]) => sessionStorage.setItem(key, value)",
                Arrays.asList(KEY, VALUE));
    }

    @Test
    void shouldRemoveSessionStorageItem()
    {
        when(javascriptActions.executeScript(
                "key => {\nvar item = sessionStorage.getItem(key); sessionStorage.removeItem(key); return item\n}",
                KEY)).thenReturn(VALUE);
        var actual = webStorage.removeItem(StorageType.SESSION, KEY);
        assertEquals(VALUE, actual);
    }

    @Test
    void shouldClearSessionStorage()
    {
        webStorage.clear(StorageType.SESSION);
        verify(javascriptActions).executeScript("sessionStorage.clear()");
    }

    @Test
    void shouldReturnSessionStorageSize()
    {
        when(javascriptActions.executeScript("sessionStorage.length")).thenReturn(1);
        assertEquals(1, webStorage.getSize(StorageType.SESSION));
    }
}
