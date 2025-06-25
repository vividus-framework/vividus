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

import java.io.File;
import java.io.IOException;
import java.util.List;

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
import org.vividus.annotation.Replacement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.locator.Locator;
import org.vividus.selenium.manager.WebDriverManager;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.steps.ui.web.validation.IElementValidations;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.util.XpathLocatorUtils;
import org.vividus.ui.web.action.ResourceFileLoader;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.validation.ScrollValidations;

@SuppressWarnings("PMD.CouplingBetweenObjects")
@TakeScreenshotOnFailure
public class ElementSteps implements ResourceLoaderAware
{
    private static final String THE_NUMBER_OF_FOUND_ELEMENTS = "The number of found elements";
    private static final String FILE_EXISTS_MESSAGE_FORMAT = "File %s exists";
    private static final String PARENT_ELEMENT_XPATH = "./..";

    private final IWebDriverProvider webDriverProvider;
    private final WebDriverManager webDriverManager;
    private final IUiContext uiContext;
    private final IDescriptiveSoftAssert descriptiveSoftAssert;
    private final IBaseValidations baseValidations;
    private final IElementValidations elementValidations;
    private final ResourceFileLoader resourceFileLoader;
    private final ScrollValidations<WebElement> scrollValidations;
    private ResourceLoader resourceLoader;

    @SuppressWarnings("checkstyle:paramNum")
    public ElementSteps(IWebDriverProvider webDriverProvider, WebDriverManager webDriverManager, IUiContext uiContext,
                        IDescriptiveSoftAssert descriptiveSoftAssert, IBaseValidations baseValidations,
                        IElementValidations elementValidations, ResourceFileLoader resourceFileLoader,
                        ScrollValidations<WebElement> scrollValidations)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverManager = webDriverManager;
        this.uiContext = uiContext;
        this.descriptiveSoftAssert = descriptiveSoftAssert;
        this.baseValidations = baseValidations;
        this.elementValidations = elementValidations;
        this.resourceFileLoader = resourceFileLoader;
        this.scrollValidations = scrollValidations;
    }

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
     * @deprecated Use step: "When I select element located by `$locator` and upload `$resourceNameOrFilePath`" instead
     */
    @Deprecated(since = "0.6.0", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
            replacementFormatPattern = "When I select element located by `%1$s` and upload `%2$s`")
    @When("I select element located `$locator` and upload file `$filePath`")
    public void uploadFileDeprecated(Locator locator, String filePath) throws IOException
    {
        Resource resource = resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + filePath);
        if (!resource.exists())
        {
            resource = resourceLoader.getResource(ResourceUtils.FILE_URL_PREFIX + filePath);
        }
        File fileForUpload = ResourceUtils.isFileURL(resource.getURL()) ? resource.getFile()
                : unpackFile(resource, filePath);
        if (descriptiveSoftAssert.assertTrue(String.format(FILE_EXISTS_MESSAGE_FORMAT, filePath),
                fileForUpload.exists()))
        {
            String fullFilePath = fileForUpload.getAbsolutePath();
            if (webDriverManager.isRemoteExecution())
            {
                webDriverProvider.getUnwrapped(RemoteWebDriver.class).setFileDetector(new LocalFileDetector());
            }
            locator.getSearchParameters().setVisibility(Visibility.ALL);
            WebElement browse = baseValidations.assertIfElementExists("An element", locator);
            if (browse != null)
            {
                browse.sendKeys(fullFilePath);
            }
        }
    }

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
    @When("I select element located by `$locator` and upload `$resourceNameOrFilePath`")
    public void uploadFile(Locator locator, String filePath) throws IOException
    {
        File fileForUpload = resourceFileLoader.loadFile(filePath);
        if (webDriverManager.isRemoteExecution())
        {
            webDriverProvider.getUnwrapped(RemoteWebDriver.class).setFileDetector(new LocalFileDetector());
        }
        locator.getSearchParameters().setVisibility(Visibility.ALL);
        baseValidations.assertElementExists("A file input element", locator)
                .ifPresent(input -> input.sendKeys(fileForUpload.getAbsolutePath()));
    }

    private File unpackFile(Resource resource, String destination) throws IOException
    {
        File uploadedFile = new File(destination);
        FileUtils.copyInputStreamToFile(resource.getInputStream(), uploadedFile);
        uploadedFile.deleteOnExit();
        return uploadedFile;
    }

    /**
     * Checks that the <b>each element</b> specified by <b>locator</b> contains an exact number
     * of visible <b>child elements</b> specified by <b>locator</b>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds all parent elements matching the '$elementLocator'
     * <li>Finds in each patent element all child element matching the '$childLocator' an verifies their number
     * </ul>
     *
     * @param elementLocator locator for the parent element
     * @param number         The number of child elements (Every positive integer from 0)
     * @param childLocator   locator for the child element
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
     * @param locator The locator for the elements to compare
     * @param dimension Elements dimension to compare. <i>Possible values:</i> <b>width, height</b>.
     * <p><b>Example:</b> <i>Then each element located 'By.xpath(//div[@class='main-menu']//input[@class='menu'])'
     *  has the same 'width'</i></p>
     * @return True if all elements have the same dimension, otherwise false
     */
    @Then("each element located by `$locator` has same `$dimension`")
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
    @Then("context element has width of $widthPercentage%")
    public void isElementHasRightWidth(int widthInPerc)
    {
        uiContext.getSearchContext(WebElement.class).ifPresent(webElement -> {
            Locator bodyLocator = new Locator(WebLocatorType.XPATH,
                    new SearchParameters(XpathLocatorUtils.getXPath("//body"), Visibility.ALL));
            WebElement bodyElement = baseValidations.assertIfElementExists("'Body' element", webDriverProvider.get(),
                    bodyLocator);
            elementValidations.assertIfElementHasWidthInPerc(bodyElement, webElement, widthInPerc);
        });
    }

    /**
     * Checks, if the context element has specified width in percentage
     * @param width Expected element with in percentage
     * @deprecated Use step: "Then context element has width of $widthPercentage% relative to parent element" instead
     */
    @Deprecated(since = "0.6.0", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
            replacementFormatPattern = "Then context element has width of %1$s%% relative to parent element")
    @Then("the context element has a width of '$widthInPerc'% relative to the parent element")
    public void isElementHasWidthRelativeToTheParentElement(int width)
    {
        uiContext.getSearchContext(WebElement.class).ifPresent(elementChild -> {
            Locator parentLocator = new Locator(WebLocatorType.XPATH, PARENT_ELEMENT_XPATH);
            WebElement elementParent = baseValidations.assertIfElementExists("Parent element", parentLocator);
            elementValidations.assertIfElementHasWidthInPerc(elementParent, elementChild, width);
        });
    }

    /**
     * Checks, if the context element has specified width in percentage
     * @param width Expected element with in percentage
     */
    @Then("context element has width of $widthPercentage% relative to parent element")
    public void elementHasWidthRelativeToParentElement(int width)
    {
        uiContext.getSearchContext(WebElement.class).ifPresent(elementChild -> {
            Locator parentLocator = new Locator(WebLocatorType.XPATH, PARENT_ELEMENT_XPATH);
            baseValidations.assertElementExists("The parent element", parentLocator)
                    .ifPresent(elementParent -> elementValidations
                            .assertIfElementHasWidthInPerc(elementParent, elementChild, width));
        });
    }

    /**
     * Checks if the element located by the specified locater is or is not presented in the browser viewport
     *
     * @param locator The locator of the element to check presence in viewport
     * @param presence The presence state of the element, either <b>is</b> or <b>is not</b>
     */
    @Then("element located by `$locator` $presence visible in viewport")
    public void checkElementViewportPresence(Locator locator, ViewportPresence presence)
    {
        baseValidations.assertElementExists("Element to check", locator)
                .ifPresent(e -> scrollValidations.assertElementPositionAgainstViewport(e, presence));
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }
}
