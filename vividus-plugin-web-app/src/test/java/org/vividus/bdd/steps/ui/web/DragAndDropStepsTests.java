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

package org.vividus.bdd.steps.ui.web;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

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
import org.vividus.bdd.steps.ui.web.model.Location;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.action.search.SearchAttributes;

@ExtendWith(MockitoExtension.class)
class DragAndDropStepsTests
{
    private static final String ACTIONS_SEQUENCE = "{id=default mouse, type=pointer, parameters={pointerType=mouse}, "
            + "actions=["
            + "{duration=100, x=0, y=0, type=pointerMove, origin=Mock for WebElement, hashCode: %s}, "
            + "{button=0, type=pointerDown}, "
            + "{duration=200, x=10, y=0, type=pointerMove, origin=pointer}, "
            + "{duration=200, x=-10, y=0, type=pointerMove, origin=pointer}, "
            + "{duration=200, x=100, y=50, type=pointerMove, origin=pointer}, "
            + "{button=0, type=pointerUp}, "
            + "{duration=1000, type=pause}]}";

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private SearchAttributes searchAttributes;

    @InjectMocks
    private DragAndDropSteps dragAndDropSteps;

    @Test
    void testDragAndDropToTargetAtLocation()
    {
        WebElement origin = mockRect(100, 100, 50, 50);
        mockFindElements(origin, mockRect(200, 200, 50, 50));
        WebDriver driver = mock(WebDriver.class, withSettings().extraInterfaces(Interactive.class));
        when(webDriverProvider.get()).thenReturn(driver);
        dragAndDropSteps.dragAndDropToTargetAtLocation(searchAttributes, Location.TOP, searchAttributes);
        verify((Interactive) driver).perform(argThat(arg -> arg.iterator().next().encode().toString()
                .equals(String.format(ACTIONS_SEQUENCE, origin.hashCode()))));
    }

    private static WebElement mockRect(int x, int y, int height, int width)
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

    @MethodSource("getElements")
    @ParameterizedTest
    void testDragAndDropToTargetAtLocationElementsNotFound(WebElement origin, WebElement target)
    {
        mockFindElements(origin, target);
        dragAndDropSteps.dragAndDropToTargetAtLocation(searchAttributes, Location.TOP, searchAttributes);
        verifyNoInteractions(webDriverProvider);
    }

    void mockFindElements(WebElement origin, WebElement target)
    {
        lenient().when(baseValidations.assertIfElementExists("Origin element", searchAttributes)).thenReturn(origin);
        lenient().when(baseValidations.assertIfElementExists("Target element", searchAttributes)).thenReturn(target);
    }
}
