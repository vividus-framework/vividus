/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.steps.ui;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IGenericWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.SubSteps;

import io.appium.java_client.remote.SupportsContextSwitching;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class MobileNativeContextStepsTests
{
    private static final String WEB_VIEW_MAIN = "WEBVIEW_MAIN";
    private static final String WEB_VIEWS_FOUND = "Web views found: {}";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IGenericWebDriverManager webDriverManager;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private MobileNativeContextSteps steps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(MobileNativeContextSteps.class);

    @Test
    void shouldSwitchToNativeContext()
    {
        SupportsContextSwitching contextSwitchingDriver = mock();
        when(webDriverProvider.getUnwrapped(SupportsContextSwitching.class)).thenReturn(contextSwitchingDriver);

        steps.switchToNativeContext();

        verify(contextSwitchingDriver).context(IGenericWebDriverManager.NATIVE_APP_CONTEXT);
        verifyNoMoreInteractions(contextSwitchingDriver, webDriverProvider);
        verifyNoInteractions(softAssert);
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldSwitchToWebViewByName()
    {
        SupportsContextSwitching contextSwitchingDriver = mock();
        when(webDriverProvider.getUnwrapped(SupportsContextSwitching.class)).thenReturn(contextSwitchingDriver);
        when(contextSwitchingDriver.getContextHandles())
                .thenReturn(Set.of(IGenericWebDriverManager.NATIVE_APP_CONTEXT, WEB_VIEW_MAIN));

        steps.switchToWebViewByName(StringComparisonRule.IS_EQUAL_TO, WEB_VIEW_MAIN);

        verify(contextSwitchingDriver).context(WEB_VIEW_MAIN);
        verifyNoMoreInteractions(softAssert, contextSwitchingDriver, webDriverProvider);
        assertThat(logger.getLoggingEvents(), is(List.of(
                info(WEB_VIEWS_FOUND, Set.of(WEB_VIEW_MAIN).toString()),
                info("Switching to web view with the name '{}'", WEB_VIEW_MAIN))));
    }

    @Test
    void shouldFailToSwitchToWebViewWhenNoWebViewIsAvailable()
    {
        SupportsContextSwitching contextSwitchingDriver = mock();
        when(webDriverProvider.getUnwrapped(SupportsContextSwitching.class)).thenReturn(contextSwitchingDriver);
        when(contextSwitchingDriver.getContextHandles()).thenReturn(
                Set.of(IGenericWebDriverManager.NATIVE_APP_CONTEXT));

        steps.switchToWebViewByName(StringComparisonRule.IS_EQUAL_TO, WEB_VIEW_MAIN);

        verify(softAssert).recordFailedAssertion("No web views found");
        verifyNoMoreInteractions(softAssert, contextSwitchingDriver, webDriverProvider);
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldFailSwitchingToWebViewIfNoWebViewMatchesTheRule()
    {
        SupportsContextSwitching contextSwitchingDriver = mock();
        when(webDriverProvider.getUnwrapped(SupportsContextSwitching.class)).thenReturn(contextSwitchingDriver);
        when(contextSwitchingDriver.getContextHandles())
                .thenReturn(Set.of(IGenericWebDriverManager.NATIVE_APP_CONTEXT, WEB_VIEW_MAIN));

        steps.switchToWebViewByName(StringComparisonRule.DOES_NOT_CONTAIN, WEB_VIEW_MAIN);

        verify(softAssert).recordFailedAssertion("The number of web views with name that does not contain"
                + " 'WEBVIEW_MAIN' is expected to be 1, but got 0");
        verifyNoMoreInteractions(softAssert, contextSwitchingDriver, webDriverProvider);
        assertThat(logger.getLoggingEvents(), is(List.of(
                info(WEB_VIEWS_FOUND, Set.of(WEB_VIEW_MAIN).toString()))));
    }

    @Test
    void shouldExecuteStepsInMobileNativeContext()
    {
        doNothing().when(webDriverManager).performActionInNativeContext(argThat(c -> {
            c.accept(null);
            return true;
        }));
        SubSteps subSteps = mock();
        steps.executeStepsInNativeContext(subSteps);

        verify(subSteps).execute(Optional.empty());
    }
}
