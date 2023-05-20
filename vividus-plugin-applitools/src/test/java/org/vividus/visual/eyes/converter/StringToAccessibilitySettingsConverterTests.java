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

package org.vividus.visual.eyes.converter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.applitools.eyes.AccessibilityGuidelinesVersion;
import com.applitools.eyes.AccessibilityLevel;
import com.applitools.eyes.AccessibilitySettings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.vividus.converter.FluentTrimmedEnumConverter;

class StringToAccessibilitySettingsConverterTests
{
    private final StringToAccessibilitySettingsConverter converter = new StringToAccessibilitySettingsConverter(
            new FluentTrimmedEnumConverter());

    @ParameterizedTest
    @ValueSource(strings = {
        "WCAG_2_1 - AA",
        "WCAG 2_1 - AA",
        "WCAG 2.1 - AA",
        "WCAG 2.1   -   AA"
    })
    void shouldConvert(String input)
    {
        AccessibilitySettings settings = converter.convertValue(input, null);
        assertAll(
            () -> assertEquals(AccessibilityLevel.AA, settings.getLevel()),
            () -> assertEquals(AccessibilityGuidelinesVersion.WCAG_2_1, settings.getGuidelinesVersion())
        );
    }

    @Test
    void shouldFailOnInvalidInputFormat()
    {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> converter.convertValue("You cannot pass!", null));
        assertEquals("Expected accessibility settings format is '<standard> - <level>', but got: You cannot pass!",
                thrown.getMessage());
    }
}
