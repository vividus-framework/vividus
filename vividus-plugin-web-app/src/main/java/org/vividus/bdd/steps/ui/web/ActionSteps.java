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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.web.model.Action;
import org.vividus.bdd.steps.ui.web.model.ActionType;
import org.vividus.bdd.steps.ui.web.model.SequenceAction;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.search.SearchAttributes;

@TakeScreenshotOnFailure
public class ActionSteps
{
    private final IWebDriverProvider webDriverProvider;
    private final IBaseValidations baseValidations;
    private final ISoftAssert softAssert;

    public ActionSteps(IWebDriverProvider webDriverProvider, IBaseValidations baseValidations, ISoftAssert softAssert)
    {
        this.webDriverProvider = webDriverProvider;
        this.baseValidations = baseValidations;
        this.softAssert = softAssert;
    }

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
     * @deprecated Use <i>When I execute sequence of actions: actions</i>
     */
    @Deprecated
    @When("I execute the sequence of actions: $actions")
    public void executeActionsSequence(List<Action> actions)
    {
        performActions(actions, (builder, action) ->
        {
            ActionType actionType = action.getType();
            if (actionType.isCoordinatesRequired())
            {
                Optional<Point> offset = action.getOffset();
                if (!softAssert.assertTrue("Action offset is present", offset.isPresent()))
                {
                    return false;
                }
                actionType.addAction(builder, offset.get());
            }
            else
            {
                Optional<SearchAttributes> searchAttributes = action.getSearchAttributes();
                Optional<WebElement> element;
                if (searchAttributes.isPresent())
                {
                    element = findElement(searchAttributes.get());
                    if (element.isEmpty())
                    {
                        return false;
                    }
                }
                else
                {
                    element = Optional.empty();
                }
                actionType.addAction(builder, element);
            }
            return true;
        });
    }

    /**
     * Executes the sequence of web actions
     * <div><b>Example:</b></div>
     * <pre>
     * <code>
     * When I execute sequence of actions:
     * <br> |type          |argument                                |
     * <br> |DOUBLE_CLICK  |By.fieldText(Hello World)               |
     * <br> |CLICK_AND_HOLD|By.xpath(//signature-pad-control/canvas)|
     * <br> |MOVE_BY_OFFSET|(-300, 0)                               |
     * <br> |RELEASE       |By.xpath(//signature-pad-control/canvas)|
     * <br> |ENTER_TEXT    |Text                                    |
     * <br> |CLICK         |By.placeholder(Enter your password)     |
     * <br> |PRESS_KEYS    |BACK_SPACE                              |
     * </code>
     * </pre>
     * where
     * <ul>
     * <li><code>type</code> can be taken from the following table:
     * <table border="1" style="border-collapse: collapse; border: 1px solid black">
     * <caption style="display: none">Arguments for action types</caption>
     * <tr>
     * <th>type</th>
     * <th>argument</th>
     * <th>example</th>
     * </tr>
     * <tr>
     * <td>DOUBLE_CLICK</td>
     * <td>search attribute</td>
     * <td>By.linkUrl(/examples)</td>
     * </tr>
     * <tr>
     * <td>CLICK_AND_HOLD</td>
     * <td>search attribute</td>
     * <td>By.linkText(Click me)</td>
     * </tr>
     * <tr>
     * <td>MOVE_BY_OFFSET</td>
     * <td>point</td>
     * <td>(10, 15) where <b>x</b> is 10 and <b>y</b> is 15</td>
     * </tr>
     * <tr>
     * <td>RELEASE</td>
     * <td>search attribute</td>
     * <td>By.tagName(div)</td>
     * </tr>
     * <tr>
     * <td>ENTER_TEXT</td>
     * <td>text</td>
     * <td>Minsk City</td>
     * </tr>
     * <tr>
     * <td>CLICK</td>
     * <td>search attribute</td>
     * <td>By.caseSensitiveText(Done)</td>
     * </tr>
     * <tr>
     * <td>PRESS_KEYS</td>
     * <td><a href="https://selenium.dev/selenium/docs/api/java/org/openqa/selenium/Keys.html">Keys</a>
     * </td>
     * <td>BACK_SPACE</td>
     * </tr>
     * </table>
     * </li>
     * <li><code>argument</code> either text or search attribute or point</li>
     * </ul>
     * @param actions table of actions to execute
     */
    @When("I execute sequence of actions: $actions")
    public void executeSequenceOfActions(List<SequenceAction> actions)
    {
        performActions(actions, (builder, action) ->
        {
            Object argument = action.getArgument();
            if (argument.getClass().equals(SearchAttributes.class))
            {
                Optional<WebElement> element = findElement((SearchAttributes) argument);
                if (element.isEmpty())
                {
                    return false;
                }
                argument = element.get();
            }
            action.getType().addAction(builder, argument);
            return true;
        });
    }

    private <T> void performActions(Collection<T> elements, BiFunction<Actions, T, Boolean> iterateFunction)
    {
        Actions actions = new Actions(webDriverProvider.get());
        for (T element : elements)
        {
            if (!iterateFunction.apply(actions, element))
            {
                return;
            }
        }
        actions.build().perform();
    }

    private Optional<WebElement> findElement(SearchAttributes searchAttributes)
    {
        return Optional.ofNullable(baseValidations.assertIfElementExists("Element for interaction", searchAttributes));
    }
}
