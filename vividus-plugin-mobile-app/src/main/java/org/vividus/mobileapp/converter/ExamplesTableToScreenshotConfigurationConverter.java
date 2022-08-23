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

import java.lang.reflect.Type;

import javax.inject.Named;

import org.jbehave.core.model.ExamplesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.converter.ui.AbstractExamplesTableToScreenshotConfigurationConverter;
import org.vividus.ui.screenshot.ScreenshotConfiguration;

@Named
public class ExamplesTableToScreenshotConfigurationConverter
        extends AbstractExamplesTableToScreenshotConfigurationConverter<ScreenshotConfiguration>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExamplesTableToScreenshotConfigurationConverter.class);

    protected ExamplesTableToScreenshotConfigurationConverter()
    {
        super(ScreenshotConfiguration.class);
    }

    @Override
    public ScreenshotConfiguration convertValue(ExamplesTable value, Type type)
    {
        ScreenshotConfiguration screenshotConfiguration = super.convertValue(value, type);
        if (screenshotConfiguration.getNativeFooterToCut() != 0)
        {
            LOGGER.atWarn().log("Screenshot configuration `nativeFooterToCut` is deprecated, use `cutBottom` instead.");
            screenshotConfiguration.setCutBottom(screenshotConfiguration.getNativeFooterToCut());
        }
        return screenshotConfiguration;
    }
}
