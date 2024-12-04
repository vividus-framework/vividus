/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.steps;

import java.util.List;
import java.util.Map;

import com.microsoft.playwright.Locator;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Then;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.web.CssValidationParameters;
import org.vividus.steps.ui.web.CssValidationResult;
import org.vividus.ui.web.action.JavascriptActions;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.ElementActions;
import org.vividus.ui.web.validation.CssValidations;
import org.vividus.util.ResourceUtils;

public class ElementCssSteps
{
    private final UiContext uiContext;
    private final ISoftAssert softAssert;
    private final ElementActions elementActions;
    private final JavascriptActions javascriptActions;
    private final CssValidations cssValidations;
    private final IAttachmentPublisher attachmentPublisher;

    public ElementCssSteps(UiContext uiContext, ISoftAssert softAssert, ElementActions elementActions,
                           JavascriptActions javascriptActions, CssValidations cssValidations,
                           IAttachmentPublisher attachmentPublisher)
    {
        this.uiContext = uiContext;
        this.softAssert = softAssert;
        this.elementActions = elementActions;
        this.javascriptActions = javascriptActions;
        this.cssValidations = cssValidations;
        this.attachmentPublisher = attachmentPublisher;
    }

    /**
     * Checks that the context <b>element</b> has an expected <b>CSS property</b>
     * @param cssName A name of the <b>CSS property</b>
     * @param comparisonRule is equal to, contains, does not contain
     * @param cssValue An expected value of the <b>CSS property</b>
     */
    @Then("context element has CSS property `$cssName` with value that $comparisonRule `$cssValue`")
    public void assertElementCssProperty(String cssName, StringComparisonRule comparisonRule, String cssValue)
    {
        String actualCssValue = elementActions.getCssValue(uiContext.getCurrentContexOrPageRoot(),
                cssName);
        Matcher<String> matcher = comparisonRule.createMatcher(cssValue);
        softAssert.assertThat("Element css property value is", actualCssValue, matcher);
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
        String getAllCssScript =
                ResourceUtils.loadResource("org/vividus/ui/web/get-element-computed-css-func.js");
        String script = "arguments => {" + getAllCssScript + "}";
        Locator element = uiContext.getCurrentContexOrPageRoot();
        Map<String, String> elementCss = javascriptActions.executeScript(script, element.elementHandle());

        List<CssValidationResult> cssResults = cssValidations.validateElementCss(parameters, elementCss);
        attachmentPublisher.publishAttachment("templates/css_validation_result.ftl",
                Map.of("cssResults", cssResults), "CSS validation results");
    }
}
