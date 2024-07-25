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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.action.WebJavascriptActions;

@ExtendWith(MockitoExtension.class)
class SeleniumWebStorageTests
{
    private static final String KEY = "key";
    private static final String VALUE = "value";

    @Mock private WebJavascriptActions javascriptActions;
    @InjectMocks private SeleniumWebStorage webStorage;

    @Test
    void shouldReturnLocalStorageItemByKey()
    {
        webStorage.getItem(StorageType.LOCAL, KEY);
        verify(javascriptActions).executeScript("return localStorage.getItem(arguments[0])", KEY);
    }

    @Test
    void shouldReturnLocalStorageKeySet()
    {
        when(javascriptActions.executeScript("return Object.keys(localStorage)")).thenReturn(List.of(KEY));
        var keys = webStorage.getKeys(StorageType.LOCAL);
        assertEquals(Set.of(KEY), keys);
    }

    @Test
    void shouldSetLocalStorageItem()
    {
        webStorage.setItem(StorageType.LOCAL, KEY, VALUE);
        verify(javascriptActions).executeScript("localStorage.setItem(arguments[0], arguments[1])", KEY, VALUE);
    }

    @Test
    void shouldRemoveLocalStorageItem()
    {
        when(javascriptActions.executeScript(
                "var item = localStorage.getItem(arguments[0]); localStorage.removeItem(arguments[0]); return item",
                KEY)).thenReturn(VALUE);
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
        when(javascriptActions.executeScript("return localStorage.length")).thenReturn(1);
        assertEquals(1, webStorage.getSize(StorageType.LOCAL));
    }

    @Test
    void shouldReturnSessionStorageItemByKey()
    {
        webStorage.getItem(StorageType.SESSION, KEY);
        verify(javascriptActions).executeScript("return sessionStorage.getItem(arguments[0])", KEY);
    }

    @Test
    void shouldReturnSessionStorageKeySet()
    {
        when(javascriptActions.executeScript("return Object.keys(sessionStorage)")).thenReturn(List.of(KEY));
        var keys = webStorage.getKeys(StorageType.SESSION);
        assertEquals(Set.of(KEY), keys);
    }

    @Test
    void shouldSetSessionStorageItem()
    {
        webStorage.setItem(StorageType.SESSION, KEY, VALUE);
        verify(javascriptActions).executeScript("sessionStorage.setItem(arguments[0], arguments[1])", KEY,
                VALUE);
    }

    @Test
    void shouldRemoveSessionStorageItem()
    {
        when(javascriptActions.executeScript(
                "var item = sessionStorage.getItem(arguments[0]); sessionStorage.removeItem(arguments[0]); return item",
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
        when(javascriptActions.executeScript("return sessionStorage.length")).thenReturn(1);
        assertEquals(1, webStorage.getSize(StorageType.SESSION));
    }
}
