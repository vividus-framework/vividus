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

import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.bdd.steps.ui.web.validation.IElementValidations;
import org.vividus.ui.State;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.LocatorUtil;

@TakeScreenshotOnFailure
public class WebElementsSteps
{
    @Inject private IWebElementActions webElementActions;
    @Inject private IBaseValidations baseValidations;
    @Inject private IElementValidations elementValidations;
    @Inject private IUiContext uiContext;
    @Inject private ISearchActions searchActions;
    @Inject private IDescriptiveSoftAssert descriptiveSoftAssert;

    /**
     * Checks if the text in context matches <b>regex</b>
     * @param regex Expected regular expression
     */
    @Then("the text matches '$regex'")
    public void ifTextMatchesRegex(String regex)
    {
        String actualText = "";
        boolean assertCondition = false;
        boolean isWebElement = getSearchContext() instanceof WebElement;
        if (isWebElement)
        {
            actualText = webElementActions.getElementText((WebElement) getSearchContext());
        }
        if (getSearchContext() instanceof WebDriver)
        {
            actualText = webElementActions.getPageText();
        }
        if (!actualText.isEmpty())
        {
            Pattern pattern = Pattern.compile(regex);
            assertCondition = pattern.matcher(actualText).find();
            if (!assertCondition && isWebElement)
            {
                String pseudoElementContent = webElementActions
                        .getPseudoElementContent((WebElement) getSearchContext());
                if (!pseudoElementContent.isEmpty())
                {
                    assertCondition = pattern.matcher(pseudoElementContent).find();
                }
            }
        }
        descriptiveSoftAssert.assertTrue("The text in search context matches regular expression " + regex,
                assertCondition);
    }

    /**
     * Checks if the <b>text</b> exists in context
     * @param text Expected text
     */
    @Then("the text '$text' exists")
    public void ifTextExists(String text)
    {
        boolean searchFromRoot = getSearchContext() instanceof WebDriver;
        if (!searchFromRoot)
        {
            elementValidations.assertIfElementContainsText(uiContext.getSearchContext(WebElement.class), text, true);
        }
        else
        {
            By locator = LocatorUtil.getXPathLocatorByInnerText(text);

            List<WebElement> elements;
            try
            {
                elements = getSearchContext().findElements(locator);
            }
            // Workaround for WebDriverException: Permission denied to access property '_wrapped'
            catch (WebDriverException ex)
            {
                elements = getSearchContext().findElements(locator);
            }

            boolean assertCondition = !elements.isEmpty();

            if (!assertCondition)
            {
                Locator caseSensitiveLocator = new Locator(WebLocatorType.CASE_SENSITIVE_TEXT, text);
                elements = searchActions.findElements(getSearchContext(), caseSensitiveLocator);
                assertCondition = !elements.isEmpty();
            }

            if (!assertCondition)
            {
                List<String> pseudoElementsContent = webElementActions.getAllPseudoElementsContent();
                for (String content : pseudoElementsContent)
                {
                    if (content.contains(text))
                    {
                        assertCondition = true;
                        break;
                    }
                }
            }

            if (!assertCondition)
            {
                String pageInnerText = webElementActions.getPageText();
                if (pageInnerText.trim().contains(text))
                {
                    assertCondition = true;
                }
            }

            descriptiveSoftAssert.assertTrue("There is an element with text=" + text + " in the context",
                    assertCondition);
        }
    }

    /**
     * Checks if the <b>text</b> does not exist in context
     * @param text Text value
     * @return <code>true</code> if text does not exist, otherwise <code>false</code>
     */
    @Then("the text '$text' does not exist")
    public boolean textDoesNotExist(String text)
    {
        if (getSearchContext() instanceof WebElement)
        {
            return elementValidations.assertIfElementContainsText(uiContext.getSearchContext(WebElement.class), text,
                    false);
        }
        else
        {
            return baseValidations.assertIfElementDoesNotExist(String.format("An element with text '%s'", text),
                    new Locator(WebLocatorType.CASE_SENSITIVE_TEXT, text));
        }
    }

    /**
     * Checks, if there is a frame with desired attribute on the context
     * @param attributeType Type of tag attribute (for ex. 'name', 'id')
     * @param attributeValue Value of the attribute
     * @return Frame web element if it is found on the page
     */
    @Then("a frame with the attribute '$attributeType'='$attributeValue' exists")
    public WebElement isFrameWithCertainAttributeFound(String attributeType, String attributeValue)
    {
        return baseValidations.assertIfElementExists(
                String.format("A frame with the attribute '%1$s'='%2$s'", attributeType, attributeValue),
                new Locator(WebLocatorType.XPATH,
                        LocatorUtil.getXPathByTagNameAndAttribute("iframe", attributeType, attributeValue)));
    }

    /**
     * Checks, if there is a frame with desired attribute on the context
     * and it has expected <b>state</b>
     * @param state Enabled or Disabled
     * @param attributeType Type of tag attribute (for ex. 'name', 'id')
     * @param attributeValue Value of the attribute
     */
    @Then("a [$state] frame with the attribute '$attributeType'='$attributeValue' exists")
    public void isFrameWithCertainAttributeFound(State state, String attributeType, String attributeValue)
    {
        WebElement element = isFrameWithCertainAttributeFound(attributeType, attributeValue);
        baseValidations.assertElementState("The found frame is " + state, state, element);
    }

    protected SearchContext getSearchContext()
    {
        return uiContext.getSearchContext();
    }
}
