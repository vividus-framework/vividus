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

package org.vividus.steps.ui.web;

import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;
import org.vividus.monitor.TakeScreenshotOnFailure;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.steps.ui.web.validation.IElementValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.ClickResult;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.LocatorUtil;

@TakeScreenshotOnFailure
public class ElementSteps implements ResourceLoaderAware
{
    private static final String AN_ELEMENT_TO_CLICK = "An element to click";
    private static final String AN_ELEMENT_WITH_ATTRIBUTES = "An element with attributes%1$s";
    private static final String AN_ELEMENT = "An element";
    private static final String THE_NUMBER_OF_FOUND_ELEMENTS = "The number of found elements";

    @Inject private IWebElementActions webElementActions;
    @Inject private IDescriptiveSoftAssert descriptiveSoftAssert;
    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IBaseValidations baseValidations;
    @Inject private IUiContext uiContext;
    @Inject private IElementValidations elementValidations;
    @Inject private IMouseActions mouseActions;
    private ResourceLoader resourceLoader;

    /**
     * This step uploads a file with the given relative path
     * <p>A <b>relative path</b> starts from some given working directory,
     * avoiding the need to provide the full absolute path
     * (i.e. <i>'about.jpeg'</i> is in the root directory
     * or <i>'/story/uploadfiles/about.png'</i>)</p>
     * @param locator The locator for the upload element
     * @param filePath relative path to the file to be uploaded
     * @see <a href="https://en.wikipedia.org/wiki/Path_(computing)#Absolute_and_relative_paths"> <i>Absolute and
     * relative paths</i></a>
     * @throws IOException If an input or output exception occurred
     */
    @When("I select element located `$locator` and upload file `$filePath`")
    public void uploadFile(Locator locator, String filePath) throws IOException
    {
        Resource resource = resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + filePath);
        if (!resource.exists())
        {
            resource = resourceLoader.getResource(ResourceUtils.FILE_URL_PREFIX + filePath);
        }
        File fileForUpload = ResourceUtils.isFileURL(resource.getURL()) ? resource.getFile()
                : unpackFile(resource, filePath);
        if (descriptiveSoftAssert.assertTrue("File " + filePath + " exists", fileForUpload.exists()))
        {
            String fullFilePath = fileForUpload.getAbsolutePath();
            if (isRemoteExecution())
            {
                webDriverProvider.getUnwrapped(RemoteWebDriver.class).setFileDetector(new LocalFileDetector());
            }
            locator.getSearchParameters().setVisibility(Visibility.ALL);
            WebElement browse = baseValidations.assertIfElementExists(AN_ELEMENT, locator);
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
     * Clicks on the element with the provided search attributes and verify that page has not been reloaded
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Assert that element with specified search attributes is found on the page;</li>
     * <li>Clicks on the element;</li>
     * <li>Assert that page has not been refreshed after click</li>
     * </ul>
     * @param locator to locate element
     */
    @When("I click on an element '$locator' then the page does not refresh")
    public void clickElementPageNotRefresh(Locator locator)
    {
        WebElement element = baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, locator);
        ClickResult clickResult = mouseActions.click(element);
        descriptiveSoftAssert.assertTrue(
                "Page has not been refreshed after clicking on the element located by" + locator,
                !clickResult.isNewPageLoaded());
    }

    /**
     * Performs right click on an element by locator
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds the element by the given locator;</li>
     * <li>Performs context click on the element.</li>
     * </ul>
     * @param locator to locate the element
     */
    @When("I perform right click on element located `$locator`")
    public void contextClickElementByLocator(Locator locator)
    {
        WebElement element = baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, locator);
        mouseActions.contextClick(element);
    }

    /**
     * Sets a cursor on the <b>element</b> specified by locator
     * @param locator to locate the element
     */
    @When("I hover mouse over element located `$locator`")
    public void hoverMouseOverElement(Locator locator)
    {
        WebElement element = baseValidations.assertIfElementExists(
                String.format(AN_ELEMENT_WITH_ATTRIBUTES, locator), locator);
        mouseActions.moveToElement(element);
    }

    /**
     * Checks that the <b>each element</b> specified by <b>XPath</b> contains an exact number
     * of visible <b>child elements</b> specified by <b>XPath</b>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds all parent elements matching the 'elementXpath'
     * <li>Finds in each patent element all child element matching the 'childXpath' an verifies their amount
     * </ul>
     * @param elementLocator locator for the parent element
     * @param number An amount of child elements (Every positive integer from 0)
     * @param childLocator locator for the child element
    */
    @Then("each element with locator `$elementLocator` has `$number` child elements with locator `$childLocator`")
    public void doesEachElementByLocatorHaveChildWithLocator(Locator elementLocator, int number,
            Locator childLocator)
    {
        List<WebElement> elements = baseValidations.assertIfElementsExist("The number of parent elements",
                elementLocator);
        for (WebElement element : elements)
        {
            baseValidations.assertIfExactNumberOfElementsFound("Parent element has number of child elements which",
                    element, childLocator, number);
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
        uiContext.getSearchContext(WebElement.class)
                .map(element -> webElementActions.getCssValue(element, cssName))
                .ifPresent(actualCssValue -> descriptiveSoftAssert.assertEquals(
                        "Element has correct css property value", cssValue, actualCssValue));
    }

    /**
     * Checks that the context <b>element</b> has an expected <b>CSS property</b> part
     * @param cssName A name of the <b>CSS property</b>
     * @param cssValue An expected value part of <b>CSS property</b>
    */
    @Then("the context element has the CSS property '$cssName' containing '$cssValue'")
    public void doesElementHaveRightPartOfCssValue(String cssName, String cssValue)
    {
        uiContext.getSearchContext(WebElement.class)
                .map(element -> webElementActions.getCssValue(element, cssName))
                .ifPresent(actualCssValue -> descriptiveSoftAssert.assertThat("Css property value part is correct",
                        String.format("Element has CSS property '%1$s' containing value '%2$s'", cssName, cssValue),
                        actualCssValue, containsString(cssValue)));
    }

    /**
     * Checks that all elements found on the page by the given <b>locator</b> have the same <i>dimension</i>:
     * <b>width / height.</b>
     * <p>
     * Elements' dimension is measured in <b>pixels</b> and means the size that given element occupies on the web-page
     * in a browser. Element size may vary in different browsers, it also depends on screen resolution, page scaling,
     * scripts running on the page.</p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Checks if there are at least 2 visible elements with the specified locator on the page. If less than 2
     * elements found, step will produce failed assertion;
     * <li>Reads specified dimension (width / height) for each found element;
     * <li>Compares dimensions of 2nd, 3rd etc. elements with the dimension of the first element. So, first element is
     * taken as an sample;
     * <li>Asserts and logs each comparison.
     * </ul>
     * @param locator to locate element
     * @param dimension Elements dimension to compare. <i>Possible values:</i> <b>width, height</b>.
     * <p><b>Example:</b> <i>Then each element located 'By.xpath(//div[@class='main-menu']//input[@class='menu'])'
     *  has the same 'width'</i></p>
     * @return True if all elements have the same dimension, otherwise false
     */
    @Then("each element located `$locator` has same `$dimension`")
    public boolean doesEachElementByLocatorHaveSameDimension(Locator locator, Dimension dimension)
    {
        int minimalElementsQuantity = 2;
        List<WebElement> elements = baseValidations.assertIfAtLeastNumberOfElementsExist(THE_NUMBER_OF_FOUND_ELEMENTS,
                locator, minimalElementsQuantity);
        return elements.size() >= minimalElementsQuantity
                && elementValidations.assertAllWebElementsHaveEqualDimension(elements, dimension);
    }

    /**
     * Checks that the context <b>element</b>
     * has an expected <b>width in a percentage</b> (from style attribute)
     * @param widthInPerc An expected width of the element in a percentage
    */
    @Then("the context has a width of '$widthInPerc'%")
    public void isElementHasRightWidth(int widthInPerc)
    {
        uiContext.getSearchContext(WebElement.class).ifPresent(webElement -> {
            Locator bodyLocator = new Locator(WebLocatorType.XPATH,
                    new SearchParameters(LocatorUtil.getXPath("//body"), Visibility.ALL));
            WebElement bodyElement = baseValidations.assertIfElementExists("'Body' element", webDriverProvider.get(),
                    bodyLocator);
            elementValidations.assertIfElementHasWidthInPerc(bodyElement, webElement, widthInPerc);
        });
    }

    /**
     * Checks, if the context element has specified width in percentage
     * @param width Expected element with in percentage
     */
    @Then("the context element has a width of '$widthInPerc'% relative to the parent element")
    public void isElementHasWidthRelativeToTheParentElement(int width)
    {
        uiContext.getSearchContext(WebElement.class).ifPresent(elementChild -> {
            Locator parentLocator = new Locator(WebLocatorType.XPATH, "./..");
            WebElement elementParent = baseValidations.assertIfElementExists("Parent element", parentLocator);
            elementValidations.assertIfElementHasWidthInPerc(elementParent, elementChild, width);
        });
    }

    /**
     * Clicks on <b>element</b> located by <b>locator</b>
     * @param locator to locate element
     */
    @When("I click on element located `$locator`")
    public void clickOnElement(Locator locator)
    {
        WebElement webElement =
                baseValidations.assertIfElementExists(String.format(AN_ELEMENT_WITH_ATTRIBUTES, locator), locator);
        mouseActions.click(webElement);
    }

    /**
     * Clicks on <b>elements</b> located by <b>locator</b>
     * @param locator to locate elements
     */
    @When("I click on all elements located `$locator`")
    public void clickOnAllElements(Locator locator)
    {
        baseValidations.assertIfElementsExist("The elements to click", locator).forEach(mouseActions::click);
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
