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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.DragToOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.Position;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.ui.web.playwright.model.Location;

@ExtendWith(MockitoExtension.class)
class DragAndDropStepsTests
{
    @Mock private UiContext uiContext;
    @Mock private PlaywrightLocator draggableLocator;
    @Mock private PlaywrightLocator targetLocator;
    @Mock private Locator draggable;
    @Mock private Locator target;
    @InjectMocks private DragAndDropSteps steps;

    @Test
    void shouldDragAndDropToTargetAtLocation()
    {
        when(uiContext.locateElement(draggableLocator)).thenReturn(draggable);
        when(uiContext.locateElement(targetLocator)).thenReturn(target);
        BoundingBox boundingBox = new BoundingBox();
        boundingBox.width = 100;
        boundingBox.height = 50;
        when(target.boundingBox()).thenReturn(boundingBox);

        steps.dragAndDropToTargetAtLocation(draggableLocator, Location.CENTER, targetLocator);

        verify(draggable).dragTo(eq(target), argThat((DragToOptions options) ->
        {
            Position position = options.targetPosition;
            return position.x == 50 && position.y == 25;
        }));
    }

    @Test
    void shouldSimulateDragAndDrop()
    {
        ElementHandle draggableHandle = mock(ElementHandle.class);
        ElementHandle targetHandle = mock(ElementHandle.class);
        Page page = mock(Page.class);
        when(uiContext.locateElement(draggableLocator)).thenReturn(draggable);
        when(uiContext.locateElement(targetLocator)).thenReturn(target);
        when(draggable.elementHandle()).thenReturn(draggableHandle);
        when(target.elementHandle()).thenReturn(targetHandle);
        when(uiContext.getCurrentPage()).thenReturn(page);

        steps.simulateDragAndDrop(draggableLocator, targetLocator);

        verify(page).evaluate(argThat((String script) -> script.contains("dragstart")),
                eq(List.of(draggableHandle, targetHandle)));
    }
}
