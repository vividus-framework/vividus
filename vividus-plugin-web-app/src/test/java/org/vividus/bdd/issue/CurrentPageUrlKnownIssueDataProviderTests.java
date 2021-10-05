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

package org.vividus.bdd.issue;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class CurrentPageUrlKnownIssueDataProviderTests
{
    private static final String DEPRECATION_WARNING =
            "'dynamicPatterns' field is deprecated in known issues and will be removed in VIVIDUS 0.4.0, use "
                    + "'variablePatterns' instead. The dynamic pattern 'currentPageUrl' must be replaced with "
                    + "variable pattern 'currentPageUrl'";

    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private CurrentPageUrlKnownIssueDataProvider currentPageUrlDataProvider;

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(CurrentPageUrlKnownIssueDataProvider.class);

    @Test
    void testGetData()
    {
        String url = "http://examples.com";
        WebDriver driver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(url);

        assertEquals(Optional.of(url), currentPageUrlDataProvider.getData());

        assertThat(testLogger.getLoggingEvents(), equalTo(List.of(warn(DEPRECATION_WARNING))));
    }

    @Test
    void testGetDataException()
    {
        WebDriver driver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenThrow(new WebDriverException());

        assertEquals(Optional.empty(), currentPageUrlDataProvider.getData());

        assertThat(testLogger.getLoggingEvents(), equalTo(List.of(warn(DEPRECATION_WARNING))));
    }
}
