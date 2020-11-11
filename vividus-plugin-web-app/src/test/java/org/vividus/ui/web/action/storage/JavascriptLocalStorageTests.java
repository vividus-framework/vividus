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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.action.WebJavascriptActions;

@ExtendWith(MockitoExtension.class)
class JavascriptLocalStorageTests
{
    private static final String VALUE = "value";
    private static final String KEY = "key";

    @Mock
    private WebJavascriptActions javascriptActions;

    @InjectMocks
    private JavascriptLocalStorage javascriptLocalStorage;

    @Test
    void getItemTest()
    {
        javascriptLocalStorage.getItem(KEY);
        verify(javascriptActions).executeScript("return window.localStorage.getItem(arguments[0]);", KEY);
    }

    @Test
    void keySetTest()
    {
        when(javascriptActions.executeScript("return Object.keys(window.localStorage)")).thenReturn(List.of(KEY));
        Set<String> keys = javascriptLocalStorage.keySet();
        assertTrue(keys instanceof HashSet);
        assertThat(javascriptLocalStorage.keySet(), Matchers.contains(KEY));
    }

    @Test
    void setItemTest()
    {
        javascriptLocalStorage.setItem(KEY, VALUE);
        verify(javascriptActions).executeScript("window.localStorage.setItem(arguments[0], arguments[1]);", KEY, VALUE);
    }

    @Test
    void removeItem()
    {
        javascriptLocalStorage.removeItem(KEY);
        verify(javascriptActions).executeScript("window.localStorage.removeItem(arguments[0]);", KEY);
    }

    @Test
    void clearTest()
    {
        javascriptLocalStorage.clear();
        verify(javascriptActions).executeScript("window.localStorage.clear();");
    }

    @Test
    void sizeTest()
    {
        when(javascriptActions.executeScript("return window.localStorage.length")).thenReturn(1);
        assertEquals(1, javascriptLocalStorage.size());
    }
}
