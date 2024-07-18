/*
 * Copyright 2019-2024 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.WaitActions;
import org.vividus.ui.web.playwright.assertions.PlaywrightLocatorAssertions;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.ui.web.playwright.locator.Visibility;

@ExtendWith(MockitoExtension.class)
class WaitStepsTests
{
    @Mock private UiContext uiContext;
    @Mock private BrowserContextProvider browserContextProvider;
    @Mock private WaitActions waitActions;
    @Mock private Locator locator;
    @Mock private BrowserContext context;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private WaitSteps waitSteps;

    private final PlaywrightLocator playwrightLocator = new PlaywrightLocator("css", "div");

    private static Stream<Arguments> visibilityTestProvider()
    {
        return Stream.of(
                Arguments.of((BiConsumer<WaitSteps, PlaywrightLocator>) WaitSteps::waitForElementAppearance),
                Arguments.of((BiConsumer<WaitSteps, PlaywrightLocator>) WaitSteps::waitForElementDisappearance)
                        );
    }

    @Test
    void shouldWaitForElementAppearance()
    {
        shouldWaitForElementState(ElementState.VISIBLE, steps -> steps.waitForElementAppearance(playwrightLocator),
                () -> PlaywrightLocatorAssertions.assertElementVisible(locator, true));
    }

    @Test
    void shouldWaitForElementDisappearance()
    {
        shouldWaitForElementState(ElementState.NOT_VISIBLE,
                steps -> steps.waitForElementDisappearance(playwrightLocator),
                () -> PlaywrightLocatorAssertions.assertElementHidden(locator, true));
    }

    @Test
    void shouldWaitForElementState()
    {
        shouldWaitForElementState(ElementState.ENABLED,
                steps -> steps.waitForElementState(playwrightLocator, ElementState.ENABLED),
                () -> PlaywrightLocatorAssertions.assertElementEnabled(locator, true));
    }

    @ParameterizedTest
    @MethodSource("visibilityTestProvider")
    void shouldThrowExceptionWhenLocatorVisibilityIsNotVisible(BiConsumer<WaitSteps, PlaywrightLocator> test)
    {
        var locator = new PlaywrightLocator("id", "value");
        locator.setVisibility(Visibility.ALL);
        var exception = assertThrows(IllegalArgumentException.class, () -> test.accept(waitSteps, locator));
        var expectedExceptionMessage = String.format(
                "The step supports locators with VISIBLE visibility settings only, but the locator is `%s`", locator);
        assertEquals(expectedExceptionMessage, exception.getMessage());
    }

    @Test
    void shouldWaitForNumberOfElements()
    {
        int numberOfElements = 5;
        when(browserContextProvider.get()).thenReturn(context);
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.count()).thenReturn(numberOfElements);
        doNothing().when(waitActions).runWithTimeoutAssertion(
                eq("number of elements located by 'css(div) with visibility: visible' to be equal to 5"),
                argThat(runnable ->
                {
                    runnable.run();
                    return true;
                }));

        waitSteps.waitForElementNumber(playwrightLocator, ComparisonRule.EQUAL_TO, numberOfElements);
        ArgumentCaptor<BooleanSupplier> conditionCaptor = ArgumentCaptor.forClass(BooleanSupplier.class);
        verify(context).waitForCondition(conditionCaptor.capture());

        BooleanSupplier condition = conditionCaptor.getValue();
        assertTrue(condition.getAsBoolean());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldWaitUntilPageTitleIs()
    {
        String title = "Title";
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);
        when(page.title()).thenReturn(title);
        doNothing().when(waitActions)
                .runWithTimeoutAssertion((Supplier<String>) argThat(
                        s -> {
                            String value = ((Supplier<String>) s).get();
                            return "current title contains \"Title\". Current title: \"Title\"".equals(value);
                        }),
                        argThat(runnable ->
                        {
                            runnable.run();
                            return true;
                        }));

        waitSteps.waitUntilPageTitleIs(StringComparisonRule.CONTAINS, title);

        ArgumentCaptor<BooleanSupplier> conditionCaptor = ArgumentCaptor.forClass(BooleanSupplier.class);
        verify(page).waitForCondition(conditionCaptor.capture());
        BooleanSupplier condition = conditionCaptor.getValue();
        assertTrue(condition.getAsBoolean());
    }

    @ParameterizedTest
    @CsvSource({ "false,true", "false, false" })
    void shouldWaitDurationWithPollingTillElementState(boolean initialStateResult, boolean finalStateResult)
    {
        var duration = Duration.ofSeconds(1);
        var pollingDuration = Duration.ofMillis(100);
        var state = ElementState.VISIBLE;

        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.isVisible()).thenReturn(initialStateResult, finalStateResult);

        waitSteps.waitDurationWithPollingTillElementState(duration, pollingDuration, playwrightLocator, state);
        var assertionDescription = String.format("The element located by `%s` has become VISIBLE", playwrightLocator);
        verify(softAssert).assertTrue(eq(assertionDescription),
                eq(finalStateResult));
    }

    private void shouldWaitForElementState(ElementState state, Consumer<WaitSteps> test,
            MockedStatic.Verification verification)
    {
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        try (var playwrightLocatorAssertions = mockStatic(PlaywrightLocatorAssertions.class))
        {
            test.accept(waitSteps);
            var conditionDescription = String.format("element located by `%s` to be %s", playwrightLocator,
                   state);
            var timeoutOperationCaptor = ArgumentCaptor.forClass(Runnable.class);
            verify(waitActions).runTimeoutPlaywrightAssertion(
                    argThat((Supplier<String> s) -> s.get().equals(conditionDescription)),
                    timeoutOperationCaptor.capture());

            timeoutOperationCaptor.getValue().run();
            playwrightLocatorAssertions.verify(verification);
        }
    }
}
