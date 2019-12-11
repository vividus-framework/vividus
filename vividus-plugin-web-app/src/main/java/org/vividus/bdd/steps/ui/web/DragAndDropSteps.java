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

import java.time.Duration;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.ObjectUtils;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.bdd.steps.ui.web.model.Location;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.ui.web.action.search.SearchAttributes;

public class DragAndDropSteps
{
    private final IWebDriverProvider webDriverProvider;
    private final IJavascriptActions javascriptActions;
    private final IBaseValidations baseValidations;

    public DragAndDropSteps(IWebDriverProvider webDriverProvider, IJavascriptActions javascriptActions,
            IBaseValidations baseValidations)
    {
        this.webDriverProvider = webDriverProvider;
        this.javascriptActions = javascriptActions;
        this.baseValidations = baseValidations;
    }

    /**
     * Drags the <b>draggable</b> element and moves it relatively to the <b>target</b> element in
     * accordance to provided <b>location</b>.
     * <br>
     * <i>Example</i>
     * <br>
     * <code>When I drag element located `By.xpath(//div[@class='draggable'])` and drop it at RIGHT_TOP of element
     * located `By.xpath(//div[@class='target'])`</code>
     * If this step doesn't work, try to use step simulating drag&amp;drop
     * @param draggable draggable element
     * @param location location relatively to the <b>target</b> element (<b>TOP</b>,<b>BOTTOM</b>,<b>LEFT</b>,
     * <b>RIGHT</b>,<b>CENTER</b>,<b>LEFT_TOP</b>,<b>RIGHT_TOP</b>,<b>LEFT_BOTTOM</b>,<b>RIGHT_BOTTOM</b>)
     * @param target target element
     */
    @When("I drag element located `$draggable` and drop it at $location of element located `$target`")
    @SuppressWarnings("checkstyle:MagicNumber")
    public void dragAndDropToTargetAtLocation(SearchAttributes draggable, Location location, SearchAttributes target)
    {
        performDragAndDrop(draggable, target, (draggableElement, targetElement) ->
        {
            Point offsetPoint = location.getPoint(draggableElement.getRect(), targetElement.getRect());
            new Actions(webDriverProvider.get())
                    .clickAndHold(draggableElement)
                    // Selenium bug: https://github.com/SeleniumHQ/selenium/issues/1365#issuecomment-547786925
                    .moveByOffset(10, 0)
                    .moveByOffset(-10, 0)
                    .moveByOffset(offsetPoint.getX(), offsetPoint.getY())
                    .release()
                    // Wait for DOM stabilization
                    .pause(Duration.ofSeconds(1))
                    .perform();
        });
    }

    /**
     * Simulates drag of the <b>draggable</b> element and its drop at the <b>target</b> element via JavaScript.
     * <br>
     * <i>Example</i>
     * <br>
     * <code>When I simulate drag of element located `By.xpath(//div[@class='draggable'])` and drop at element located
     * `By.xpath(//div[@class='target'])`</code>
     * <p>
     *   The reason of having this step is that Selenium WebDriver doesn't support HTML5 drag&amp;drop:
     * </p>
     * <ul>
     *   <li><a href="https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/3604">
     *       Issue 3604: HTML5 Drag and Drop with Selenium WebDriver
     *       </a>
     *   </li>
     *   <li><a href="https://github.com/SeleniumHQ/selenium/issues/1365">
     *       Issue 1365: Actions drag and drop method
     *       </a>
     *   </li>
     * </ul>
     * <p>
     * As workaround for these issue the step simulates HTML5 drag&amp;drop via JavaScript. There is no difference in
     * actual drag&amp;drop and its simulation via JavaScript from the functional side.
     * </p>
     * @param draggable draggable element
     * @param target target element
     */
    @When("I simulate drag of element located `$draggable` and drop at element located `$target`")
    public void simulateDragAndDrop(SearchAttributes draggable, SearchAttributes target)
    {
        performDragAndDrop(draggable, target, (draggableElement, targetElement) ->
                // See gist for details: https://gist.github.com/valfirst/7f36c8755676cdf8943a8a8f08eab2e3
                javascriptActions.executeScriptFromResource(getClass(), "simulate-drag-and-drop.js", draggableElement,
                        targetElement));
    }

    private void performDragAndDrop(SearchAttributes draggable, SearchAttributes target,
            BiConsumer<WebElement, WebElement> dragAndDropExecutor)
    {
        WebElement draggableElement = baseValidations.assertIfElementExists("Draggable element", draggable);
        WebElement targetElement = baseValidations.assertIfElementExists("Target element", target);

        if (ObjectUtils.allNotNull(draggableElement, targetElement))
        {
            dragAndDropExecutor.accept(draggableElement, targetElement);
        }
    }
}
