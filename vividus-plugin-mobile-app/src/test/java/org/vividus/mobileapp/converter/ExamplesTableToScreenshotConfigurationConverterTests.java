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

package org.vividus.mobileapp.converter;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParameters;

@ExtendWith(TestLoggerFactoryExtension.class)
class ExamplesTableToScreenshotConfigurationConverterTests
{
    private static final TestLogger LOGGER
        = TestLoggerFactory.getTestLogger(ExamplesTableToScreenshotConfigurationConverter.class);

    @Test
    void shouldConvertExamplesTableToScreenshotConfiguration()
    {
        ExamplesTable table = new ExamplesTable("|cutTop|\n|101|");
        ScreenshotConfiguration screenshotConfiguration = new ExamplesTableToScreenshotConfigurationConverter()
                .convertValue(table, ScreenshotParameters.class);
        Assertions.assertAll(() -> assertEquals(Optional.empty(), screenshotConfiguration.getShootingStrategy()),
                             () -> assertEquals(101, screenshotConfiguration.getCutTop()));
    }

    @Test
    void shouldNotifyAboutDeprecatedFieldAndReuseValueInNonReplacement()
    {
        ExamplesTable table = new ExamplesTable("|nativeFooterToCut|\n|101|");
        ScreenshotConfiguration screenshotConfiguration = new ExamplesTableToScreenshotConfigurationConverter()
                .convertValue(table, ScreenshotParameters.class);
        Assertions.assertAll(() -> assertEquals(Optional.empty(), screenshotConfiguration.getShootingStrategy()),
                             () -> assertEquals(101, screenshotConfiguration.getCutBottom()),
                () -> assertEquals(List.of(
                    warn("Screenshot configuration `nativeFooterToCut` is deprecated, use `cutBottom` instead.")),
                        LOGGER.getLoggingEvents()));
    }
}
