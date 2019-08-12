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

import java.util.List;
import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.validation.IElementValidations;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.IClickActions;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@TakeScreenshotOnFailure
public class ImageSteps
{
    private static final String THE_FOUND_IMAGE_IS = "The found image is ";
    private static final String AN_IMAGE_WITH_THE_SRC_PATTERN = "An image with the src '%s'";
    private static final String AN_IMAGE_WITH_THE_SRC_PART_PATTERN = "An image with the src part '%s'";

    @Inject private ISearchActions searchActions;
    @Inject private IBaseValidations baseValidations;
    @Inject private IElementValidations elementValidations;
    @Inject private IWebUiContext webUiContext;
    @Inject private IClickActions clickActions;
    @Inject private IMouseActions mouseActions;

    /**
     * Sets a cursor on the <b>image</b> specified by the 'src' attribute value
     * <p>
     * The 'src' attribute specifies the URL of the <b>image</b>.
     * An <b>image</b> is defined with the <i>{@literal <img>}</i> tag.
     * <br>
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like src="http://www.example.com/image.gif")
     * <li>A relative URL - points to a file within a web site (like src="image.gif")
     * </ul>
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the <b>image</b>
     * <li>Sets cursor on it
     * </ul>
     * @param src An image's 'src' attribute value
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I hover a mouse over an image with the src '$src'")
    public void mouseOverLinkImage(String src)
    {
        WebElement image = ifImageWithSrcExists(src);
        mouseActions.moveToElement(image);
    }

    /**
     * Sets a cursor on the <b>image</b> specified by the <b>tooltip</b>
     * <p>
     * A 'tooltip' can be the value of a 'title' or an 'alt' attributes of the <b>image</b>.<br>
     * An <b>image</b> is defined with the <i>{@literal <img>}</i> tag.
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds the <b>image</b>
     * <li>Sets a cursor on it
     * </ul>
     * @param tooltipImage A 'title' or an 'alt' attribute value of the link
     * <p>
     * <b>Example:</b>
     * <pre>
     * {@code
     * <img src="example1.gif" alt=}<b>'tooltipImage'</b>{@code ><img src="example2.gif" title=}
     * <b>'tooltipImage'</b>{@code >}</pre>
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I hover a mouse over an image with the tooltip '$tooltipImage'")
    public void mouseOverLinkImageTooltip(String tooltipImage)
    {
        WebElement image = ifImageWithTooltipExists(tooltipImage);
        mouseActions.moveToElement(image);
    }

    /**
     * Clicks on an image with the {@literal <src>} attribute in the search context
     * <p>
     * @param src Image 'src' attribute value
     */
    @When("I click on an image with the src '$src'")
    public void clickImageWithSrc(String src)
    {
        WebElement imageLabel = ifImageWithSrcExists(src);
        clickActions.click(imageLabel);
    }

    /**
     * Clicks on an image with the <b>name</b> in the search context.
     * <p>
     * @param imageName Any <b>attribute value</b> of the image <b>element</b>
     */
    @When("I click on an image with the name '$imageName'")
    public void clickImageWithName(String imageName)
    {
        By imageLocator = LocatorUtil.getXPathLocator(".//img[@*='%1$s' or text()='%1$s']", imageName);
        List<WebElement> images = searchActions.findElements(webUiContext.getSearchContext(), imageLocator);
        if (!images.isEmpty())
        {
            clickActions.click(images.get(0));
        }
        else
        {
            WebElement imageLabel = baseValidations.assertIfElementExists("An image label to click",
                    new SearchAttributes(ActionAttributeType.XPATH,
                            LocatorUtil.getXPath(ElementPattern.LABEL_PATTERN, imageName)));
            clickActions.click(imageLabel);
        }
    }

    /**
     * Checks that an <b>image</b> with the specified <b>src</b> exists
     * @param src A 'href' attribute value of the image
     * @return <b>WebElement</b> An element (image) matching the requirements <br>
     * <b>null</b> - if there are no desired elements
     */
    @Then("an image with the src '$src' exists")
    public WebElement ifImageWithSrcExists(String src)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.IMAGE_SRC, src);
        return baseValidations.assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PATTERN, src), attributes);
    }

    /**
     * Checks that an <b>image</b> with the specified <b>src</b> exists
     * and it has expected <b>state</b>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param src A 'href' attribute value of the image
     * @return <b>Web element</b> - An image that meets the requirements;
     */
    @Then("a [$state] image with the src '$src' exists")
    public WebElement ifStateImageWithSrcExists(State state, String src)
    {
        WebElement image = ifImageWithSrcExists(src);
        baseValidations.assertElementState(THE_FOUND_IMAGE_IS + state, state, image);
        return image;
    }

    /**
     * Checks that an <b>image</b> with the specified <b>src</b> does not exist in context
     * @param src A 'href' attribute value of the image
     */
    @Then("an image with the src '$src' does not exist")
    public void doesNotImageWithSrcExist(String src)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.IMAGE_SRC, src);
        baseValidations.assertIfElementDoesNotExist(String.format(AN_IMAGE_WITH_THE_SRC_PATTERN, src), attributes);
    }

    /**
     * Checks that an <b>image</b> with the specified <b>src</b> part exists
     * @param srcpart Any part of 'href' attribute value of the image
     * @return <b>WebElement</b> An element (image) matching the requirements <br>
     * <b>null</b> - if there are no desired elements
     */
    @Then("an image with the src containing '$srcpart' exists")
    public WebElement ifImageWithSrcPartExists(String srcpart)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.IMAGE_SRC_PART, srcpart);
        return baseValidations.assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PART_PATTERN, srcpart),
                attributes);
    }

    /**
     * Checks, that there is an <b>image</b> that contains a partial value of the <i>'src'</i> attribute
     * and has the right <b>tooltip</b> <br>
     * A <b>tooltip</b> is a small hint that appears when the user hovers a mouse over the image. <br>
     * An <b>image</b> is defined with the <i>{@literal <img>}</i> tag.
     * <p>
     * @param tooltip A <i>'title'</i> or an <i>'alt'</i> attribute value of the image
     * @param srcpart A partial 'src' attribute value of an image element;
     * @return <b>Web element</b> - An image that meets the requirements; <br>
     * <b>null</b> - if there is no expected element.
     */
    @Then(value = "an image with the tooltip '$tooltip' and src containing '$srcpart' exists", priority = 1)
    public WebElement ifImageWithTooltipAndSrcPartExists(String tooltip, String srcpart)
    {
        WebElement image = ifImageWithSrcPartExists(srcpart);
        elementValidations.assertIfElementContainsTooltip(image, tooltip);
        return image;
    }

    /**
     * Checks, that there is an <b>image</b> that contains a partial value of the <i>'src'</i> attribute
     * and has the right <b>tooltip</b> <br>
     * A <b>tooltip</b> is a small hint that appears when the user hovers a mouse over the image. <br>
     * An <b>image</b> is defined with the <i>{@literal <img>}</i> tag.
     * <p>
     * @param imageSrc A value of the 'src' attribute
     * @param tooltip A <i>'title'</i> or an <i>'alt'</i> attribute value of the image
     * @return <b>Web element</b> - An image that meets the requirements; <br>
     * <b>null</b> - if there is no expected element.
     */
    @Then(value = "an image with the src '$imageSrc' and tooltip '$tooltip' exists", priority = 1)
    public WebElement ifImageWithSrcAndTooltipExists(String imageSrc, String tooltip)
    {
        WebElement image = ifImageWithSrcExists(imageSrc);
        elementValidations.assertIfElementContainsTooltip(image, tooltip);
        return image;
    }

    /**
     * Checks, that there is an <b>image</b> that contains a partial value of the <i>'src'</i> attribute
     * and has the right <b>tooltip</b> <br>
     * and the right <b>state</b>
     * A <b>tooltip</b> is a small hint that appears when the user hovers a mouse over the image. <br>
     * An <b>image</b> is defined with the <i>{@literal <img>}</i> tag.
     * <p>
     * @param state A state value of the element
     * @param imageSrc A value of the 'src' attribute
     * @param tooltip A <i>'title'</i> or an <i>'alt'</i> attribute value of the image
     */
    @Then(value = "a [$state] image with the src '$imageSrc' and tooltip '$tooltip' exists", priority = 1)
    public void ifImageWithSrcAndTooltipExists(State state, String imageSrc, String tooltip)
    {
        WebElement image = ifImageWithSrcAndTooltipExists(imageSrc, tooltip);
        baseValidations.assertElementState(THE_FOUND_IMAGE_IS + state, state, image);
    }

    /**
     * Checks that an <b>image</b> with the specified <b>src</b> part exists
     * and it has expected <b>state</b>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param srcpart Any part of 'href' attribute value of the image
     */
    @Then("a [$state] image with the src containing '$srcpart' exists")
    public void ifStateImageWithSrcPartExists(State state, String srcpart)
    {
        WebElement image = ifImageWithSrcPartExists(srcpart);
        baseValidations.assertElementState(THE_FOUND_IMAGE_IS + state, state, image);
    }

    /**
     * Checks that previously set searchContext contains an <b>image</b> with the specified <b>tooltip</b>
     * and it has expected <b>state</b>
     * An <b>Image</b> is defined with the <i>{@literal <img>}</i> tag.
     * A <b>Tooltip</b> is a small hint that appears when the user hovers a mouse over the image. <br>
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Verifies, that there is <b>exactly one</b> image with the expected <b>tooltip</b>
     * </ul>
     * <p>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param tooltipImage <i>'title'</i> attribute value of the image
     */
    @Then("a [$state] image with the tooltip '$tooltipImage' exists")
    public void ifImageWithTooltipExists(State state,  String tooltipImage)
    {
        WebElement image = ifImageWithTooltipExists(tooltipImage);
        baseValidations.assertElementState(THE_FOUND_IMAGE_IS + state, state, image);
    }

    /**
     * Checks that previously set searchContext contains an <b>image</b> with the specified <b>tooltip</b>, <b>src</b>
     * and it has expected <b>state</b>
     * <p>
     * An <b>Image</b> is defined with the <i>{@literal <img>}</i> tag. A <b>Tooltip</b> is a small hint that appears
     * when the user hovers a mouse over the image. A <b>Src</b> is a value of the <i>src</i> attribute, which refers to
     * the image file location
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Verifies, that there is <b>exactly one</b> image with the expected <b>tooltip</b>, <b>src</b> and
     * <b>state</b>
     * </ul>
     * <p>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param imageSrc A <i>'src'</i> attribute value of the image
     * @param tooltipImage <i>'title'</i> attribute value of the image
     */
    @Then("a [$state] image with the src '$imageSrc' and tooltip '$tooltipImage' exists")
    public void ifImageWithUrlAndTooltipExists(State state, String imageSrc, String tooltipImage)
    {
        WebElement image = ifImageWithUrlAndTooltipExists(imageSrc, tooltipImage);
        baseValidations.assertElementState(THE_FOUND_IMAGE_IS + state, state, image);
    }

    /**
     * Checks that previously set searchContext contains an <b>image</b> with the specified
     * <b>tooltip</b> and <b>src</b>
     * <p>
     * An <b>Image</b> is defined with the <i>{@literal <img>}</i> tag. A <b>Tooltip</b> is a small hint that appears
     * when the user hovers a mouse over the image. A <b>Src</b> is a value of the <i>src</i> attribute, which refers to
     * the image file location
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Verifies, that there is <b>exactly one</b> image with the expected <b>tooltip</b> and <b>src</b>
     * </ul>
     * <p>
     * @param src A <i>'src'</i> attribute value of the image
     * @param tooltipImage <i>'title'</i> attribute value of the image
     * @return image if found, otherwise null
     */
    @Then("an image with the src '$src' and tooltip '$tooltipImage' exists")
    public WebElement ifImageWithUrlAndTooltipExists(String src, String tooltipImage)
    {
        WebElement image = ifImageWithSrcExists(src);
        return elementValidations.assertIfElementContainsTooltip(image, tooltipImage) ? image : null;
    }

    /**
     * Checks that an <b>image</b> with the specified <b>tooltip</b> exists
     * @param tooltipImage A <i>'title'</i> or an <i>'alt'</i> attribute value of the image
     * @return <b>WebElement</b> An element (image) matching the requirements <br>
     * <b>null</b> - if there are no desired elements
     */
    @Then("an image with the tooltip '$tooltipImage' exists")
    public WebElement ifImageWithTooltipExists(String tooltipImage)
    {
        return baseValidations.assertIfElementExists(String.format("An image with the tooltip '%s'", tooltipImage),
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(ElementPattern.LINK_IMAGE_TOOLTIP_PATTERN, tooltipImage)));
    }
}
