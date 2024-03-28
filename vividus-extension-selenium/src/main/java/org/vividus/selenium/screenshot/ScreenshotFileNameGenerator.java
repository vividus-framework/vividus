/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.selenium.screenshot;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.vividus.selenium.manager.IGenericWebDriverManager;

import jakarta.inject.Inject;

public class ScreenshotFileNameGenerator implements IScreenshotFileNameGenerator
{
    private static final String DEFAULT_IMAGE_FORMAT = "png";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS")
            .withZone(ZoneId.systemDefault());

    @Inject private IGenericWebDriverManager webDriverManager;

    @Override
    public String generateScreenshotFileName(String screenshotName)
    {
        Dimension windowSize = webDriverManager.getSize();
        StringBuilder screenshotFileName = new StringBuilder()
                .append(DATE_FORMAT.format(Instant.now()))
                .append('-')
                .append(screenshotName);
        Capabilities capabilities = webDriverManager.getCapabilities();
        if (capabilities != null)
        {
            screenshotFileName.append('-').append(capabilities.getBrowserName());
        }
        return screenshotFileName.append('-')
            .append(windowSize.getWidth())
            .append('x')
            .append(windowSize.getHeight())
            .append('.')
            .append(DEFAULT_IMAGE_FORMAT)
            .toString();
    }
}
