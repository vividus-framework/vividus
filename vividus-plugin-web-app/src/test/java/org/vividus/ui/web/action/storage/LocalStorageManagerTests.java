/*
 * Copyright 2019-2021 the original author or authors.
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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.html5.LocalStorage;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class LocalStorageManagerTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(LocalStorageManager.class);
    private static final String VALUE = "value";
    private static final String KEY = "Key";

    @Mock
    private ILocalStorageProvider localStorageProvider;

    @Mock
    private LocalStorage localStorage;

    @InjectMocks
    private LocalStorageManager localStorageManager;

    @BeforeEach
    void beforeEach()
    {
        when(localStorageProvider.getLocalStorage()).thenReturn(localStorage);
    }

    @Test
    void shouldReturnItemByKey()
    {
        localStorageManager.getItem(KEY);
        verify(localStorage).getItem(KEY);
    }

    @Test
    void shouldReturnKeySet()
    {
        localStorageManager.getKeys();
        verify(localStorage).keySet();
    }

    @Test
    void shouldSetItem()
    {
        localStorageManager.setItem(KEY, VALUE);
        verify(localStorage).setItem(KEY, VALUE);
        assertThat(LOGGER.getLoggingEvents(),
                is(List.of(info("Adding local storage item. Key: {} Value: {}", KEY, VALUE))));
    }

    @Test
    void shouldRemoveItem()
    {
        localStorageManager.removeItem(KEY);
        verify(localStorage).removeItem(KEY);
        assertThat(LOGGER.getLoggingEvents(), is(List.of(info("Removing local storage item with key: {}", KEY))));
    }

    @Test
    void shouldClearStorage()
    {
        localStorageManager.clear();
        verify(localStorage).clear();
    }

    @Test
    void shouldReturnStorageSize()
    {
        localStorageManager.getSize();
        verify(localStorage).size();
    }
}
