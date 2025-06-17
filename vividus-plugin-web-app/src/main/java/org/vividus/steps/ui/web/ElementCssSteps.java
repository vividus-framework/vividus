/*
 * Copyright 2019-2025 the original author or authors.
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

import static org.hamcrest.Matchers.containsString;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.annotation.Replacement;
import org.vividus.context.VariableContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.locator.Locator;
import org.vividus.softassert.SoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.JavascriptActions;
import org.vividus.ui.web.validation.CssValidations;
import org.vividus.util.ResourceUtils;
import org.vividus.variable.VariableScope;

@TakeScreenshotOnFailure
public class ElementCssSteps
{
    private static final String ELEMENT_CSS_CONTAINING_VALUE = "Element has CSS property '%s' containing value '%s'";

    private final IWebElementActions webElementActions;
    private final IUiContext uiContext;
    private final IDescriptiveSoftAssert descriptiveSoftAssert;
    private final IBaseValidations baseValidations;
    private final VariableContext variableContext;
    private final SoftAssert softAssert;
    private final JavascriptActions javascriptActions;
    private final CssValidations cssValidations;
    private final IAttachmentPublisher attachmentPublisher;

    public ElementCssSteps(IWebElementActions webElementActions, IUiContext uiContext,
                           IDescriptiveSoftAssert descriptiveSoftAssert, IBaseValidations baseValidations,
                           VariableContext variableContext, SoftAssert softAssert,
                           JavascriptActions javascriptActions, CssValidations cssValidations,
                           IAttachmentPublisher attachmentPublisher)
    {
        this.webElementActions = webElementActions;
        this.uiContext = uiContext;
        this.descriptiveSoftAssert = descriptiveSoftAssert;
        this.baseValidations = baseValidations;
        this.variableContext = variableContext;
        this.softAssert = softAssert;
        this.javascriptActions = javascriptActions;
        this.cssValidations = cssValidations;
        this.attachmentPublisher = attachmentPublisher;
    }

    /**
     * Checks that the context <b>element</b> has an expected <b>CSS property</b>
     * @param cssName A name of the <b>CSS property</b>
     * @param cssValue An expected value of <b>CSS property</b>
     * @deprecated Use step:
     * "Then context element has CSS property `$cssName` with value that is equal to `$cssValue`" instead
    */
    @Deprecated(since = "0.6.0", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
            replacementFormatPattern =
                    "Then context element has CSS property `%1$s` with value that is equal to `%2$s`")
    @Then("the context element has the CSS property '$cssName'='$cssValue'")
    public void doesElementHaveRightCss(String cssName, String cssValue)
    {
        uiContext.getSearchContext(WebElement.class).ifPresent(element -> {
            String actualCssValue = webElementActions.getCssValue(element, cssName);
            descriptiveSoftAssert.assertEquals("Element has correct css property value", cssValue, actualCssValue);
        });
    }

    /**
     * Checks that the context <b>element</b> has an expected <b>CSS property</b>
     * @param cssName A name of the <b>CSS property</b>
     * @param comparisonRule is equal to, contains, does not contain
     * @param cssValue An expected value of <b>CSS property</b>
     */
    @Then("context element has CSS property `$cssName` with value that $comparisonRule `$cssValue`")
    public void doesElementHaveRightCss(String cssName, StringComparisonRule comparisonRule, String cssValue)
    {
        uiContext.getSearchContext(WebElement.class).ifPresent(element -> {
            Matcher<String> matcher = comparisonRule.createMatcher(cssValue);
            String actualCssValue = webElementActions.getCssValue(element, cssName);
            descriptiveSoftAssert.assertThat("Element css property value is", actualCssValue, matcher);
        });
    }

    /**
     * Gets the value of <b>CSS property</b> from element located by <b>locator</b> and saves it to the <b>variable</b>
     * with the specified <b>variableName</b>
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li>Finds element using <i>locator</i>
     * <li>Extracts from the element value of the <i>cssProperty</i> and saves it to the <i>variableName</i>
     * </ul>
     * @param cssProperty    The name of the CSS property (for ex. 'color', 'flex')
     * @param locator        The locator to find an element
     * @param scopes         The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                       <i>Available scopes:</i>
     *                       <ul>
     *                       <li><b>STEP</b> - the variable will be available only within the step,
     *                       <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                       <li><b>STORY</b> - the variable will be available within the whole story,
     *                       <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                       </ul>
     * @param variableName   The name of the variable to save the CSS property value
     */
    @When("I save `$cssProperty` CSS property value of element located by `$locator` to $scopes"
            + " variable `$variableName`")
    public void saveCssPropertyValue(String cssProperty, Locator locator, Set<VariableScope> scopes,
                                     String variableName)
    {
        baseValidations.assertElementExists("The element to get the CSS property value", locator).ifPresent(e ->
        {
            String cssValue = webElementActions.getCssValue(e, cssProperty);
            if (!cssValue.isEmpty())
            {
                variableContext.putVariable(scopes, variableName, cssValue);
            }
            else
            {
                descriptiveSoftAssert
                        .recordFailedAssertion(String.format("The '%s' CSS property does not exist", cssProperty));
            }
        });
    }

    /**
     * Checks that the context <b>element</b> has an expected <b>CSS property</b> part
     * @param cssName A name of the <b>CSS property</b>
     * @param cssValue An expected value part of <b>CSS property</b>
     * @deprecated Use step:
     * "Then context element has CSS property `$cssName` with value that contains `$cssValue`" instead
    */
    @Deprecated(since = "0.6.0", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
            replacementFormatPattern = "Then context element has CSS property `%1$s` with value that contains `%2$s`")
    @Then("the context element has the CSS property '$cssName' containing '$cssValue'")
    public void doesElementHaveRightPartOfCssValue(String cssName, String cssValue)
    {
        uiContext.getSearchContext(WebElement.class).ifPresent(element -> {
            String actualCssValue = webElementActions.getCssValue(element, cssName);

            descriptiveSoftAssert.assertThat("Css property value part is correct",
                    String.format(ELEMENT_CSS_CONTAINING_VALUE, cssName, cssValue),
                    actualCssValue, containsString(cssValue));
        });
    }

    /**
     * Checks that the context <b>element</b> has an expected <b>CSS properties</b>
     * <p>The expected CSS parameters to be defined in the ExamplesTable:</p>
     * <ul>
     * <li><b>cssProperty</b> - the name of the CSS property</li>
     * <li><b>comparisonRule</b> - String comparison rule: "is equal to", "contains", "does not contain",
     * "matches".</li>
     * <li><b>expectedValue</b> - expected CSS property value</li>
     * </ul>
     * <p>Usage example:</p>
     * <code>
     * <br>Then context element does have CSS properties matching rules:
     * <br>|cssProperty |comparisonRule |expectedValue |
     * <br>|border      |contains       |solid         |
     * </code>
     *
     * @param parameters The parameters of the expected CSS properties to set as ExamplesTable
     */
    @Then("context element does have CSS properties matching rules:$parameters")
    public void doesElementHasCssProperties(List<CssValidationParameters> parameters)
    {
        uiContext.getSearchContext(WebElement.class).ifPresent(element -> {
            String getAllCssScript =
                    ResourceUtils.loadResource("org/vividus/ui/web/get-element-computed-css-func.js");
            Map<String, String> elementCss = javascriptActions.executeScript(getAllCssScript, element);

            List<CssValidationResult> cssResults = cssValidations.validateElementCss(parameters, elementCss);
            attachmentPublisher.publishAttachment("templates/css_validation_result.ftl",
                    Map.of("cssResults", cssResults), "CSS validation results");
        });
    }
}
