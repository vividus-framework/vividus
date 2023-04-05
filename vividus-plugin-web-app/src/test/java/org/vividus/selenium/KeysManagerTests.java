/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.selenium;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Keys;
import org.vividus.selenium.manager.IWebDriverManager;

@ExtendWith(MockitoExtension.class)
class KeysManagerTests
{
    @Mock private IWebDriverManager webDriverManager;
    @InjectMocks private KeysManager keysManager;

    @Test
    void shouldConvertKeysToCharSequenceArray()
    {
        var test = "test";
        var actual = keysManager.convertToKeys(List.of("ENTER", test));
        assertArrayEquals(actual, new CharSequence[] { Keys.ENTER, test });
    }

    @ParameterizedTest
    @CsvSource({
            "true,  COMMAND",
            "false, CONTROL"
    })
    void shouldConvertKeysToCharSequenceArray(boolean macOs, Keys controlKey)
    {
        var key = "C";
        when(webDriverManager.isMacOs()).thenReturn(macOs);
        var actual = keysManager.convertToKeys(List.of("OS_INDEPENDENT_CONTROL", key));
        assertArrayEquals(actual, new CharSequence[] { controlKey, key });
    }

    @Test
    void shouldConvertToKeyWithValidation()
    {
        var key = keysManager.convertToKey(true, "ALT");
        assertEquals(Keys.ALT, key);
    }

    @Test
    void shouldFailToConvertToKeyWithValidation()
    {
        var exception = assertThrows(IllegalArgumentException.class, () -> keysManager.convertToKey(true, "str"));
        assertEquals("The 'str' is not allowed as a key", exception.getMessage());
    }
}
