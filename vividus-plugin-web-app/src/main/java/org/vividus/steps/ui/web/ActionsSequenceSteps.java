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

import java.util.List;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.interactions.Actions;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.AtomicAction;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.steps.AbstractActionsSequenceSteps;

@TakeScreenshotOnFailure
public class ActionsSequenceSteps extends AbstractActionsSequenceSteps
{
    public ActionsSequenceSteps(IWebDriverProvider webDriverProvider, IBaseValidations baseValidations)
    {
        super(webDriverProvider, baseValidations);
    }

    /**
     * Executes the sequence of web actions
     * <div><b>Example:</b></div>
     * <pre>
     * When I execute sequence of actions:
     * |type          |argument                                |
     * |DOUBLE_CLICK  |By.fieldText(Hello World)               |
     * |DOUBLE_CLICK  |                                        |
     * |CLICK_AND_HOLD|By.xpath(//signature-pad-control/canvas)|
     * |CLICK_AND_HOLD|                                        |
     * |MOVE_BY_OFFSET|(-300, 0)                               |
     * |RELEASE       |By.xpath(//signature-pad-control/canvas)|
     * |RELEASE       |                                        |
     * |ENTER_TEXT    |Text                                    |
     * |CLICK         |By.placeholder(Enter your password)     |
     * |CLICK         |                                        |
     * |PRESS_KEYS    |BACK_SPACE                              |
     * |KEY_DOWN      |CONTROL,SHIFT                           |
     * |KEY_UP        |CONTROL,SHIFT                           |
     * |MOVE_TO       |By.id(name)                             |
     * </pre>
     * where <code>type</code> and <code>argument</code> can be taken from the following table:
     * <table border="1" style="border-collapse: collapse; border: 1px solid black">
     * <caption style="display: none">Arguments for action types</caption>
     * <tr><th>type</th><th>argument</th><th>example</th></tr>
     * <tr><td>DOUBLE_CLICK</td><td>element locator or empty</td><td>By.linkUrl(/examples)</td></tr>
     * <tr><td>CLICK_AND_HOLD</td><td>element locator or empty</td><td>By.linkText(Click me)</td></tr>
     * <tr><td>MOVE_BY_OFFSET</td><td>point</td><td>(10, 15) where <b>x</b> is 10 and <b>y</b> is 15</td></tr>
     * <tr><td>RELEASE</td><td>element locator or empty</td><td>By.tagName(div)</td></tr>
     * <tr><td>ENTER_TEXT</td><td>text</td><td>Minsk City</td></tr>
     * <tr><td>CLICK</td><td>element locator or empty</td><td>By.caseSensitiveText(Done)</td></tr>
     * <tr>
     * <td>PRESS_KEYS</td>
     * <td>Press and release any of
     * <a href="https://selenium.dev/selenium/docs/api/java/org/openqa/selenium/Keys.html">Keys</a></td>
     * <td>BACK_SPACE</td>
     * </tr>
     * <tr>
     * <td>KEY_DOWN</td>
     * <td>Press any of <a href="https://selenium.dev/selenium/docs/api/java/org/openqa/selenium/Keys.html">Keys</a>
     *  one by one</td>
     * <td>CONTROL,SHIFT</td>
     * </tr>
     * <tr>
     * <td>KEY_UP</td>
     * <td>Release any of <a href="https://selenium.dev/selenium/docs/api/java/org/openqa/selenium/Keys.html">Keys</a>
     * one by one</td>
     * <td>CONTROL,SHIFT</td>
     * </tr>
     * <tr><td>MOVE_TO</td><td>element locator</td><td>By.id(username)</td></tr>
     * </table>
     *
     * @param actions table of actions to execute
     */
    @When("I execute sequence of actions:$actions")
    public void executeSequenceOfActions(List<AtomicAction<Actions>> actions)
    {
        execute(Actions::new, actions);
    }
}
