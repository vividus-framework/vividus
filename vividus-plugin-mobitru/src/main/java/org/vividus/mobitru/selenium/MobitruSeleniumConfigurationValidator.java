/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.mobitru.selenium;

import static org.apache.commons.lang3.Validate.isTrue;

import java.net.URL;

import org.vividus.selenium.SeleniumConfigurationValidator;

public class MobitruSeleniumConfigurationValidator implements SeleniumConfigurationValidator
{
    private static final String INCORRECT_PROPERTY_FOR_MOBITRU_MESSAGE =
            "Incorrect 'selenium.grid.%s' property value for Mobitru: %s";

    private final String seleniumGridHost;
    private final URL seleniumGridUrl;

    public MobitruSeleniumConfigurationValidator(String seleniumGridHost, URL seleniumGridUrl)
    {
        this.seleniumGridHost = seleniumGridHost;
        this.seleniumGridUrl = seleniumGridUrl;
    }

    @Override
    public void validate()
    {
        isTrue(seleniumGridHost != null && seleniumGridHost.endsWith("mobitru.com"),
                INCORRECT_PROPERTY_FOR_MOBITRU_MESSAGE, "host", seleniumGridHost);
        isTrue(seleniumGridUrl != null && seleniumGridHost.equals(seleniumGridUrl.getHost()),
                INCORRECT_PROPERTY_FOR_MOBITRU_MESSAGE, "url", "The host specified in the URL property"
                        + " must be the same as the value of the 'selenium.grid.host' property");
    }
}
