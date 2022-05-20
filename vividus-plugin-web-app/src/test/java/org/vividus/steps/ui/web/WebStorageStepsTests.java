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

package org.vividus.steps.ui.web;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.html5.Storage;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.storage.StorageType;
import org.vividus.ui.web.storage.WebStorageManager;
import org.vividus.variable.VariableScope;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class WebStorageStepsTests
{
    private static final String KEY = "key";
    private static final String VALUE = "value";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(WebStorageSteps.class);

    @Mock private WebStorageManager webStorageManager;
    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private WebStorageSteps webStorageSteps;

    @Test
    void shouldSaveWebStorageItemToVariable()
    {
        var storageType = mock(StorageType.class);
        Storage storage = mock(Storage.class);
        when(webStorageManager.getStorage(storageType)).thenReturn(storage);
        when(storage.getItem(KEY)).thenReturn(VALUE);
        var scopes = Set.of(VariableScope.STEP);

        webStorageSteps.saveWebStorageItemToVariable(storageType, KEY, scopes, KEY);

        verify(variableContext).putVariable(scopes, KEY, VALUE);
    }

    @Test
    void shouldSetWebStorageItem()
    {
        var storageType = mock(StorageType.class);
        Storage storage = mock(Storage.class);
        when(webStorageManager.getStorage(storageType)).thenReturn(storage);
        webStorageSteps.setWebStorageItem(storageType, KEY, VALUE);

        verify(storage).setItem(KEY, VALUE);
        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Setting {} storage item with key '{}' and value '{}", storageType, KEY, VALUE))));
    }

    @Test
    void shouldAssertThatWebStorageItemExists()
    {
        var storageType = StorageType.LOCAL;
        Storage storage = mock(Storage.class);
        when(webStorageManager.getStorage(storageType)).thenReturn(storage);
        when(storage.getItem(KEY)).thenReturn(VALUE);

        webStorageSteps.assertWebStorageItemExists(storageType, KEY);

        verify(softAssert).assertThat(eq("Local storage item with key 'key'"), eq(VALUE),
                argThat(m -> "not null".equals(m.toString())));
    }

    @Test
    void shouldAssertThatWebStorageItemDoesNotExist()
    {
        var storageType = StorageType.SESSION;
        Storage storage = mock(Storage.class);
        when(webStorageManager.getStorage(storageType)).thenReturn(storage);
        when(storage.getItem(KEY)).thenReturn(VALUE);

        webStorageSteps.assertWebStorageItemDoesNotExist(storageType, KEY);

        verify(softAssert).assertThat(eq("Session storage item with key 'key'"), eq(VALUE),
                argThat(m -> "null".equals(m.toString())));
    }
}
