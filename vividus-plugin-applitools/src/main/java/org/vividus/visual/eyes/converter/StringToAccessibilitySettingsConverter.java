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

import java.lang.reflect.Type;

import com.applitools.eyes.AccessibilityGuidelinesVersion;
import com.applitools.eyes.AccessibilityLevel;
import com.applitools.eyes.AccessibilitySettings;

import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.vividus.converter.FluentTrimmedEnumConverter;

public class StringToAccessibilitySettingsConverter extends AbstractParameterConverter<String, AccessibilitySettings>
{
    private final FluentTrimmedEnumConverter fluentTrimmedEnumConverter;

    public StringToAccessibilitySettingsConverter(FluentTrimmedEnumConverter fluentTrimmedEnumConverter)
    {
        this.fluentTrimmedEnumConverter = fluentTrimmedEnumConverter;
    }

    @Override
    public AccessibilitySettings convertValue(String value, Type type)
    {
        String [] parts = value.split("-");
        if (parts.length != 2)
        {
            throw new IllegalArgumentException(
                    "Expected accessibility settings format is '<standard> - <level>', but got: " + value);
        }

        AccessibilityGuidelinesVersion standard = (AccessibilityGuidelinesVersion) fluentTrimmedEnumConverter
                .convertValue(parts[0], AccessibilityGuidelinesVersion.class);
        AccessibilityLevel level = (AccessibilityLevel) fluentTrimmedEnumConverter.convertValue(parts[1],
                AccessibilityLevel.class);
        return new AccessibilitySettings(level, standard);
    }
}
