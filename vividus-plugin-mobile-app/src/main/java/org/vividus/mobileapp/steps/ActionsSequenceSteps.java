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

package org.vividus.mobileapp.steps;

import java.util.List;

import org.jbehave.core.annotations.When;
import org.vividus.mobileapp.action.TouchGestures;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.AtomicAction;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.steps.AbstractActionsSequenceSteps;

@TakeScreenshotOnFailure
public class ActionsSequenceSteps extends AbstractActionsSequenceSteps
{
    public ActionsSequenceSteps(IWebDriverProvider actionBuilder, IBaseValidations baseValidations)
    {
        super(actionBuilder, baseValidations);
    }

    /**
     * Executes the sequence of touch actions
     * <div><b>Example:</b></div>
     * <pre>
     * When I execute sequence of touch actions:
     * |type          |argument                                |
     * |TAP_AND_HOLD  |By.accessibilityId(OK)                  |
     * |TAP_AND_HOLD  |                                        |
     * |MOVE_BY_OFFSET|(-300, 0)                               |
     * |RELEASE       |                                        |
     * |TAP           |By.accessibilityId(Close)               |
     * |TAP           |                                        |
     * |MOVE_TO       |By.id(name)                             |
     * </pre>
     * where <code>type</code> and <code>argument</code> can be taken from the following table:
     * <table border="1" style="border-collapse: collapse; border: 1px solid black">
     * <caption style="display: none">Arguments for action types</caption>
     * <tr><th>type</th><th>argument</th><th>example</th></tr>
     * <tr><td>TAP</td><td>element locator or empty</td><td>By.id(Name)</td></tr>
     * <tr><td>TAP_AND_HOLD</td><td>element locator or empty</td><td>By.id(Name)</td></tr>
     * <tr><td>MOVE_BY_OFFSET</td><td>point</td><td>(10, 15) where <b>x</b> is 10 and <b>y</b> is 15</td></tr>
     * <tr><td>RELEASE</td><td>empty</td><td></td></tr>
     * <tr><td>WAIT</td><td>duration</td><td>PT1S - wait 1 second</td></tr>
     * <tr><td>MOVE_TO</td><td>element locator</td><td>By.id(username)</td></tr>
     * </table>
     * @param actions table of actions to execute
     */
    @When("I execute sequence of touch actions:$actions")
    public void executeSequenceOfActions(List<AtomicAction<TouchGestures>> actions)
    {
        execute(TouchGestures::new, actions);
    }
}
