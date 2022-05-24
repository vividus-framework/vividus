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

package org.vividus.converter.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Optional;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vividus.selenium.screenshot.CoordsProviderType;
import org.vividus.ui.web.screenshot.WebScreenshotConfiguration;

class ExamplesTableToWebScreenshotParametersConverterTests
{
    private final ExamplesTableToWebScreenshotConfigurationConverter tableConverter
        = new ExamplesTableToWebScreenshotConfigurationConverter();

    @Test
    void shouldConvertExamplesTableToWebScreenshotConfiguration()
    {
        ExamplesTable table = new ExamplesTable(
                "|nativeHeaderToCut|nativeFooterToCut|webHeaderToCut|webFooterToCut|\n|1|2|3|4|");
        WebScreenshotConfiguration parameters =
                (WebScreenshotConfiguration) tableConverter.convertValue(table, null);
        Assertions.assertAll(() -> assertEquals(Duration.ofMillis(500), parameters.getScrollTimeout()),
                             () -> assertEquals(CoordsProviderType.CEILING, parameters.getCoordsProvider()),
                             () -> assertEquals(Optional.empty(), parameters.getScrollableElement()),
                             () -> assertEquals(1, parameters.getNativeHeaderToCut()),
                             () -> assertEquals(2, parameters.getNativeFooterToCut()),
                             () -> assertEquals(3, parameters.getWebHeaderToCut()),
                             () -> assertEquals(4, parameters.getWebFooterToCut()));
    }
}
