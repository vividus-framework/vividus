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

import javax.inject.Inject;

import org.apache.commons.lang3.ObjectUtils;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.bdd.steps.ui.web.model.Location;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.action.search.SearchAttributes;

public class DragAndDropSteps
{
    @Inject private IBaseValidations baseValidations;
    @Inject private IWebDriverProvider webDriverProvider;

    /**
     * Drags the <b>origin</b> element and moves it relatively to the <b>target</b> element in
     * accordance to provided <b>location</b>.
     * <br>
     * <i>Example</i>
     * <br>
     * <code>When I drag element located `By.xpath(//div[@class='origin'])` and drop it at RIGHT_TOP of element
     * located `By.xpath(//div[@class='target'])`</code>
     * @param origin element to be dragged
     * @param location location relatively to the <b>target</b> element (<b>TOP</b>,<b>BOTTOM</b>,<b>LEFT</b>,
     * <b>RIGHT</b>,<b>CENTER</b>,<b>LEFT_TOP</b>,<b>RIGHT_TOP</b>,<b>LEFT_BOTTOM</b>,<b>RIGHT_BOTTOM</b>)
     * @param target target element
     */
    @When("I drag element located `$origin` and drop it at $location of element located `$target`")
    @SuppressWarnings("checkstyle:MagicNumber")
    public void dragAndDropToTargetAtLocation(SearchAttributes origin, Location location, SearchAttributes target)
    {
        WebElement originElement = baseValidations.assertIfElementExists("Origin element", origin);
        WebElement targetElement = baseValidations.assertIfElementExists("Target element", target);

        if (ObjectUtils.allNotNull(originElement, targetElement))
        {
            Point offsetPoint = location.getPoint(originElement.getRect(), targetElement.getRect());
            new Actions(webDriverProvider.get())
                    .clickAndHold(originElement)
                    // Selenium bug: https://github.com/SeleniumHQ/selenium/issues/1365#issuecomment-547786925
                    .moveByOffset(10, 0)
                    .moveByOffset(-10, 0)
                    .moveByOffset(offsetPoint.getX(), offsetPoint.getY())
                    .release()
                    // Wait for DOM stabilization
                    .pause(Duration.ofSeconds(1))
                    .perform();
        }
    }
}
