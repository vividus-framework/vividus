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

package org.vividus.mobileapp.converter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.vividus.ui.screenshot.ScreenshotParameters;

class ExamplesTableToScreenshotConfigurationConverterTests
{
    @Test
    void shouldConvertExamplesTableToScreenshotConfiguration()
    {
        var table = new ExamplesTable("|cutTop|\n|101|");
        var screenshotConfiguration = new ExamplesTableToScreenshotConfigurationConverter()
                .convertValue(table, ScreenshotParameters.class);
        assertAll(
                () -> assertEquals(Optional.empty(), screenshotConfiguration.getShootingStrategy()),
                () -> assertEquals(101, screenshotConfiguration.getCutTop())
        );
    }
}
