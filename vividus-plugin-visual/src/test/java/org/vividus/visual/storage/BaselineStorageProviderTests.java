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

package org.vividus.visual.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class BaselineStorageProviderTests
{
    public static final String EXISTING = "existing";
    public static final String NOT_EXISTING = "not_existing";

    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private BaselineStorage baselineStorage;

    @InjectMocks
    private BaselineStorageProvider baselineStorageProvider;

    void mockContext()
    {
        when(applicationContext.getBeanNamesForType(BaselineStorage.class)).thenReturn(new String[]{ EXISTING,
                NOT_EXISTING });
        when(applicationContext.getBean(EXISTING, BaselineStorage.class)).thenReturn(baselineStorage);
        when(applicationContext.getBean(NOT_EXISTING, BaselineStorage.class)).thenThrow(
                new BeanCreationException(NOT_EXISTING, new ClassNotFoundException(NOT_EXISTING)));
    }

    @Test
    void shouldReturnExistingBaselineStorage()
    {
        mockContext();
        baselineStorageProvider.init();
        assertSame(baselineStorage, baselineStorageProvider.getBaselineStorage(EXISTING));
    }

    @Test
    void shouldThrowAnExceptionForNotExistingBaselineStorage()
    {
        mockContext();
        baselineStorageProvider.init();
        var iae =
            assertThrows(IllegalArgumentException.class,
                () -> baselineStorageProvider.getBaselineStorage(NOT_EXISTING));
        assertEquals("Unable to find baseline storage with name: not_existing. Available baseline storages: [existing]",
            iae.getMessage());
    }

    @Test
    void shouldRethrowExceptionIfItsNotExpected()
    {
        when(applicationContext.getBeanNamesForType(BaselineStorage.class)).thenReturn(new String[]{ EXISTING,
                NOT_EXISTING });
        when(applicationContext.getBean(EXISTING, BaselineStorage.class)).thenReturn(baselineStorage);
        var bce = new BeanCreationException(NOT_EXISTING, new IllegalStateException(NOT_EXISTING));
        when(applicationContext.getBean(NOT_EXISTING, BaselineStorage.class)).thenThrow(bce);
        var actual = assertThrows(BeanCreationException.class, baselineStorageProvider::init);
        assertSame(bce, actual);
    }
}
