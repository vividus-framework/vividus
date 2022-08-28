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

package org.vividus.mobileapp.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.ContextAware;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IGenericWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class SetContextStepsTests
{
    private static final String WEB_VIEW_MAIN = "WEBVIEW_MAIN";
    private static final String WEB_VIEWS_FOUND = "Web views found: {}";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private ISoftAssert softAssert;
    @Mock private ContextAware contextAware;
    @InjectMocks private SetContextSteps setContextSteps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SetContextSteps.class);

    @Test
    void shouldSwitchToNativeContext()
    {
        when(webDriverProvider.getUnwrapped(ContextAware.class)).thenReturn(contextAware);

        setContextSteps.switchToNativeContext();

        verify(contextAware).context(IGenericWebDriverManager.NATIVE_APP_CONTEXT);
        verifyNoMoreInteractions(contextAware, webDriverProvider);
        verifyNoInteractions(softAssert);
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldSwitchToWebViewByName()
    {
        when(webDriverProvider.getUnwrapped(ContextAware.class)).thenReturn(contextAware);
        when(contextAware.getContextHandles())
                .thenReturn(Set.of(IGenericWebDriverManager.NATIVE_APP_CONTEXT, WEB_VIEW_MAIN));

        setContextSteps.switchToWebViewByName(StringComparisonRule.IS_EQUAL_TO, WEB_VIEW_MAIN);

        verify(contextAware).context(WEB_VIEW_MAIN);
        verifyNoMoreInteractions(softAssert, contextAware, webDriverProvider);
        assertThat(logger.getLoggingEvents(), is(List.of(
                info(WEB_VIEWS_FOUND, Set.of(WEB_VIEW_MAIN).toString()),
                info("Switching to web view with the name '{}'", WEB_VIEW_MAIN))));
    }

    @Test
    void shouldFailToSwitchToWebViewWhenNoWebViewIsAvailable()
    {
        when(webDriverProvider.getUnwrapped(ContextAware.class)).thenReturn(contextAware);
        when(contextAware.getContextHandles()).thenReturn(Set.of(IGenericWebDriverManager.NATIVE_APP_CONTEXT));

        setContextSteps.switchToWebViewByName(StringComparisonRule.IS_EQUAL_TO, WEB_VIEW_MAIN);

        verify(softAssert).recordFailedAssertion("No web views found");
        verifyNoMoreInteractions(softAssert, contextAware, webDriverProvider);
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldFailSwitchingToWebViewIfNoWebViewMatchesTheRule()
    {
        when(webDriverProvider.getUnwrapped(ContextAware.class)).thenReturn(contextAware);
        when(contextAware.getContextHandles())
                .thenReturn(Set.of(IGenericWebDriverManager.NATIVE_APP_CONTEXT, WEB_VIEW_MAIN));

        setContextSteps.switchToWebViewByName(StringComparisonRule.DOES_NOT_CONTAIN, WEB_VIEW_MAIN);

        verify(softAssert).recordFailedAssertion("The number of web views with name that does not contain"
                + " 'WEBVIEW_MAIN' is expected to be 1, but got 0");
        verifyNoMoreInteractions(softAssert, contextAware, webDriverProvider);
        assertThat(logger.getLoggingEvents(), is(List.of(
                info(WEB_VIEWS_FOUND, Set.of(WEB_VIEW_MAIN).toString()))));
    }
}
