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

package org.vividus.bdd.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith(MockitoExtension.class)
class CurrentPageUrlKnownIssueDataProviderTests
{
    @Mock
    private IWebDriverProvider webDriverProvider;

    @InjectMocks
    private CurrentPageUrlKnownIssueDataProvider currentPageUrlDataProvider;

    @Test
    void testGetData()
    {
        String url = "http://examples.com";
        WebDriver driver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(url);

        assertEquals(Optional.of(url), currentPageUrlDataProvider.getData());
    }

    @Test
    void testGetDataException()
    {
        WebDriver driver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenThrow(new WebDriverException());

        assertEquals(Optional.empty(), currentPageUrlDataProvider.getData());
    }
}
