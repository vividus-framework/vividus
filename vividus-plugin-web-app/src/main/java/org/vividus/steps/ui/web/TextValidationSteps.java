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
import java.util.regex.Pattern;

import org.jbehave.core.annotations.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.vividus.annotation.Replacement;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.steps.ui.web.validation.IElementValidations;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.WebXpathLocatorUtils;

@TakeScreenshotOnFailure
public class TextValidationSteps
{
    private final IUiContext uiContext;
    private final ISearchActions searchActions;
    private final IWebElementActions webElementActions;
    private final ISoftAssert softAssert;
    private final IBaseValidations baseValidations;
    private final IElementValidations elementValidations;

    public TextValidationSteps(IUiContext uiContext, ISearchActions searchActions, IWebElementActions webElementActions,
            ISoftAssert softAssert, IBaseValidations baseValidations, IElementValidations elementValidations)
    {
        this.uiContext = uiContext;
        this.searchActions = searchActions;
        this.webElementActions = webElementActions;
        this.softAssert = softAssert;
        this.baseValidations = baseValidations;
        this.elementValidations = elementValidations;
    }

    /**
     * Checks if the text in context matches <b>regex</b>
     * @param regex Expected regular expression
     */
    @Then("the text matches '$regex'")
    public void ifTextMatchesRegex(Pattern regex)
    {
        uiContext.getOptionalSearchContext().ifPresent(searchContext ->
        {
            String actualText = "";
            if (searchContext instanceof WebElement)
            {
                actualText = webElementActions.getElementText((WebElement) searchContext);
            }
            else if (searchContext instanceof WebDriver)
            {
                actualText = webElementActions.getPageText();
            }
            boolean assertCondition = !actualText.isEmpty() && verifyText(regex, actualText, searchContext);
            softAssert.assertTrue("The text in search context matches regular expression " + regex, assertCondition);
        });
    }

    private boolean verifyText(Pattern regex, String actualText, SearchContext searchContext)
    {
        boolean assertCondition = regex.matcher(actualText).find();
        if (!assertCondition && searchContext instanceof WebElement)
        {
            String pseudoElementContent = webElementActions.getPseudoElementContent((WebElement) searchContext);
            return !pseudoElementContent.isEmpty() && regex.matcher(pseudoElementContent).find();
        }
        return assertCondition;
    }

    /**
     * Checks if the <b>text</b> exists in context
     * @param text Expected text
     */
    @Then("text `$text` exists")
    public void ifTextExists(String text)
    {
        uiContext.getOptionalSearchContext().ifPresent(searchContext ->
        {
            if (searchContext instanceof WebElement)
            {
                elementValidations.assertIfElementContainsText((WebElement) searchContext, text, true);
            }
            else
            {
                List<WebElement> elements = findElements(searchContext,
                    WebXpathLocatorUtils.getXPathLocatorByInnerText(text));

                boolean assertCondition = !elements.isEmpty();

                if (!assertCondition)
                {
                    Locator caseSensitiveLocator = new Locator(WebLocatorType.CASE_SENSITIVE_TEXT, text);
                    assertCondition = !searchActions.findElements(caseSensitiveLocator).isEmpty();
                }

                if (!assertCondition)
                {
                    assertCondition = webElementActions.getAllPseudoElementsContent().stream()
                            .anyMatch(content -> content.contains(text));
                }

                if (!assertCondition)
                {
                    assertCondition = webElementActions.getPageText().trim().contains(text);
                }

                softAssert.assertTrue("There is an element with text=" + text + " in the context", assertCondition);
            }
        });
    }

    private List<WebElement> findElements(SearchContext searchContext, By locator)
    {
        try
        {
            return searchContext.findElements(locator);
        }
        // Workaround for WebDriverException: Permission denied to access property '_wrapped'
        catch (WebDriverException ex)
        {
            return searchContext.findElements(locator);
        }
    }

    /**
     * Checks if the <b>text</b> does not exist in context
     * @param text Text value
     * @return <code>true</code> if text does not exist, otherwise <code>false</code>
     * @deprecated Use step: "Then text `$text` does not exist"
     */
    @Deprecated(since = "0.5.4", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0", replacementFormatPattern = "Then text `%1$s` does not exist")
    @Then("the text '$text' does not exist")
    public boolean textDoesNotExist(String text)
    {
        return uiContext.getOptionalSearchContext().map(searchContext ->
        {
            if (searchContext instanceof WebElement)
            {
                return elementValidations.assertIfElementContainsText((WebElement) searchContext, text, false);
            }
            return baseValidations.assertIfElementDoesNotExist(String.format("An element with text '%s'", text),
                    new Locator(WebLocatorType.CASE_SENSITIVE_TEXT, text));
        }).orElse(false);
    }

    /**
     * Checks if the <b>text</b> does not exist in context
     * @param text Text value
     */
    @Then("text `$text` does not exist")
    public void assertTextDoesNotExist(String text)
    {
        uiContext.getOptionalSearchContext().ifPresent(searchContext ->
        {
            if (searchContext instanceof WebElement)
            {
                elementValidations.assertIfElementContainsText((WebElement) searchContext, text, false);
            }
            else
            {
                baseValidations.assertElementDoesNotExist(String.format("Element with text '%s'", text),
                        new Locator(WebLocatorType.CASE_SENSITIVE_TEXT, text));
            }
        });
    }
}
