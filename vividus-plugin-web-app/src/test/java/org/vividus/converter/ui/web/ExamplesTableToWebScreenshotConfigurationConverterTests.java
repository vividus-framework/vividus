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

package org.vividus.converter.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Optional;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vividus.selenium.screenshot.CoordsProviderType;
import org.vividus.selenium.screenshot.WebScreenshotConfiguration;

class ExamplesTableToWebScreenshotConfigurationConverterTests
{
    private final ExamplesTableToWebScreenshotConfigurationConverter tableConverter
        = new ExamplesTableToWebScreenshotConfigurationConverter();

    @Test
    void shouldConvertExamplesTableToWebScreenshotConfiguration()
    {
        ExamplesTable table = new ExamplesTable(
                "|nativeHeaderToCut|nativeFooterToCut|webHeaderToCut|webFooterToCut|\n|1|2|3|4|");
        WebScreenshotConfiguration configuration =
                (WebScreenshotConfiguration) tableConverter.convertValue(table, null);
        Assertions.assertAll(() -> assertEquals(Duration.ofMillis(500), configuration.getScrollTimeout()),
                             () -> assertEquals(CoordsProviderType.CEILING, configuration.getCoordsProvider()),
                             () -> assertEquals(Optional.empty(), configuration.getScrollableElement().get()),
                             () -> assertEquals(1, configuration.getNativeHeaderToCut()),
                             () -> assertEquals(2, configuration.getNativeFooterToCut()),
                             () -> assertEquals(3, configuration.getWebHeaderToCut()),
                             () -> assertEquals(4, configuration.getWebFooterToCut()));
    }
}
