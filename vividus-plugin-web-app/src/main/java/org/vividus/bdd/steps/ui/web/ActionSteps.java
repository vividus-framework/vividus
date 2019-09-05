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

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.web.model.Action;
import org.vividus.bdd.steps.ui.web.model.ActionType;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.search.SearchAttributes;

@TakeScreenshotOnFailure
public class ActionSteps
{
    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IBaseValidations baseValidations;
    @Inject private ISoftAssert softAssert;

    /**
     * Executes the sequence of web actions
     * <div>Example:</div>
     * <code>
     *   <br>When I execute the sequence of actions:
     *   <br>|type           |searchAttributes                        |offset   |
     *   <br>|MOVE_BY_OFFSET |                                        |(-300, 0)|
     *   <br>|MOVE_BY_OFFSET |                                        |(0, 40)  |
     *   <br>|CLICK_AND_HOLD |By.xpath(//signature-pad-control/canvas)|         |
     *   <br>|MOVE_BY_OFFSET |                                        |(0, 100) |
     *   <br>|RELEASE        |By.xpath(//signature-pad-control/canvas)|         |
     * </code>
     * <br>
     * <br>
     * where
     * <ul>
     * <li><code>type</code> is one of web actions: move by offset, click and hold, release</li>
     * <li><code>searchAttributes</code> is search attribute to find element for interaction
     *  (could be empty if not applicable)</li>
     * <li><code>offset</code> the offset to move by (ex.: (10, 10))</li>
     * </ul>
     * @param actions table of actions to execute
     */
    @When("I execute the sequence of actions: $actions")
    public void executeActionsSequence(List<Action> actions)
    {
        Actions actionSequence = new Actions(webDriverProvider.get());
        for (Action action : actions)
        {
            ActionType actionType = action.getType();
            if (actionType.isCoordinatesRequired())
            {
                Optional<Point> offset = action.getOffset();
                if (!softAssert.assertTrue("Action offset is present", offset.isPresent()))
                {
                    return;
                }
                actionType.addAction(actionSequence, offset.get());
            }
            else
            {
                Optional<SearchAttributes> searchAttributes = action.getSearchAttributes();
                Optional<WebElement> element;
                if (searchAttributes.isPresent())
                {
                    WebElement foundElement = baseValidations.assertIfElementExists("Element for interaction",
                            searchAttributes.get());
                    if (foundElement == null)
                    {
                        return;
                    }
                    element = Optional.of(foundElement);
                }
                else
                {
                    element = Optional.empty();
                }
                actionType.addAction(actionSequence, element);
            }
        }
        actionSequence.build().perform();
    }
}
