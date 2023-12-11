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

package org.vividus.steps.ui.web;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Interactive;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.locator.Locator;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.steps.ui.web.model.Location;
import org.vividus.ui.web.action.WebJavascriptActions;

@ExtendWith(MockitoExtension.class)
class DragAndDropStepsTests
{
    private static final String DRAGGABLE_ELEMENT = "Draggable element";
    private static final String TARGET_ELEMENT = "Target element";
    private static final String SIMULATE_DRAG_AND_DROP_JS = "simulate-drag-and-drop.js";
    private static final String DRAG_AND_DROP_ACTIONS = "{id=default mouse, "
            + "type=pointer, parameters={pointerType=mouse}, "
            + "actions=["
            + "{duration=100, x=0, y=0, type=pointerMove, origin=Mock for WebElement, hashCode: %s}, "
            + "{button=0, type=pointerDown}, "
            + "{duration=200, x=10, y=0, type=pointerMove, origin=pointer}, "
            + "{duration=200, x=-10, y=0, type=pointerMove, origin=pointer}, "
            + "{duration=200, x=100, y=50, type=pointerMove, origin=pointer}, "
            + "{button=0, type=pointerUp}, "
            + "{duration=1000, type=pause}]}";
    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private WebJavascriptActions javascriptActions;

    @Mock
    private IBaseValidations baseValidations;

    @InjectMocks
    private DragAndDropSteps dragAndDropSteps;

    @Test
    void shouldDragAndDropToTargetAtLocationDeprecated()
    {
        WebElement draggable = mockWebElementWithRect(100, 100, 50, 50);
        WebElement target = mockWebElementWithRect(200, 200, 50, 50);
        WebDriver driver = mock(WebDriver.class, withSettings().extraInterfaces(Interactive.class));
        when(webDriverProvider.get()).thenReturn(driver);
        testDragAndDropToTargetAtLocationDeprecated(draggable, Location.TOP, target);
        String actionsSequence = String.format(
                DRAG_AND_DROP_ACTIONS, draggable.hashCode());
        verify((Interactive) driver).perform(
                argThat(arg -> arg.iterator().next().encode().toString().equals(actionsSequence)));
    }

    private static WebElement mockWebElementWithRect(int x, int y, int height, int width)
    {
        WebElement element = mock(WebElement.class);
        when(element.getRect()).thenReturn(new Rectangle(x, y, height, width));
        return element;
    }

    static Stream<Arguments> getElements()
    {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(null, mock(WebElement.class)),
            Arguments.of(mock(WebElement.class), null)
        );
    }

    @Test
    void shouldDragAndDropToTargetAtLocation()
    {
        WebElement draggable = mockWebElementWithRect(100, 100, 50, 50);
        WebElement target = mockWebElementWithRect(200, 200, 50, 50);
        WebDriver driver = mock(WebDriver.class, withSettings().extraInterfaces(Interactive.class));
        when(webDriverProvider.get()).thenReturn(driver);
        testDragAndDropToTargetAtLocation(draggable, Location.TOP, target);
        String actionsSequence = String.format(
                DRAG_AND_DROP_ACTIONS, draggable.hashCode());
        verify((Interactive) driver).perform(
                argThat(arg -> arg.iterator().next().encode().toString().equals(actionsSequence)));
    }

    @MethodSource("getElements")
    @ParameterizedTest
    void shouldNotDragAndDropWhenAnyElementIsNotFoundDeprecated(WebElement draggable, WebElement target)
    {
        testDragAndDropToTargetAtLocationDeprecated(draggable, Location.TOP, target);
        verifyNoInteractions(webDriverProvider);
    }

    private void testDragAndDropToTargetAtLocationDeprecated(WebElement draggable, Location location, WebElement target)
    {
        Locator draggableLocator = mockDraggableElementSearchDeprecated(draggable);
        Locator targetLocator = mockTargetElementSearchDeprecated(target);
        dragAndDropSteps.dragAndDropToTargetAtLocationDeprecated(draggableLocator, location, targetLocator);
    }

    @MethodSource("getElements")
    @ParameterizedTest
    void shouldNotDragAndDropWhenAnyElementIsNotFound(WebElement draggable, WebElement target)
    {
        testDragAndDropToTargetAtLocation(draggable, Location.TOP, target);
        verifyNoInteractions(webDriverProvider);
    }

    private void testDragAndDropToTargetAtLocation(WebElement draggable, Location location, WebElement target)
    {
        Locator draggableLocator = mockDraggableElementSearch(draggable);
        Locator targetLocator = mockTargetElementSearch(target);
        dragAndDropSteps.dragAndDropToTargetAtLocation(draggableLocator, location, targetLocator);
    }

    @Test
    void shouldSimulateDragAndDropDeprecated()
    {
        WebElement draggable = mock();
        WebElement target = mock();
        Locator draggableLocator = mockDraggableElementSearchDeprecated(draggable);
        Locator targetLocator = mockTargetElementSearchDeprecated(target);
        dragAndDropSteps.simulateDragAndDropDeprecated(draggableLocator, targetLocator);
        verify(javascriptActions)
                .executeScriptFromResource(DragAndDropSteps.class, SIMULATE_DRAG_AND_DROP_JS, draggable, target);
    }

    @Test
    void shouldSimulateDragAndDrop()
    {
        WebElement draggable = mock();
        WebElement target = mock();
        Locator draggableLocator = mockDraggableElementSearch(draggable);
        Locator targetLocator = mockTargetElementSearch(target);
        dragAndDropSteps.simulateDragAndDrop(draggableLocator, targetLocator);
        verify(javascriptActions).executeScriptFromResource(DragAndDropSteps.class, SIMULATE_DRAG_AND_DROP_JS,
                draggable, target);
    }

    private Locator mockDraggableElementSearchDeprecated(WebElement draggable)
    {
        return mockElementSearchDeprecated(draggable, DRAGGABLE_ELEMENT);
    }

    private Locator mockTargetElementSearchDeprecated(WebElement target)
    {
        return mockElementSearchDeprecated(target, TARGET_ELEMENT);
    }

    private Locator mockDraggableElementSearch(WebElement draggable)
    {
        return mockElementSearch(draggable, DRAGGABLE_ELEMENT);
    }

    private Locator mockTargetElementSearch(WebElement target)
    {
        return mockElementSearch(target, TARGET_ELEMENT);
    }

    private Locator mockElementSearchDeprecated(WebElement element, String assertionDescription)
    {
        Locator locator = mock(Locator.class);
        lenient().when(baseValidations.assertIfElementExists(assertionDescription, locator)).thenReturn(
                element);
        return locator;
    }

    private Locator mockElementSearch(WebElement element, String assertionDescription)
    {
        Locator locator = mock(Locator.class);
        lenient().when(baseValidations.assertElementExists(assertionDescription, locator))
                .thenReturn(Optional.ofNullable(element));
        return locator;
    }
}
