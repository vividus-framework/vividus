/*
 * Copyright 2019-2020 the original author or authors.
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
import java.util.function.BiFunction;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.model.SequenceAction;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.action.search.Locator;

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
     * Executes the sequence of web actions
     * <div><b>Example:</b></div>
     * <pre>
     * <code>
     * When I execute sequence of actions:
     * <br> |type          |argument                                |
     * <br> |DOUBLE_CLICK  |By.fieldText(Hello World)               |
     * <br> |DOUBLE_CLICK  |                                        |
     * <br> |CLICK_AND_HOLD|By.xpath(//signature-pad-control/canvas)|
     * <br> |CLICK_AND_HOLD|                                        |
     * <br> |MOVE_BY_OFFSET|(-300, 0)                               |
     * <br> |RELEASE       |By.xpath(//signature-pad-control/canvas)|
     * <br> |RELEASE       |                                        |
     * <br> |ENTER_TEXT    |Text                                    |
     * <br> |CLICK         |By.placeholder(Enter your password)     |
     * <br> |CLICK         |                                        |
     * <br> |PRESS_KEYS    |BACK_SPACE                              |
     * <br> |KEY_DOWN      |CONTROL,SHIFT                           |
     * <br> |KEY_UP        |CONTROL,SHIFT                           |
     * <br> |MOVE_TO       |By.id(name)                             |
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
     * <td>search attribute or empty value</td>
     * <td>By.linkUrl(/examples)</td>
     * </tr>
     * <tr>
     * <td>CLICK_AND_HOLD</td>
     * <td>search attribute or empty value</td>
     * <td>By.linkText(Click me)</td>
     * </tr>
     * <tr>
     * <td>MOVE_BY_OFFSET</td>
     * <td>point</td>
     * <td>(10, 15) where <b>x</b> is 10 and <b>y</b> is 15</td>
     * </tr>
     * <tr>
     * <td>RELEASE</td>
     * <td>search attribute or empty value</td>
     * <td>By.tagName(div)</td>
     * </tr>
     * <tr>
     * <td>ENTER_TEXT</td>
     * <td>text</td>
     * <td>Minsk City</td>
     * </tr>
     * <tr>
     * <td>CLICK</td>
     * <td>search attribute or empty value</td>
     * <td>By.caseSensitiveText(Done)</td>
     * </tr>
     * <tr>
     * <td>PRESS_KEYS</td>
     * <td><a href="https://selenium.dev/selenium/docs/api/java/org/openqa/selenium/Keys.html">Keys</a></td>
     * <td>BACK_SPACE</td>
     * </tr>
     * <tr>
     * <td>KEY_DOWN</td>
     * <td>
     *     key – Either <a href="https://selenium.dev/selenium/docs/api/java/org/openqa/selenium/Keys.html">Keys</a>
     *     SHIFT, ALT or CONTROL.
     * </td>
     * <td>CONTROL,SHIFT</td>
     * </tr>
     * <tr>
     * <td>KEY_UP</td>
     * <td>
     *     key – Either <a href="https://selenium.dev/selenium/docs/api/java/org/openqa/selenium/Keys.html">Keys</a>
     *     SHIFT, ALT or CONTROL.
     * </td>
     * <td>CONTROL,SHIFT</td>
     * </tr>
     * <tr>
     * <td>MOVE_TO</td>
     * <td>search attribute</td>
     * <td>By.id(username)</td>
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
            if (argument != null && argument.getClass().equals(Locator.class))
            {
                WebElement element = baseValidations.assertIfElementExists("Element for interaction",
                        (Locator) argument);
                if (element == null)
                {
                    return false;
                }
                argument = element;
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
}
