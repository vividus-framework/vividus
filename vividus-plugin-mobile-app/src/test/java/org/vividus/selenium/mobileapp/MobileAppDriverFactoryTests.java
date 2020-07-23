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

package org.vividus.selenium.mobileapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.selenium.IRemoteWebDriverFactory;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.IPropertyParser;

@ExtendWith(MockitoExtension.class)
class MobileAppDriverFactoryTests
{
    @Mock private IRemoteWebDriverFactory remoteWebDriverFactory;
    @Mock private IPropertyParser propertyParser;
    @Mock private JsonUtils jsonUtils;
    @InjectMocks private MobileAppDriverFactory factory;

    @Test
    void updateDesiredCapabilities()
    {
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        assertEquals(desiredCapabilities, factory.updateDesiredCapabilities(desiredCapabilities));
        verifyNoInteractions(desiredCapabilities, remoteWebDriverFactory, propertyParser, jsonUtils);
    }

    @Test
    void configureWebDriver()
    {
        WebDriver webDriver = mock(WebDriver.class);
        factory.configureWebDriver(webDriver);
        verifyNoInteractions(webDriver, remoteWebDriverFactory, propertyParser, jsonUtils);
    }
}
