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

import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.validation.IElementValidations;
import org.vividus.bdd.steps.ui.web.validation.IHighlightingSoftAssert;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.ClickResult;
import org.vividus.ui.web.action.IClickActions;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@SuppressWarnings("checkstyle:methodcount")
@TakeScreenshotOnFailure
public class ElementSteps implements ResourceLoaderAware
{
    private static final String AN_ELEMENT_TO_CLICK = "An element to click";
    private static final String THE_FOUND_ELEMENT_IS = "The found element is ";
    private static final String AN_ELEMENT_WITH_THE_NAME = "An element with the name '%1$s'";
    private static final String AN_ELEMENT = "An element";
    private static final String THE_NUMBER_OF_FOUND_ELEMENTS = "The number of found elements";

    @Inject private IWebElementActions webElementActions;
    @Inject private IHighlightingSoftAssert highlightingSoftAssert;
    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IBaseValidations baseValidations;
    @Inject private IWebUiContext webUiContext;
    @Inject private IElementValidations elementValidations;
    @Inject private IClickActions clickActions;
    @Inject private IMouseActions mouseActions;
    private ResourceLoader resourceLoader;

    /**
     * This steps is uploading the file with the given relative path
     * <p>A <b>relative path</b> - starts from some given working directory,
     * avoiding the need to provide the full absolute path
     * (i.e. <i>'about.jpeg'</i> is in the root directory
     * or <i>'/story/uploadfiles/about.png'</i>)</p>
     * @param attributeType A type of the element's attribute
     * @param attributeValue A value of the element's attribute

     * @param filePath relative path to the file to be uploaded
     * @see <a href="https://en.wikipedia.org/wiki/Path_(computing)#Absolute_and_relative_paths"> <i>Absolute and
     * relative paths</i></a>
     * @throws IOException If an input or output exception occurred
     */
    @When("I select an element with the '$attributeType'='$attributeValue' and upload the file '$filePath'")
    public void uploadFile(String attributeType, String attributeValue, String filePath) throws IOException
    {
        Resource resource = resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + filePath);
        if (!resource.exists())
        {
            resource = resourceLoader.getResource(ResourceUtils.FILE_URL_PREFIX + filePath);
        }
        File fileForUpload = ResourceUtils.isFileURL(resource.getURL()) ? resource.getFile()
                : unpackFile(resource, filePath);
        if (highlightingSoftAssert.assertTrue("File " + filePath + " exists", fileForUpload.exists()))
        {
            String fullFilePath = fileForUpload.getAbsolutePath();
            if (isRemoteExecution())
            {
                webDriverProvider.getUnwrapped(RemoteWebDriver.class).setFileDetector(new LocalFileDetector());
            }
            WebElement browse = baseValidations.assertIfElementExists(AN_ELEMENT,
                    new SearchAttributes(ActionAttributeType.XPATH,
                            new SearchParameters(LocatorUtil.getXPathByAttribute(attributeType, attributeValue))
                                    .setVisibility(Visibility.ALL)));
            if (browse != null)
            {
                browse.sendKeys(fullFilePath);
            }
        }
    }

    private File unpackFile(Resource resource, String destination) throws IOException
    {
        File uploadedFile = new File(destination);
        FileUtils.copyInputStreamToFile(resource.getInputStream(), uploadedFile);
        uploadedFile.deleteOnExit();
        return uploadedFile;
    }

    /**
     * Clicks on an element by the xpath
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds the element by the given xpath;</li>
     * <li>Clicks on it.</li>
     * </ul>
     * @param xpath Xpath selector of the element
    */
    @When("I click on an element by the xpath '$xpath'")
    public void clickElementByXpath(String xpath)
    {
        WebElement element = baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, new SearchAttributes(
                ActionAttributeType.XPATH, xpath));
        clickActions.click(element);
    }

    /**
     * Clicks on all elements by the xpath.
     * Click on any of elements by xpath shouldn't lead to current page changing.
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds the element by the given xpath;</li>
     * <li>Clicks on found elements.</li>
     * </ul>
     * @param xpath selector of the elements to click
    */
    @When("I click on all elements by xpath '$xpath'")
    public void clickEachElementByXpath(String xpath)
    {
        baseValidations.assertIfElementsExist("The elements to click", new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath(xpath))).forEach(e -> clickActions.click(e));
    }

    /**
     * Clicks on any item with the corresponding text.
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds the element;</li>
     * <li>Clicks on it.</li>
     * </ul>
     * @param text A text value of the element
     */
    @When("I click on an element with the text '$text'")
    public void clickElementByText(String text)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.CASE_SENSITIVE_TEXT, text);
        WebElement element = baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, attributes);
        clickActions.click(element);
    }

    /**
     * Clicks on the element with a desired attribute
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds an element on the page;</li>
     * <li>Clicks on the element.</li>
     * </ul>
     * @param attributeType A type of the attribute (for ex. 'name', 'id')
     * @param attributeValue A value of the attribute
     */
    @When("I click on an element with the attribute '$attributeType'='$attributeValue'")
    public void clickElementWithCertanAttribute(String attributeType, String attributeValue)
    {
        WebElement element = baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK,
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPathByAttribute(attributeType, attributeValue)));
        clickActions.click(element);
    }

    /**
     * Clicks on the element with the provided search attributes and verify that page has not been reloaded
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Assert that element with specified search attributes is found on the page;</li>
     * <li>Clicks on the element;</li>
     * <li>Assert that page has not been refreshed after click</li>
     * </ul>
     * @param searchAttributes to locate element
     */
    @When("I click on an element '$searchAttributes' then the page does not refresh")
    public void clickElementPageNotRefresh(SearchAttributes searchAttributes)
    {
        WebElement element = baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, searchAttributes);
        ClickResult clickResult = clickActions.click(element);
        highlightingSoftAssert.assertTrue(
                "Page has not been refreshed after clicking on the element located by" + searchAttributes,
                !clickResult.isNewPageLoaded());
    }

    /**
     * Performs right click on an element by the xpath
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds the element by the given xpath;</li>
     * <li>Performs context click on the element.</li>
     * </ul>
     * @param xpath Xpath selector of the element
     */
    @When("I perform right click on an element by the xpath '$xpath'")
    public void contextClickElementByXpath(String xpath)
    {
        WebElement element = baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK,
                new SearchAttributes(ActionAttributeType.XPATH, xpath));
        mouseActions.contextClick(element);
    }

    /**
     * Sets a cursor on the <b>element</b> specified by the 'xpath' value
     * @param xpath that specified the element
     */
    @When("I hover a mouse over an element with the xpath '$xpath'")
    public void hoverMouseOverAnElementByXpaht(String xpath)
    {
        WebElement element = doesElementByXpathExist(xpath);
        mouseActions.moveToElement(element);
    }

    /**
     * Checks that an <b>element</b> with the specified <b>name</b> exists in the context
     * @param elementName Any attribute or text value of the element
     * @return <b>WebElement</b> An element matching the requirements,
     * <b> null</b> - if there are no desired elements
     */
    @Then("an element with the name '$elementName' exists")
    public WebElement ifElementWithNameExists(String elementName)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, elementName);
        return baseValidations.assertIfElementExists(String.format(AN_ELEMENT_WITH_THE_NAME, elementName),
                attributes);
    }

    /**
     * Checks that an <b>element</b> with the specified <b>name</b> exists in the context
     * and it has expected <b>state</b>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param elementName Any attribute or text value of the element
     */
    @Then("a [$state] element with the name '$elementName' exists")
    public void ifElementWithNameExists(State state, String elementName)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, elementName);
        attributes.addFilter(ActionAttributeType.STATE, state.toString());
        baseValidations.assertIfElementExists(String.format("A %s element with the name '%s", state, elementName),
                attributes);
    }

    /**
     * Checks that a visible <b>element</b> specified by any attribute or text
     * value does not exist in the context
     * @param elementName Any attribute or <b>text value</b> of the element
     */
    @Then("an element with the name '$elementName' does not exist")
    public void doesNotContainElementExist(String elementName)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, elementName);
        baseValidations.assertIfElementDoesNotExist(String.format(AN_ELEMENT_WITH_THE_NAME, elementName), attributes);
    }

    /**
     * Checks, that there is <b>at least one</b> element with a certain attribute in the context
     * @param attributeType A type of the attribute (for ex. <i>'name', 'id'</i>)
     * @param attributeValue A value of the attribute
     * @return <b>WebElement</b> - a <i>first</i> element that has an expected attribute,
     * <b> null</b> - if there are no expected elements.
     */
    @Then("at least one element with the attribute '$attributeType'='$attributeValue' exists")
    public WebElement atLeastOneElementWithAttributeExists(String attributeType, String attributeValue)
    {
        return baseValidations.assertIfAtLeastOneElementExists(
                String.format("The number of elements with the attribute '%1$s'='%2$s'", attributeType, attributeValue),
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPathByAttribute(attributeType, attributeValue)));
    }

    /**
     * Checks that the <b>page</b> contains an <b>element</b> specified by <b>XPath</b>
     * @param xpath A XPath locator for an expected element
     * @return <b>WebElement</b> An element matching the requirements,
     * <b> null</b> - if there are no desired elements
    */
    @Then("an element by the xpath '$xpath' exists")
    public WebElement doesElementByXpathExist(String xpath)
    {
        return baseValidations.assertIfElementExists(String.format("An element with the locator '%1$s'", xpath),
                new SearchAttributes(ActionAttributeType.XPATH, xpath));
    }

    /**
     * Checks that the <b>page</b> contains an <b>element</b> specified by <b>CSS selector</b>
     * @param cssSelector A CSS selector for an expected element
     * @return <b>WebElement</b> An element matching the requirements,
     * <b> null</b> - if there are no desired elements
    */
    @Then("an element by the cssSelector '$cssSelector' exists")
    public WebElement doesElementByCssSelectorExist(String cssSelector)
    {
        return baseValidations.assertIfElementExists(String.format("An element with the selector '%1$s'", cssSelector),
                new SearchAttributes(ActionAttributeType.CSS_SELECTOR, cssSelector));
    }

    /**
     * Checks that the <b>page</b> contains at least 1 <b>element</b> specified by <b>XPath</b>
     * @param xpath A XPath locator for an expected element
    */
    @Then("at least one element by the xpath '$xpath' exists")
    public void doesAtLeastOneELementByXpathExist(String xpath)
    {
        baseValidations.assertIfAtLeastOneElementExists(
                String.format("The number of elemants by the xpath: '%1$s'", xpath),
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(xpath)));
    }

    /**
     * Checks that the <b>each element</b> specified by <b>XPath</b> contains an exact number
     * of visible <b>child elements</b> specified by <b>XPath</b>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds all parent elements matching the 'elementXpath'
     * <li>Finds in each patent element all child element matching the 'childXpath' an verifies their amount
     * </ul>
     * @param elementXpath A XPath locator for the parent element
     * @param number An amount of child elements (Every positive integer from 0)
     * @param childXpath A XPath locator for the child element (e.g div[@class='className'])
    */
    @Then("each element with the xpath '$elementXpath' has '$number' child elements with the xpath '$childXpath'")
    public void doesEachElementByXpathHasChildWithTheXpath(String elementXpath, int number, String childXpath)
    {
        List<WebElement> elements = baseValidations.assertIfElementsExist("The number of parent elements",
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(elementXpath)));
        for (WebElement element : elements)
        {
            SearchAttributes childSearchAttributes = new SearchAttributes(ActionAttributeType.XPATH,
                    LocatorUtil.getXPath(childXpath));
            baseValidations.assertIfExactNumberOfElementsFound("Parent element has number of child elements which",
                    element, childSearchAttributes, number);
        }
    }

    /**
     * Checks that the context <b>element</b> has an expected <b>CSS property</b>
     * @param cssName A name of the <b>CSS property</b>
     * @param cssValue An expected value of <b>CSS property</b>
    */
    @Then("the context element has the CSS property '$cssName'='$cssValue'")
    public void doesElementHaveRightCss(String cssName, String cssValue)
    {
        WebElement element = webUiContext.getSearchContext(WebElement.class);
        String actualCssValue = webElementActions.getCssValue(element, cssName);
        highlightingSoftAssert.assertEquals("Element has correct css property value", cssValue,
                actualCssValue);
    }

    /**
     * Checks that the context <b>element</b> has an expected <b>CSS property</b> part
     * @param cssName A name of the <b>CSS property</b>
     * @param cssValue An expected value part of <b>CSS property</b>
    */
    @Then("the context element has the CSS property '$cssName' containing '$cssValue'")
    public void doesElementHaveRightPartOfCssValue(String cssName, String cssValue)
    {
        WebElement element = webUiContext.getSearchContext(WebElement.class);
        String actualCssValue = webElementActions.getCssValue(element, cssName);
        highlightingSoftAssert.assertThat("Css property value part is correct",
                String.format("Element has CSS property '%1$s' containing value '%2$s'", cssName, cssValue),
                actualCssValue, containsString(cssValue));
    }

    /**
     * Checks whether the context contains
     * exact amount of elements by locator
     * @param searchAttributes to locate element
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> LESS_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL_TO,
     * EQUAL_TO</b></i>)
     * @param quantity desired amount of elements
     */
    @Then("number of elements found by `$locator` is $comparisonRule `$quantity`")
    public void thePageContainsQuatityElements(SearchAttributes searchAttributes, ComparisonRule comparisonRule,
            int quantity)
    {
        baseValidations.assertIfNumberOfElementsFound(THE_NUMBER_OF_FOUND_ELEMENTS, searchAttributes, quantity,
                comparisonRule);
    }

    /**
     * Checks that all elements found on the page by the given <b>xpath expression</b> have the same <i>dimension</i>:
     * <b>width / height.</b>
     * <p>
     * Elements' dimension is measured in <b>pixels</b> and means the size that given element occupies on the web-page
     * in a browser. Element size may vary in different browsers, it also depends on screen resolution, page scaling,
     * scripts running on the page.</p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Checks if there are at least 2 visible elements with the specified xpath on the page. If less than 2 elements
     * found, step will produce failed assertion;
     * <li>Reads specified dimension (width / height) for each found element;
     * <li>Compares dimensions of 2nd, 3rd etc. elements with the dimension of the first element. So, first element is
     * taken as an sample;
     * <li>Asserts and logs each comparison.
     * </ul>
     * @param xpath Valid xPath expression
     * @param dimension Elements dimension to compare. <i>Possible values:</i> <b>width, height</b>.
     * <p><b>Example:</b> <i>Then each element by the xpath '//div[@class='main-menu']//input[@class='menu']'
     *  has the same 'width'</i></p>
     * @return True if all elements have the same dimension, otherwise false
     */
    @Then("each element by the xpath '$xpath' has the same '$dimension'")
    public boolean doesEachElementByXpathHaveSameDimension(String xpath, Dimension dimension)
    {
        int minimalElementsQuantity = 2;
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, xpath);
        List<WebElement> elements = baseValidations.assertIfAtLeastNumberOfElementsExist(THE_NUMBER_OF_FOUND_ELEMENTS,
                searchAttributes, minimalElementsQuantity);
        return elements.size() >= minimalElementsQuantity
                && elementValidations.assertAllWebElementsHaveEqualDimension(elements, dimension);
    }

    /**
     * Checks, if there is element with desired tag and partial value of the attribute
     * in the context
     * @param attributeType Type of tag attribute (for ex. 'name', 'id')
     * @param attributeValue Partial value of the attribute
     * @return <b>WebElement</b> An element matching the requirements,
     * <b> null</b> - if there are no desired elements
     */
    @Then("an element with the attribute '$attributeType' containing '$attributeValue' exists")
    public WebElement isElemenWithPartialAttributetFound(String attributeType, String attributeValue)
    {
        return baseValidations.assertIfElementExists(AN_ELEMENT, new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath(String.format(".//*[contains(@%s,%%s)]", attributeType), attributeValue)));
    }

    /**
     * Checks, that there is <b>exactly one</b> element with certain <b>tag</b> and attribute in the context
     * @param elementTag Type of html tag (for ex. <i>'div', 'img', 'span'</i>)
     * @param attributeType Type of attribute (for ex. <i>'name', 'id', 'title'</i>)
     * @param attributeValue Value of the attribute
     * @return <b>WebElement</b> An element matching the requirements,
     * <b> null</b> - if there are no desired elements
     */
    @Then(value = "an element with the tag '$elementTag' and attribute '$attributeType'='$attributeValue' exists",
            priority = 1)
    public WebElement isCertainElementFound(String elementTag, String attributeType, String attributeValue)
    {
        return baseValidations.assertIfElementExists(AN_ELEMENT, new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPathByTagNameAndAttribute(elementTag, attributeType, attributeValue)));
    }

    /**
     * Checks that there is exactly one <b>element</b> with an expected <b>attribute value</b>
     * within the search context
     * @param attributeType A type of the element's attribute
     * @param attributeValue A value of the element's attribute
     * @return <b>WebElement</b> An element matching the requirements,
     * <b> null</b> - if there are no desired elements
    */
    @Then("an element with the attribute '$attributeType'='$attributeValue' exists")
    public WebElement isElementWithCertainAttributeFound(String attributeType, String attributeValue)
    {
        return baseValidations.assertIfElementExists(AN_ELEMENT, new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPathByAttribute(attributeType, attributeValue)));
    }

    /**
     * Checks that there is exactly one <b>element</b> with an expected <b>attribute value</b>
     * within the search context
     * @param state Enabled or Disabled
     * @param attributeType attributeType A type of the element's attribute
     * @param attributeValue attributeValue A value of the element's attribute
     */
    @Then("a [$state] element with the attribute '$attributeType'='$attributeValue' exists")
    public void isElementWithCertainAttributeAndStateFound(State state, String attributeType, String attributeValue)
    {
        WebElement webElement = isElementWithCertainAttributeFound(attributeType, attributeValue);
        baseValidations.assertElementState(THE_FOUND_ELEMENT_IS + state, state, webElement);
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
            String text = row.valueAs("radioOption", String.class);
            WebElement radioButtonLabel = baseValidations.assertIfElementExists(
                    String.format("A radio button label with text '%1$s'", text),
                    new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(ElementPattern.LABEL_PATTERN,
                            text)));
            if (radioButtonLabel != null)
            {
                baseValidations.assertIfElementExists("Radio button", new SearchAttributes(ActionAttributeType.XPATH,
                    LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN,
                            radioButtonLabel.getAttribute("for"))));
            }
        }
    }

    /**
     * Checks that the context <b>element</b>
     * has an expected <b>width in a percentage</b> (from style attribute)
     * @param widthInPerc An expected width of the element in a percentage
    */
    @Then("the context has a width of '$widthInPerc'%")
    public void isElementHasRightWidth(int widthInPerc)
    {
        WebElement webElement = webUiContext.getSearchContext(WebElement.class);
        WebElement bodyElement = baseValidations.assertIfElementExists("'Body' element", webDriverProvider.get(),
                new SearchAttributes(ActionAttributeType.XPATH, new SearchParameters(LocatorUtil.getXPath("//body"))
                        .setVisibility(Visibility.ALL)));
        elementValidations.assertIfElementHasWidthInPerc(bodyElement, webElement, widthInPerc);
    }

    /**
     * Checks that an element with the expected <b>tag</b> and <b>text</b> exists
     * <p>
     * @param elementTag Name of element's tag
     * @param text Expected text of the element
     * @return <b>WebElement</b> An element matching the requirements,
     * <b> null</b> - if there are no desired elements
     */
    @Then("an element with the tag '$elementTag' and text '$text' exists")
    public WebElement isElementWithTagAndTextFound(String elementTag, String text)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.TAG_NAME, elementTag);
        attributes.addFilter(ActionAttributeType.CASE_SENSITIVE_TEXT, text);
        return baseValidations.assertIfElementExists(
                String.format("An element with the tag '%1$s' and text '%2$s'", elementTag, text), attributes);
    }

    /**
     * Checks, if the context element has specified width in percentage
     * @param width Expected element with in percentage
     */
    @Then("the context element has a width of '$widthInPerc'% relative to the parent element")
    public void isElementHasWidthRelativeToTheParentElement(int width)
    {
        WebElement elementChild = webUiContext.getSearchContext(WebElement.class);
        WebElement elementParent = baseValidations.assertIfElementExists("Parent element",
                new SearchAttributes(ActionAttributeType.XPATH, "./.."));
        elementValidations.assertIfElementHasWidthInPerc(elementParent, elementChild, width);
    }

    /**
     * Checks that an <b>element</b> with the specified <b>name</b> and <b>text</b> exists in the context
     * and has desired <b>state</b>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Verifies that there is <b>exactly one</b> element a with desired attributes
     * </ul>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param elementName Value of any attribute of a tag
     * @param text Expected text of the element
     * @return <b>WebElement</b> An element (field) matching the requirements,
     * <b> null</b> - if there are no desired elements
     */
    @Then(value = "a [$state] element with the name '$elementName' and text '$text' exists", priority = 1)
    public WebElement isElementWithNameAndTextFound(State state, String elementName, String text)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, elementName);
        attributes.addFilter(ActionAttributeType.STATE, state.toString());
        attributes.addFilter(ActionAttributeType.CASE_SENSITIVE_TEXT, text);
        return baseValidations.assertIfElementExists(
                String.format("A %1$s element with the name '%2$s' and text '%3$s'", state, elementName, text),
                attributes);
    }

    /**
     * Checks that an <b>element</b> with the specified <b>name</b> and <b>text</b> exists in the context
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Verifies that there is <b>exactly one</b> desired element a with desired attributes
     * </ul>
     * <p>
     * @param elementName Value of any attribute of a tag
     * @param text Expected text of the field
     * @return <b>WebElement</b> An element (field) matching the requirements,
     * <b> null</b> - if there are no desired elements
     */
    @Then(value = "an element with the name '$elementName' and text '$text' exists", priority = 1)
    public WebElement isElementWithNameAndTextFound(String elementName, String text)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, elementName);
        attributes.addFilter(ActionAttributeType.CASE_SENSITIVE_TEXT, text);
        return baseValidations.assertIfElementExists(
                String.format("An element with the name '%1$s' and text '%2$s'", elementName, text), attributes);
    }

    /**
     * Checks that an <b>element</b> with the specified <b>name</b> containing  <b>text</b> exists in the context
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Verifies that there is <b>exactly one</b> element with desired attributes</li>
     * </ul>
     * @param elementName Any attribute or text(exact) value
     * @param text part that is contained in element
     * @return <b>WebElement</b> An element (field) matching the requirements,
     * <b> null</b> - if there are no desired elements
     */
    @Then(value = "an element with the name '$elementName' containing text '$text' exists", priority = 1)
    public WebElement doesElementWithNameContainingTextExist(String elementName, String text)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, elementName);
        attributes.addFilter(ActionAttributeType.TEXT_PART, text);
        return baseValidations.assertIfElementExists(
                String.format("An element with the name '%1$s' containing text '%2$s'", elementName, text),
                attributes);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    private boolean isRemoteExecution()
    {
        return webDriverProvider.isRemoteExecution();
    }
}
