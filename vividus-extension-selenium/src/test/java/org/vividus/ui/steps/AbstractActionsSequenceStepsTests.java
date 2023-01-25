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

package org.vividus.ui.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Interactive;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.testdouble.TestLocatorType;
import org.vividus.testdouble.TestSequenceActionType;
import org.vividus.ui.action.SequenceAction;
import org.vividus.ui.action.search.Locator;

@ExtendWith(MockitoExtension.class)
class AbstractActionsSequenceStepsTests
{
    private static final Locator LOCATOR = new Locator(TestLocatorType.SEARCH, "id");
    private static final String ELEMENT_EXISTS_MESSAGE = "Element for interaction";
    private static final String TEXT = "text";

    @Mock private WebDriver webDriver;
    @Mock private IBaseValidations baseValidations;
    @Mock private Actions actionsBuilder;
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private TestActionsSequenceSteps sequenceActionSteps;

    @BeforeEach
    void beforeEach()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
    }

    @Test
    void shouldPerformSequenceOfActions()
    {
        var actions = List.of(
                new SequenceAction<>(TestSequenceActionType.CLICK, LOCATOR),
                new SequenceAction<>(TestSequenceActionType.ENTER_TEXT, TEXT),
                new SequenceAction<>(TestSequenceActionType.DOUBLE_CLICK, LOCATOR),
                new SequenceAction<>(TestSequenceActionType.RELEASE, null));
        var webElement = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_EXISTS_MESSAGE, LOCATOR)).thenReturn(Optional.of(webElement));
        sequenceActionSteps.executeSequenceOfActions(actionsBuilder, webDriver, actions);
        InOrder ordered = inOrder(actionsBuilder);
        ordered.verify(actionsBuilder).click(webElement);
        ordered.verify(actionsBuilder).sendKeys(TEXT);
        ordered.verify(actionsBuilder).doubleClick(webElement);
        ordered.verify(actionsBuilder).release();
        ordered.verify(actionsBuilder).perform();
        verifyNoMoreInteractions(actionsBuilder);
    }

    @Test
    void shouldNotPerformActionsIfElementIsNotExist()
    {
        var webDriver = mock(WebDriver.class, withSettings().extraInterfaces(Interactive.class));

        when(webDriverProvider.get()).thenReturn(webDriver);
        when(baseValidations.assertElementExists(ELEMENT_EXISTS_MESSAGE, LOCATOR)).thenReturn(Optional.empty());

        var actions = List.of(new SequenceAction<>(TestSequenceActionType.CLICK, LOCATOR));
        sequenceActionSteps.executeSequenceOfActions(actionsBuilder, webDriver, actions);
        verifyNoMoreInteractions(actionsBuilder);
    }

    private static final class TestActionsSequenceSteps extends AbstractActionsSequenceSteps
    {
        private TestActionsSequenceSteps(IWebDriverProvider webDriverProvider, IBaseValidations baseValidations)
        {
            super(webDriverProvider, baseValidations);
        }

        public void executeSequenceOfActions(Actions actionBuilder, WebDriver webDriver,
                List<SequenceAction<TestSequenceActionType>> actions)
        {
            execute(wd -> {
                assertEquals(wd, webDriver);
                return actionBuilder;
            }, actions);
        }
    }
}
