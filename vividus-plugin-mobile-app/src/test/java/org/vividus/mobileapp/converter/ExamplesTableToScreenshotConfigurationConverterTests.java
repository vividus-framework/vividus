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

package org.vividus.mobileapp.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vividus.selenium.screenshot.ScreenshotConfiguration;

class ExamplesTableToScreenshotConfigurationConverterTests
{
    @Test
    void shouldConvertExamplesTableToScreenshotConfiguraiton()
    {
        ExamplesTable table = new ExamplesTable("|nativeFooterToCut|\n|101|");
        ScreenshotConfiguration configuration = new ExamplesTableToScreenshotConfigurationConverter()
                .convertValue(table, ScreenshotConfiguration.class);
        Assertions.assertAll(() -> assertEquals(Optional.empty(), configuration.getShootingStrategy()),
                             () -> assertEquals(101, configuration.getNativeFooterToCut()));
    }
}
