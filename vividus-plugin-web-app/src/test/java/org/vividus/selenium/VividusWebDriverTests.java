/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

class VividusWebDriverTests
{
    private VividusWebDriver vividusWebDriver;

    @BeforeEach
    void beforeEach()
    {
        vividusWebDriver = new VividusWebDriver();
    }

    @Test
    void testGetSetWebDriver()
    {
        WebDriver driver = mock(WebDriver.class);
        vividusWebDriver.setWebDriver(driver);
        assertEquals(driver, vividusWebDriver.getWrappedDriver());
    }

    @Test
    void testGetSetIsRemote()
    {
        vividusWebDriver.setRemote(true);
        assertTrue(vividusWebDriver.isRemote());
    }

    @Test
    void testGetSetDesiredCapabilities()
    {
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        vividusWebDriver.setDesiredCapabilities(desiredCapabilities);
        assertEquals(desiredCapabilities, vividusWebDriver.getDesiredCapabilities());
    }
}
