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

import static org.vividus.bdd.steps.ui.web.ElementPattern.LABEL_PATTERN;
import static org.vividus.bdd.steps.ui.web.ElementPattern.RADIO_OPTION_INPUT_PATTERN;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.util.LocatorUtil;

@TakeScreenshotOnFailure
public class RadioButtonSteps
{
    private static final String RADIO_BUTTON = "Radio button";

    @Inject private IMouseActions mouseActions;
    @Inject private IBaseValidations baseValidations;

    /**
     * Checks that a <b>radio button</b> specified by the <b>name</b> exists within
     * the previously specified <b>search context</b>
     * <p>A <b>radio button</b> is an <i><code>&lt;input&gt;</code></i> element with an attribute
     * 'type' = 'radio' and a <b>name</b> for it is a 'text' or any 'attribute value' of
     * it's <i><code>&lt;label&gt;</code></i> element (<i><code>&lt;label&gt;</code></i>
     * with an attribute 'for' = radio button id).
     * </p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds the <b>radio button</b> specified by the <b>name</b> in the radio group;</li>
     * </ul>
     * @param radioOption A name of the <b>radio button</b>
     * @return WebElement An <b>element</b> matching the requirements,
     * <b> null</b> - if there are no desired elements.
     * <b>Example:</b>
     * <pre>
     * {@code <div>
     *      <label>}<b>'radioGroup'</b>{@code </label>
     *      <div>
     *          <input id="radioButtonId" type="radio" />
     *          <label for="radioButtonId">}<b>'radioOption'</b>{@code </label>
     *      </div>
     * </div>}</pre>
     */
    @Then("a radio button with the name '$radioOption' exists")
    public WebElement assertIfRadioOptionExists(String radioOption)
    {
        WebElement radioButtonLabel = baseValidations.assertIfElementExists(
                String.format("A radio button label with text '%s'", radioOption),
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(LABEL_PATTERN, radioOption)));
        if (radioButtonLabel == null)
        {
            return null;
        }
        String labelForAtr = radioButtonLabel.getAttribute("for");
        if (StringUtils.isNotEmpty(labelForAtr))
        {
            return baseValidations.assertIfElementExists(RADIO_BUTTON, new SearchAttributes(ActionAttributeType.XPATH,
                    LocatorUtil.getXPath(RADIO_OPTION_INPUT_PATTERN, labelForAtr)));
        }
        return baseValidations.assertIfElementExists(RADIO_BUTTON, radioButtonLabel,
                new SearchAttributes(ActionAttributeType.XPATH, "input[@type='radio']"));
    }

    /**
     * Checks a <b>radio button</b> specified by the <b>name</b>
     * within the specified <b>search context</b>
     * <p>A <b>radio group</b> is some <i><code>&lt;div&gt;</code></i> that contains a <b>radio buttons</b>,
     * and it's <b>name</b> is a 'text' containing in one of it's child elements.
     * </p>
     * <p>A <b>radio button</b> is an <i><code>&lt;input&gt;</code></i> element with an attribute
     * 'type' = 'radio' and a <b>name</b> for it is a 'text' or any 'attribute value' of
     * it's <i><code>&lt;label&gt;</code></i> element (<i><code>&lt;label&gt;</code></i>
     * with an attribute 'for' = radio button id).
     * </p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds the <b>radio button</b> specified by the <b>name</b>;</li>
     * <li>Clicks on the <b>radio button</b>;</li>
     * <li>Waits for the page to load;</li>
     * </ul>
     * @param radioOption A name of the <b>radio button</b>.
     * <b>Example:</b>
     * <pre>
     * {@code <div>
     *      <label>}<b>'radioGroup'</b>{@code </label>
     *      <div>
     *          <input id="radioButtonId" type="radio" />
     *          <label for="radioButtonId">}<b>'radioOption'</b>{@code </label>
     *      </div>
     * </div>}</pre>
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I select a radio button with the name '$radioOption'")
    public void checkRadioOption(String radioOption)
    {
        WebElement element = assertIfRadioOptionExists(radioOption);
        mouseActions.click(element);
    }

    /**
     * Checks that a <b>radio button</b> specified by the <b>name</b> exists within
     * the previously specified <b>search context</b>
     * <p>A <b>radio button</b> is an <i><code>&lt;input&gt;</code></i> element with an attribute
     * 'type' = 'radio' and a <b>name</b> for it is a 'text' or any 'attribute value' of
     * it's <i><code>&lt;label&gt;</code></i> element (<i><code>&lt;label&gt;</code></i>
     * with an attribute 'for' = radio button id).
     * </p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds the <b>radio button</b> specified by the <b>name</b> in the <b>radio group</b> ;
     * <li>Compares an actual radio button 'state' with expected;
     * </ul>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param radioOption A name of the <b>radio button</b>.
     * <b>Example:</b>
     * <pre>
     * {@code <div>
     *      <label>}<b>'radioGroup'</b>{@code </label>
     *      <div>
     *          <input id="radioButtonId" type="radio" />
     *          <label for="radioButtonId">}<b>'radioOption'</b>{@code </label>
     *      </div>
     * </div>}</pre>
     */
    @Then("a [$state] radio button with the name '$radioOption' exists")
    public void assertIfRadioOptionExists(State state, String radioOption)
    {
        WebElement radioButton = assertIfRadioOptionExists(radioOption);
        baseValidations.assertElementState("The found radio button is " + state, state, radioButton);
    }

    /**
     * Checks that <b>radio buttons</b> specified by the <b>name</b> exists in the context
     * <p>
     * A <b>radio button</b> is an <i>&lt;input&gt;</i> element with an attribute 'type' = 'radio' and a <b>name</b>
     * for it is a 'text' or any 'attribute value' of it's <i>&lt;label&gt;</i> element (<i>&lt;label&gt;</i>
     * with an attribute 'for' = radio button id).</p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds the <b>label of the radio button</b>
     * <li>Check that this label exists
     * <li>Check that the <b>radio button</b> exists
     * </ul>
     * @param radioOptions Table of items: text of the radioOptions:
     * <pre>
     * |radioOption |
     * |$option     |
     * |$option     |
     * |$option     |
     * </pre>
     * <b>Example:</b>
     * <pre>
     * &lt;div&gt;
     *      &lt;div&gt;
     *          &lt;input id="radioButtonId" type="radio" /&gt;
     *          &lt;label for="radioButtonId"&gt;<b>'radioOption'</b>&lt;/label&gt;
     *      &lt;/div&gt;
     * &lt;/div&gt;
     * </pre>
     */
    @Then("an element contains the radio buttons: $radioOptions")
    public void doesElementContainRadioOptions(ExamplesTable radioOptions)
    {
        for (Parameters row : radioOptions.getRowsAsParameters(true))
        {
            assertIfRadioOptionExists(row.valueAs("radioOption", String.class));
        }
    }
}
