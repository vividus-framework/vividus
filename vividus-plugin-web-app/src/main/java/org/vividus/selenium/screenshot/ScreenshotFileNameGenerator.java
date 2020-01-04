/*
 * Copyright 2019-2020 the original author or authors.
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.vividus.selenium.manager.IWebDriverManager;

public class ScreenshotFileNameGenerator implements IScreenshotFileNameGenerator
{
    private static final String DEFAULT_IMAGE_FORMAT = "png";

    private static final ThreadLocal<DateFormat> DATE_FORMAT = ThreadLocal
            .withInitial(() -> new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS", Locale.getDefault()));

    @Inject private IWebDriverManager webDriverManager;

    @Override
    public String generateScreenshotFileName(String screenshotName)
    {
        Dimension windowSize = webDriverManager.getSize();
        StringBuilder screenshotFileName = new StringBuilder()
                .append(DATE_FORMAT.get().format(Calendar.getInstance().getTime()))
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
