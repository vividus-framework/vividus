/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.ui.web.playwright.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import org.apache.commons.lang3.function.TriConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.AssertionFailedError;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class SetContextStepsTests
{
    @Mock private UiContext uiContext;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private SetContextSteps setContextSteps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SetContextSteps.class);

    @Test
    void shouldResetContext()
    {
        setContextSteps.resetContext();
        verify(uiContext).reset();
    }

    @Test
    void shouldChangeContextSuccessfully()
    {
        testSuccessfulContextChange(setContextSteps::changeContext, ordered -> ordered.verify(uiContext).reset());
    }

    @Test
    void shouldFailToChangeContext()
    {
        testFailureToContextChange(setContextSteps::changeContext, ordered -> ordered.verify(uiContext).reset());
    }

    @Test
    void shouldChangeContextInScopeOfCurrentContextSuccessfully()
    {
        testSuccessfulContextChange(setContextSteps::changeContextInScopeOfCurrentContext, ordered -> { });
    }

    @Test
    void shouldFailToChangeContextInScopeOfCurrentContext()
    {
        testFailureToContextChange(setContextSteps::changeContextInScopeOfCurrentContext, ordered -> { });
    }

    private void testSuccessfulContextChange(Consumer<PlaywrightLocator> test, Consumer<InOrder> orderedVerification)
    {
        testContextChange((locator, context, locatorAssertions) ->
        {
            test.accept(locator);
            var ordered = inOrder(uiContext, locatorAssertions);
            orderedVerification.accept(ordered);
            ordered.verify(locatorAssertions).hasCount(1);
            ordered.verify(uiContext).setContext(context);
            ordered.verifyNoMoreInteractions();
            assertThat(logger.getLoggingEvents(), is(List.of(info("The context is successfully changed"))));
        });
    }

    private void testFailureToContextChange(Consumer<PlaywrightLocator> test, Consumer<InOrder> orderedVerification)
    {
        testContextChange((locator, context, locatorAssertions) ->
        {
            var error = new AssertionFailedError("Locator expected to have count: 1\nReceived: 8");
            doThrow(error).when(locatorAssertions).hasCount(1);
            test.accept(locator);
            var ordered = inOrder(uiContext, locatorAssertions, softAssert);
            orderedVerification.accept(ordered);
            ordered.verify(locatorAssertions).hasCount(1);
            ordered.verify(softAssert).recordFailedAssertion(
                    "The element to set context is not found. " + error.getMessage(), error);
            ordered.verifyNoMoreInteractions();
            assertThat(logger.getLoggingEvents(), is(empty()));
        });
    }

    private void testContextChange(TriConsumer<PlaywrightLocator, Locator, LocatorAssertions> test)
    {
        var locator = new PlaywrightLocator("xpath", "//a");
        Locator context = mock();
        when(uiContext.locateElement(locator)).thenReturn(context);
        try (var playwrightAssertionsStaticMock = mockStatic(PlaywrightAssertions.class))
        {
            LocatorAssertions locatorAssertions = mock();
            playwrightAssertionsStaticMock.when(() -> PlaywrightAssertions.assertThat(context)).thenReturn(
                    locatorAssertions);
            test.accept(locator, context, locatorAssertions);
        }
    }
}
