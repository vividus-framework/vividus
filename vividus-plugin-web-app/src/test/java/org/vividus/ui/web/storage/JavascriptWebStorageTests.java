/*
 * Copyright 2019-2022 the original author or authors.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.vividus.ui.web.action.WebJavascriptActions;

class JavascriptWebStorageTests
{
    private static final String KEY = "key";
    private static final String VALUE = "value";

    private final WebJavascriptActions javascriptActions = mock(WebJavascriptActions.class);
    private final JavascriptWebStorage javascriptWebStorage = new JavascriptWebStorage(javascriptActions);

    @Test
    void shouldReturnLocalStorageItemByKey()
    {
        javascriptWebStorage.getLocalStorage().getItem(KEY);
        verify(javascriptActions).executeScript("return window.localStorage.getItem(arguments[0])", KEY);
    }

    @Test
    void shouldReturnLocalStorageKeySet()
    {
        when(javascriptActions.executeScript("return Object.keys(window.localStorage)")).thenReturn(List.of(KEY));
        Set<String> keys = javascriptWebStorage.getLocalStorage().keySet();
        assertEquals(Set.of(KEY), keys);
    }

    @Test
    void shouldSetLocalStorageItem()
    {
        javascriptWebStorage.getLocalStorage().setItem(KEY, VALUE);
        verify(javascriptActions).executeScript("window.localStorage.setItem(arguments[0], arguments[1])", KEY, VALUE);
    }

    @Test
    void shouldRemoveLocalStorageItem()
    {
        javascriptWebStorage.getLocalStorage().removeItem(KEY);
        verify(javascriptActions).executeScript("window.localStorage.removeItem(arguments[0])", KEY);
    }

    @Test
    void shouldClearLocalStorage()
    {
        javascriptWebStorage.getLocalStorage().clear();
        verify(javascriptActions).executeScript("window.localStorage.clear()");
    }

    @Test
    void shouldReturnLocalStorageSize()
    {
        when(javascriptActions.executeScript("return window.localStorage.length")).thenReturn(1);
        assertEquals(1, javascriptWebStorage.getLocalStorage().size());
    }

    @Test
    void shouldReturnSessionStorageItemByKey()
    {
        javascriptWebStorage.getSessionStorage().getItem(KEY);
        verify(javascriptActions).executeScript("return window.sessionStorage.getItem(arguments[0])", KEY);
    }

    @Test
    void shouldReturnSessionStorageKeySet()
    {
        when(javascriptActions.executeScript("return Object.keys(window.sessionStorage)")).thenReturn(List.of(KEY));
        Set<String> keys = javascriptWebStorage.getSessionStorage().keySet();
        assertEquals(Set.of(KEY), keys);
    }

    @Test
    void shouldSetSessionStorageItem()
    {
        javascriptWebStorage.getSessionStorage().setItem(KEY, VALUE);
        verify(javascriptActions).executeScript("window.sessionStorage.setItem(arguments[0], arguments[1])", KEY,
                VALUE);
    }

    @Test
    void shouldRemoveSessionStorageItem()
    {
        javascriptWebStorage.getSessionStorage().removeItem(KEY);
        verify(javascriptActions).executeScript("window.sessionStorage.removeItem(arguments[0])", KEY);
    }

    @Test
    void shouldClearSessionStorage()
    {
        javascriptWebStorage.getSessionStorage().clear();
        verify(javascriptActions).executeScript("window.sessionStorage.clear()");
    }

    @Test
    void shouldReturnSessionStorageSize()
    {
        when(javascriptActions.executeScript("return window.sessionStorage.length")).thenReturn(1);
        assertEquals(1, javascriptWebStorage.getSessionStorage().size());
    }
}
