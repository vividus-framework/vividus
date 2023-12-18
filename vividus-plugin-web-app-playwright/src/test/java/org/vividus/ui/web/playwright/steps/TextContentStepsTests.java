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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class TextContentStepsTests
{
    @Mock private UiContext uiContext;
    @Mock private VariableContext variableContext;
    @Mock private PlaywrightSoftAssert playwrightSoftAssert;
    @InjectMocks private TextContentSteps steps;

    @Test
    void shouldSaveTextOfContext()
    {
        Locator context = mock();
        when(uiContext.getCurrentContexOrPageRoot()).thenReturn(context);
        var text = "context";
        when(context.textContent()).thenReturn(text);
        var scopes = Set.of(VariableScope.STORY);
        var variableName = "textOfContext";
        steps.saveTextOfContext(scopes, variableName);
        verify(variableContext).putVariable(scopes, variableName, text);
    }

    @Test
    void shouldSaveTextOfElement()
    {
        Locator element = mock();
        PlaywrightLocator playwrightLocator = mock();
        when(uiContext.locateElement(playwrightLocator)).thenReturn(element);
        var text = "element";
        when(element.textContent()).thenReturn(text);
        var scopes = Set.of(VariableScope.STORY);
        var variableName = "textOfElement";
        steps.saveTextOfElement(playwrightLocator, scopes, variableName);
        verify(variableContext).putVariable(scopes, variableName, text);
    }

    @Test
    void shouldAssertTextMatchesRegex()
    {
        shouldAssertText("The text matching regular expression is not found in the context",
                (steps, locatorAssertions) -> {
                    var regex = Pattern.compile(".*");
                    steps.assertTextMatchesRegex(regex);
                    verify(locatorAssertions).containsText(regex);
                });
    }

    @Test
    void shouldAssertTextExists()
    {
        shouldAssertText("The expected text is not found in the context",
                (steps, locatorAssertions) -> {
                    var text = "text";
                    steps.assertTextExists(text);
                    verify(locatorAssertions).containsText(text);
                });
    }

    @Test
    void shouldAssertTextDoesNotExist()
    {
        shouldAssertText("The unexpected text is found in the context",
                (steps, locatorAssertions) -> {
                    var unexpectedText = "error";
                    steps.assertTextDoesNotExist(unexpectedText);
                    var ordered = inOrder(locatorAssertions);
                    ordered.verify(locatorAssertions).not();
                    ordered.verify(locatorAssertions).containsText(unexpectedText);
                    ordered.verifyNoMoreInteractions();
                });
    }

    private void shouldAssertText(String messageOnFailure, BiConsumer<TextContentSteps, LocatorAssertions> test)
    {
        Locator context = mock();
        when(uiContext.getCurrentContexOrPageRoot()).thenReturn(context);
        try (var playwrightAssertionsStaticMock = mockStatic(PlaywrightAssertions.class))
        {
            var locatorAssertions = mock(LocatorAssertions.class, RETURNS_SELF);
            playwrightAssertionsStaticMock.when(() -> PlaywrightAssertions.assertThat(context)).thenReturn(
                    locatorAssertions);
            doNothing().when(playwrightSoftAssert).runAssertion(eq(messageOnFailure), argThat(runnable -> {
                runnable.run();
                return true;
            }));
            test.accept(steps, locatorAssertions);
        }
    }
}
