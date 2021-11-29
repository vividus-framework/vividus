/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.mobileapp.steps;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.mobileapp.action.MobileSequenceActionType;
import org.vividus.mobileapp.action.PositionCachingTouchAction;
import org.vividus.monitor.TakeScreenshotOnFailure;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.steps.ui.model.SequenceAction;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;

import io.appium.java_client.PerformsTouchActions;

@TakeScreenshotOnFailure
public class ActionSteps
{
    private final IWebDriverProvider webDriverProvider;
    private final IBaseValidations baseValidations;

    public ActionSteps(IWebDriverProvider webDriverProvider, IBaseValidations baseValidations)
    {
        this.webDriverProvider = webDriverProvider;
        this.baseValidations = baseValidations;
    }

    /**
     * Executes the sequence of actions
     * <div><b>Example:</b></div>
     * <pre>
     * <code>
     * When I execute sequence of actions:
     * <br> |type          |argument              |
     * <br> |PRESS         |By.id(Hello World)    |
     * <br> |WAIT          |PT1S                  |
     * <br> |MOVE_TO       |By.accessibilityId(OK)|
     * <br> |RELEASE       |                      |
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
     * <td>PRESS</td>
     * <td>search attribute</td>
     * <td>By.id(Name)</td>
     * </tr>
     * <tr>
     * <td>PRESS_BY_COORDS</td>
     * <td>coordinates</td>
     * <td>(50, 200) where <b>x</b> is 50 and <b>y</b> is 200</td>
     * </tr>
     * <tr>
     * <td>MOVE_TO</td>
     * <td>search attribute</td>
     * <td>By.accessibilityId(OK)</td>
     * </tr>
     * <tr>
     * <td>MOVE_BY_OFFSET</td>
     * <td>point with relative coordinates to previous position</td>
     * <td>(10, 15) where <b>x offset</b> is 10 and <b>y offset</b> is 15</td>
     * </tr>
     * <tr>
     * <td>WAIT</td>
     * <td>duration</td>
     * <td>PT1S - wait 1 second</td>
     * </tr>
     * <tr>
     * <td>RELEASE</td>
     * <td></td>
     * <td></td>
     * </tr>
     * </table>
     * </li>
     * <li><code>argument</code> either duration or search attribute or point</li>
     * </ul>
     * @param actions table of actions to execute
     */
    @When("I execute sequence of actions: $actions")
    public void executeSequenceOfActions(List<SequenceAction<MobileSequenceActionType>> actions)
    {
        performActions(actions, (builder, action) ->
        {
            Object argument = action.getArgument();
            if (argument != null && argument.getClass().equals(Locator.class))
            {
                Optional<WebElement> element = baseValidations.assertElementExists("Element for interaction",
                        (Locator) argument);
                if (!element.isPresent())
                {
                    return false;
                }
                argument = element.get();
            }
            action.getType().addAction(builder, argument);
            return true;
        });
    }

    private <T> void performActions(Collection<T> elements,
            BiFunction<PositionCachingTouchAction, T, Boolean> iterateFunction)
    {
        PerformsTouchActions performsTouchActions = webDriverProvider.getUnwrapped(PerformsTouchActions.class);
        PositionCachingTouchAction touchAction = new PositionCachingTouchAction(performsTouchActions);

        for (T element : elements)
        {
            if (!iterateFunction.apply(touchAction, element))
            {
                return;
            }
        }
        touchAction.perform();
    }
}
