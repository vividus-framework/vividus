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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.WaitActions;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class SetContextStepsTests
{
    private static final PlaywrightLocator LOCATOR = new PlaywrightLocator("xpath", "div");
    private static final String TITLE_1 = "Title1";
    private static final String TITLE_2 = "Title2";

    @Mock private BrowserContextProvider browserContextProvider;
    @Mock private UiContext uiContext;
    @Mock private WaitActions waitActions;
    @Mock private PlaywrightSoftAssert playwrightSoftAssert;
    @InjectMocks private SetContextSteps steps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SetContextSteps.class);

    @Test
    void shouldResetContext()
    {
        steps.resetContext();
        verify(uiContext).resetContext();
    }

    @Test
    void shouldChangeContext()
    {
        testSuccessfulContextChange(steps::changeContext, ordered -> ordered.verify(uiContext).resetContext());
    }

    @Test
    void shouldChangeContextInScopeOfCurrentContext()
    {
        testSuccessfulContextChange(steps::changeContextInScopeOfCurrentContext, ordered -> { });
    }

    private void testSuccessfulContextChange(Consumer<PlaywrightLocator> test, Consumer<InOrder> orderedVerification)
    {
        Locator context = mock();
        when(uiContext.locateElement(LOCATOR)).thenReturn(context);
        try (var playwrightAssertionsStaticMock = mockStatic(PlaywrightAssertions.class))
        {
            LocatorAssertions locatorAssertions = mock();
            playwrightAssertionsStaticMock.when(() -> PlaywrightAssertions.assertThat(context)).thenReturn(
                    locatorAssertions);
            doNothing().when(playwrightSoftAssert).runAssertion(eq("The element to set context is not found"),
                    argThat(runnable -> {
                        runnable.run();
                        return true;
                    }));
            test.accept(LOCATOR);
            var ordered = inOrder(uiContext, locatorAssertions);
            orderedVerification.accept(ordered);
            ordered.verify(locatorAssertions).hasCount(1);
            ordered.verify(uiContext).setContext(context);
            ordered.verifyNoMoreInteractions();
            assertThat(logger.getLoggingEvents(), is(List.of(info("The context is successfully changed"))));
        }
    }

    @Test
    void shouldSwitchToFrameFromPage()
    {
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);
        when(uiContext.getCurrentFrame()).thenReturn(null);
        testSwitchToFrame(page::frameLocator);
    }

    @Test
    void shouldSwitchToFrameFromFrame()
    {
        FrameLocator currentFrame = mock();
        when(uiContext.getCurrentFrame()).thenReturn(currentFrame);
        testSwitchToFrame(currentFrame::frameLocator);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldWaitForTabAndSwitch()
    {
        Page page1 = mock();
        Page page2 = mock();
        when(page1.title()).thenReturn(TITLE_1);
        when(page2.title()).thenReturn(TITLE_2);
        BrowserContext browserContext = mock();
        when(browserContextProvider.get()).thenReturn(browserContext);
        when(browserContext.pages()).thenReturn(List.of(page1, page2));

        doNothing().when(waitActions).runWithTimeoutAssertion(
                (Supplier<String>) argThat(
                        s -> {
                            String value = ((Supplier<String>) s).get();
                            return "switch to the tab where title contains \"Title2\"".equals(value);
                        }),
                argThat(runnable ->
                {
                    runnable.run();
                    return true;
                }));

        Duration duration = Duration.ofSeconds(5);
        steps.waitForTabAndSwitch(duration, StringComparisonRule.CONTAINS, TITLE_2);

        ArgumentCaptor<BooleanSupplier> conditionCaptor = ArgumentCaptor.forClass(BooleanSupplier.class);
        verify(browserContext).waitForCondition(conditionCaptor.capture(),
                argThat(option -> option.timeout == duration.toMillis()));
        BooleanSupplier condition = conditionCaptor.getValue();
        assertTrue(condition.getAsBoolean());
        verify(uiContext).reset();
        verify(uiContext).setCurrentPage(page2);
        verifyNoMoreInteractions(uiContext);
        verify(page2).bringToFront();
        verify(page1).title();
        verifyNoMoreInteractions(page1);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldWaitForTabAndNotSwitchIfMatchedTitleNotFound()
    {
        Page page = mock();
        when(page.title()).thenReturn(TITLE_1);
        BrowserContext browserContext = mock();
        when(browserContextProvider.get()).thenReturn(browserContext);
        when(browserContext.pages()).thenReturn(List.of(page));

        doNothing().when(waitActions).runWithTimeoutAssertion(
                (Supplier<String>) argThat(s -> {
                    String value = ((Supplier<String>) s).get();
                    return String.format("switch to the tab where title contains \"%s\"", TITLE_2)
                            .equals(value);
                }),
                argThat(runnable ->
                {
                    runnable.run();
                    return true;
                }));

        Duration duration = Duration.ofSeconds(5);
        steps.waitForTabAndSwitch(duration, StringComparisonRule.CONTAINS, TITLE_2);

        ArgumentCaptor<BooleanSupplier> conditionCaptor = ArgumentCaptor.forClass(BooleanSupplier.class);
        verify(browserContext).waitForCondition(conditionCaptor.capture(),
                argThat(option -> option.timeout == duration.toMillis()));
        BooleanSupplier condition = conditionCaptor.getValue();
        assertFalse(condition.getAsBoolean());
        verifyNoInteractions(uiContext);
        verify(page).title();
        verifyNoMoreInteractions(page);
    }

    private void testSwitchToFrame(Function<String, FrameLocator> frameLocatorProvider)
    {
        FrameLocator frameLocator = mock();
        Locator rootLocator = mock();
        when(frameLocatorProvider.apply(LOCATOR.getLocator())).thenReturn(frameLocator);
        when(frameLocator.locator(":root")).thenReturn(rootLocator);
        try (var playwrightAssertionsStaticMock = mockStatic(PlaywrightAssertions.class))
        {
            LocatorAssertions locatorAssertions = mock();
            playwrightAssertionsStaticMock.when(() -> PlaywrightAssertions.assertThat(rootLocator)).thenReturn(
                    locatorAssertions);
            doNothing().when(playwrightSoftAssert).runAssertion(eq("The frame to switch is not found"),
                    argThat(runnable -> {
                        runnable.run();
                        return true;
                    }));
            steps.switchToFrame(LOCATOR);
            var ordered = inOrder(uiContext, locatorAssertions);
            ordered.verify(uiContext).resetContext();
            ordered.verify(locatorAssertions).hasCount(1);
            ordered.verify(uiContext).setCurrentFrame(frameLocator);
            ordered.verifyNoMoreInteractions();
            assertThat(logger.getLoggingEvents(), is(List.of(info("Successfully switched to frame"))));
        }
    }

    @Test
    void shouldSwitchBackToPage()
    {
        steps.switchingToDefault();
        verify(uiContext).reset();
    }
}
