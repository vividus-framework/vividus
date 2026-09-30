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

import java.util.List;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.DragToOptions;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.Position;

import org.jbehave.core.annotations.When;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.ui.web.playwright.model.Location;

public class DragAndDropSteps
{
    private static final String SIMULATE_DRAG_AND_DROP_JS = """
            ([sourceNode, destinationNode]) => {
                function createCustomEvent(type) {
                    const event = new CustomEvent(type, { bubbles: true, cancelable: true });
                    event.dataTransfer = new DataTransfer();
                    return event;
                }
                const dragStartEvent = createCustomEvent('dragstart');
                sourceNode.dispatchEvent(dragStartEvent);

                const mouseOverEvent = new MouseEvent('mouseover', { bubbles: true, cancelable: true });
                destinationNode.dispatchEvent(mouseOverEvent);

                const dropEvent = createCustomEvent('drop');
                dropEvent.dataTransfer = dragStartEvent.dataTransfer;
                destinationNode.dispatchEvent(dropEvent);

                const dragEndEvent = createCustomEvent('dragend');
                dragEndEvent.dataTransfer = dragStartEvent.dataTransfer;
                sourceNode.dispatchEvent(dragEndEvent);
            }""";

    private final UiContext uiContext;

    public DragAndDropSteps(UiContext uiContext)
    {
        this.uiContext = uiContext;
    }

    /**
     * Drags the <b>draggable</b> element and moves it relatively to the <b>target</b> element in
     * accordance to provided <b>location</b>.
     * <br>
     * <i>Example</i>
     * <br>
     * <code>When I drag element located by `id(draggable)` and drop it at RIGHT_TOP of element
     * located by `id(target)`</code>
     * If this step doesn't work, try to use step simulating drag&amp;drop
     *
     * @param draggable The locator used to find the draggable element.
     * @param location  The location relatively to the <b>target</b> element (<b>TOP</b>, <b>BOTTOM</b>,
     *                  <b>LEFT</b>, <b>RIGHT</b>, <b>CENTER</b>, <b>LEFT_TOP</b>, <b>RIGHT_TOP</b>,
     *                  <b>LEFT_BOTTOM</b>, <b>RIGHT_BOTTOM</b>).
     * @param target    The locator used to find the target element.
     */
    @When("I drag element located by `$draggable` and drop it at $location of element located by `$target`")
    public void dragAndDropToTargetAtLocation(PlaywrightLocator draggable, Location location,
            PlaywrightLocator target)
    {
        Locator draggableElement = uiContext.locateElement(draggable);
        Locator targetElement = uiContext.locateElement(target);
        BoundingBox targetBoundingBox = targetElement.boundingBox();
        Position targetPosition = location.calculatePosition(targetBoundingBox);
        draggableElement.dragTo(targetElement, new DragToOptions().setTargetPosition(targetPosition));
    }

    /**
     * Simulates drag of the <b>draggable</b> element and its drop at the <b>target</b> element via JavaScript.
     * <br>
     * <i>Example</i>
     * <br>
     * <code>When I simulate drag of element located by `id(draggable)` and drop at element located by
     * `id(target)`</code>
     * <p>
     * The reason of having this step is that some pages implement HTML5 drag&amp;drop in a way that is not
     * compatible with the native mouse-based drag and drop performed by the {@link #dragAndDropToTargetAtLocation}
     * step. As a workaround for such cases the step simulates HTML5 drag&amp;drop via JavaScript.
     * </p>
     *
     * @param draggable The locator used to find the draggable element.
     * @param target    The locator used to find the target element.
     */
    @When("I simulate drag of element located by `$draggable` and drop at element located by `$target`")
    public void simulateDragAndDrop(PlaywrightLocator draggable, PlaywrightLocator target)
    {
        ElementHandle draggableHandle = uiContext.locateElement(draggable).elementHandle();
        ElementHandle targetHandle = uiContext.locateElement(target).elementHandle();
        uiContext.getCurrentPage().evaluate(SIMULATE_DRAG_AND_DROP_JS,
                List.of(draggableHandle, targetHandle));
    }
}
