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

import static org.vividus.ui.web.action.search.ActionAttributeType.LINK_URL;
import static org.vividus.ui.web.action.search.ActionAttributeType.LINK_URL_PART;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.validation.ILinkValidations;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@SuppressWarnings("checkstyle:methodcount")
@TakeScreenshotOnFailure
public class LinkSteps
{
    private static final String THE_FOUND_LINK_IS = "The found link is ";
    private static final String TEXT = "text";

    @Inject private ILinkValidations linkValidations;
    @Inject private IWebUiContext webUiContext;
    @Inject private IBaseValidations baseValidations;
    @Inject private IMouseActions mouseActions;

    /**
     * Sets a cursor on the <b>link</b> specified by the 'text' value
     * <p>
     * A 'text' value is a visible text of the <b>link</b> (<i>{@literal <a>}</i> tag)
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the <b>link</b>
     * <li>Sets cursor on it
     * </ul>
     * @param linkText An URL that should be in a 'href' attribute of the link
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I hover a mouse over a link with the text '$text'")
    public void mouseOverLinkText(String linkText)
    {
        WebElement link = ifLinkWithTextExists(linkText);
        mouseActions.moveToElement(link);
    }

    /**
     * Sets a cursor on the <b>link</b> specified by an 'URL'
     * <p>
     * An 'URL' is a 'href' attribute value of the <b>link</b> (<i>{@literal <a>}</i> tag)
     * <br>
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/absolute")
     * <li>A relative URL - points to a file within a web site (like href="/relative")
     * </ul>
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the <b>link</b>
     * <li>Sets a cursor on it
     * </ul>
     * @param url An URL that should be in a 'href' attribute of the link
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     * @see <a href="http://www.w3schools.com/html/html_links.asp"><i>HTML Links</i></a>
     */
    @When("I hover a mouse over a link with the URL '$URL'")
    public void mouseOverUriLink(String url)
    {
        WebElement link = assertLinkExists(new SearchAttributes(LINK_URL, url));
        mouseActions.moveToElement(link);
    }

    /**
     * Clicks on a link by the <b>CSS</b> selector
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the link;
     * <li>Clicks on it.
     * </ul>
     * <p>
     * @param cssSelector CSS selector of the link
     * @see <a href="http://www.w3schools.com/cssref/pr_font_font-style.asp"><i>How to find CSS style values</i></a>
     * @see <a href="http://www.w3schools.com/css/css_link.asp"><i>CSS Links</i></a>
     */
    @When("I click on a link with the CSS selector '$cssSelector'")
    public void clickLinkByCss(String cssSelector)
    {
        WebElement element = linkValidations.assertIfLinkExists(getSearchContext(),
                new SearchAttributes(ActionAttributeType.CSS_SELECTOR, cssSelector));
        mouseActions.click(element);
    }

    /**
     * Checks if the <b><i>link</i></b> with the certain <b>part of URL and text value</b>exists and
     * clicks on this link
     * <p>
     * <b>part of ther URL</b> is a part of value of the 'href' attribute in the tag <i>(for ex.
     * href="https://www.vividus.org", part of href="vividus")</i>
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the link as an element with certain part of the URL and text value
     * <li>Clicks on this link
     * </ul>
     * <p>
     * @param urlPart Value of the part of the href attribute in a link tag
     * @param text Value of any attribute value
     */
    @When(value = "I click on a link with the URL containing '$URLpart' and the text '$text'", priority = 1)
    public void clickLinkWithPartUrlAndText(String urlPart, String text)
    {
        WebElement link = ifLinkWithTextAndUrlPartExists(text, urlPart);
        mouseActions.click(link);
    }

    /**
     * Clicks on a link with the specified <b>linkText</b> value
     * and URL, which is the value of the attribute {@literal href} in a {@literal <a>} tag
     * <p>
     * <b>Actions performed at this step:</b>
     * <ul>
     * <li>Checks that this context contains the link with the specified <b>text and URL</b>
     * <li>Click on this link
     * </ul>
     * <b>Link</b> is a {@literal <a>} tag
     * <p>
     * @param linkText Any attribute value of the link tag
     * @param url Expected value of the <b>{@literal <href>}</b> attribute in link tag
     */
    @When(value = "I click on a link with the text '$text' and URL '$URL'", priority = 1)
    public void clickLinkWithTextAndURL(String linkText, String url)
    {
        WebElement linkElement = ifLinkWithTextAndUrlExists(linkText, url);
        mouseActions.click(linkElement);
    }

    /**
     * Clicks on a link with the specified <b>linkText</b> value
     * <b>Actions performed at this step:</b>
     * <ul>
     * <li>Checks that this context contains the link with the specified <b>text</b>
     * <li>Click on this link
     * </ul>
     * <b>Link</b> is a {@literal <a>} tag
     * <p>
     * @param text Any attribute value of the link tag
     */
    @When("I click on a link with the text '$text'")
    public void clickLinkWithText(String text)
    {
        WebElement link = ifLinkWithTextExists(text);
        mouseActions.click(link);
    }

    /**
     * Clicks on a link with the {@literal <href>} attribute in the search context
     * <p>
     * <b>Link</b> is a {@literal <a>} tag
     * <p>
     * @param url A 'href' attribute of the link
     */
    @When("I click on a link with the URL '$URL'")
    public void clickLinkWithUrl(String url)
    {
        WebElement link = assertLinkExists(new SearchAttributes(LINK_URL, url));
        mouseActions.click(link);
    }

    /**
     * Clicks on a link with any part of the {@literal <href>} attribute in the search context
     * <p>
     * <b>Link</b> is a {@literal <a>} tag
     * <p>
     * @param urlPart Value of the part of the 'href' attribute in a link tag
     */
    @When("I click on a link with the URL containing '$URLpart'")
    public void clickLinkWithUrlPart(String urlPart)
    {
        WebElement link = assertLinkExists(new SearchAttributes(LINK_URL_PART, urlPart));
        mouseActions.click(link);
    }

    /**
     * Clicks on an image with a desired tooltip.
     * <br>
     * An <b>image</b> is defined with the <i>{@literal <img>}</i> tag.
     * A <b>tooltip</b> is a small hint that appears when the user hovers a mouse over the image.<br>
     * Actions performed at this step:
     * <ul>
     * <li>Finds an image by its <i>tooltip</i>
     * <li>Clicks on an element
     * <li>Waits for the page to load
     * </ul>
     * <p>
     * @param tooltipImage A <i>'title'</i> or an <i>'alt'</i> attribute value of the image
     */
    @When("I click on an image with the tooltip '$tooltipImage'")
    public void clickLinkImageTooltip(String tooltipImage)
    {
        String imageXpath = LocatorUtil.getXPath(ElementPattern.LINK_IMAGE_TOOLTIP_PATTERN, tooltipImage);
        WebElement imageLink = linkValidations.assertIfLinkExists(getSearchContext(),
                new SearchAttributes(ActionAttributeType.XPATH, imageXpath));
        mouseActions.click(imageLink);
    }

    /**
     * Checks that a <b>link</b> with the expected <b>text</b> does not exist in context
     * @param text An expected text of the link
     */
    @Then("a link with the text '$text' does not exist")
    public void doesNotLinkWithTextExist(String text)
    {
        linkValidations.assertIfLinkWithTextNotExists(getSearchContext(), text);
    }

    /**
     * Checks that a <b>link</b> with the expected <b>text</b> and <b>URL</b> does not exist in context
     * An 'URL' is a 'href' attribute value of the <b>link</b> (<i>{@literal <a>}</i> tag) <br>
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/absolute")
     * <li>A relative URL - points to a file within a web site (like href="/relative")
     * </ul>
     * @param url A 'href' attribute value of the link
     * @param text An expected text of the link
    */
    @Then(value = "a link with the text '$text' and URL '$URL' does not exist", priority = 1)
    public void doesNotLinkWithTextAndUrlExist(String text, String url)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, text).addFilter(LINK_URL,
                url);
        linkValidations.assertIfLinkDoesNotExist(getSearchContext(), attributes);
    }

    /**
     * Checks that a <b>link</b> with the expected <b>URL</b> and <b>'title' attribute</b> does not exist in context
     * @param url A 'href' attribute value of the link
     * @param tooltip A 'title' attribute value of the link
     */
    @Then("a link with the URL '$URL' and tooltip '$tooltip' does not exist")
    public void doesNotLinkWithUrlAndTooltipExist(String url, String tooltip)
    {
        SearchAttributes attributes = new SearchAttributes(LINK_URL, url).addFilter(ActionAttributeType.TOOLTIP,
                tooltip);
        linkValidations.assertIfLinkDoesNotExist(getSearchContext(), attributes);
    }

    /**
     * Checks that a <b>link</b> with the expected <b>text</b> and <b>URL</b> and <b>'title' attribute</b> does not
     * exist in context
     * An 'URL' is a 'href' attribute value of the <b>link</b> (<i>{@literal <a>}</i> tag) <br>
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/absolute")
     * <li>A relative URL - points to a file within a web site (like href="/relative")
     * </ul>
     * @param url A 'href' attribute value of the link
     * @param text An expected text of the link
     * @param tooltip A 'title' attribute value of the link
    */
    @Then("a link with the text '$text' and URL '$URL' and tooltip '$tooltip' does not exist")
    public void doesNotLinkWithTextAndUrlAndTooltipExist(String text, String url, String tooltip)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, text).addFilter(LINK_URL, url)
                .addFilter(ActionAttributeType.TOOLTIP, tooltip);
        linkValidations.assertIfLinkDoesNotExist(getSearchContext(), attributes);
    }

    /**
     * Checks that searchContext contains <b>linkItems</b> with expected text and url
     * (<b>text</b> and <b>href attribute</b> values):
     * <table border="1" style="width:10%">
     * <caption>A table of attributes</caption>
     * <thead>
     * <tr>
     * <td>
     * <h1>text</h1></td>
     * <td>
     * <h1>link</h1></td>
     * </tr>
     * </thead> <tbody>
     * <tr>
     * <td>linkItemText1</td>
     * <td>linkItemLink1</td>
     * </tr>
     * <tr>
     * <td>linkItemText2</td>
     * <td>linkItemLink2</td>
     * </tr>
     * <tr>
     * <td>linkItemText3</td>
     * <td>linkItemLink3</td>
     * </tr>
     * </tbody>
     * </table>
     * @param expectedLinkItems A table of expected <b>link</b> items
     */
    @Then(value = "context contains list of link items with the text and link: $expectedLinkItems", priority = 1)
    public void ifLinkItemsWithTextAndLink(ExamplesTable expectedLinkItems)
    {
        for (Parameters row : expectedLinkItems.getRowsAsParameters(true))
        {
            ifLinkWithTextAndUrlExists(row.valueAs(TEXT, String.class), row.valueAs("link", String.class));
        }
    }

    /**
     * Checks that a <b>link</b> with the expected <b>text</b> exists
     * @param text An expected text of the link
     * @return <b>WebElement</b> An element (link) matching the requirements <br>
     * <b>null</b> - if there are no desired elements
    */
    @Then("a link with the text '$text' exists")
    public WebElement ifLinkWithTextExists(String text)
    {
        return linkValidations.assertIfLinkWithTextExists(getSearchContext(), text);
    }

    /**
     * Checks that a <b>link</b> with the expected <b>text</b> exists
     * and it has expected state
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param text An expected text of the link
    */
    @Then("a [$state] link with the text '$text' exists")
    public void ifStateLinkWithTextExists(State state, String text)
    {
        WebElement link = ifLinkWithTextExists(text);
        baseValidations.assertElementState(THE_FOUND_LINK_IS + state, state, link);
    }

    /**
     * Checks that a <b>link</b> by the specified <b>locator</b> exists
     * @param searchAttributes Locator used to find a link
     * @return <b>WebElement</b> An element (link) matching the requirements <br>
     * <b>null</b> - if there are no desired elements
    */
    @Then("a link by $locator exists")
    public WebElement assertLinkExists(SearchAttributes searchAttributes)
    {
        return linkValidations.assertIfLinkExists(getSearchContext(), searchAttributes);
    }

    /**
     * Checks that a <b>link</b> with an <b>URL</b> exists
     * and it has expected state
     * An 'URL' is a 'href' attribute value of the <b>link</b> (<i>{@literal <a>}</i> tag) <br>
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/absolute")
     * <li>A relative URL - points to a file within a web site (like href="/relative")
     * </ul>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param url A 'href' attribute value of the link
    */
    @Then("a [$state] link with the URL '$URL' exists")
    public void ifStateLinkWithUrlExists(State state, String url)
    {
        WebElement link = assertLinkExists(new SearchAttributes(LINK_URL, url));
        baseValidations.assertElementState(THE_FOUND_LINK_IS + state, state, link);
    }

    /**
     * Checks that a <b>link</b> with an <b>URL</b> doesn't exist
     * An 'URL' is a 'href' attribute value of the <b>link</b> (<i>{@literal <a>}</i> tag) <br>
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/absolute")
     * <li>A relative URL - points to a file within a web site (like href="/relative")
     * </ul>
     * @param url A 'href' attribute value of the link
    */
    @Then("a link with the URL '$URL' does not exist")
    public void ifLinkWithUrlDoesntExist(String url)
    {
        linkValidations.assertIfLinkDoesNotExist(getSearchContext(), new SearchAttributes(LINK_URL, url));
    }

    /**
     * Checks that a <b>link</b> with the expected <b>text</b> and <b>'title' attribute</b> values exists
     * <p>
     * @param text An expected text of the link
     * @param tooltip A 'title' attribute value of the link
     * @return <b>WebElement</b> An element (link) matching the requirements <br>
     * <b>null</b> - if there are no desired elements
    */
    @Then(value = "a link with the text '$text' and tooltip '$tooltip' exists", priority = 1)
    public WebElement ifLinkWithTextAndTooltipExists(String text, String tooltip)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, text)
                .addFilter(ActionAttributeType.TOOLTIP, tooltip);
        return linkValidations.assertIfLinkExists(getSearchContext(), attributes);
    }

    /**
     * Checks that a <b>link</b> with the expected <b>text</b> and <b>'title' attribute</b> values exists
     * and it has expected state
     * <p>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param text An expected text of the link
     * @param tooltip A 'title' attribute value of the link
    */
    @Then(value = "a [$state] link with the text '$text' and tooltip '$tooltip' exists", priority = 1)
    public void ifStateLinkWithTextAndTooltipExists(State state, String text, String tooltip)
    {
        WebElement link = ifLinkWithTextAndTooltipExists(text, tooltip);
        baseValidations.assertElementState(THE_FOUND_LINK_IS + state, state, link);
    }

    /**
     * Checks that a <b>link</b> with the expected <b>URL</b> and <b>'title' attribute</b> values exists
     * <p>
     * An 'URL' is a 'href' attribute value of the <b>link</b> (<i>{@literal <a>}</i> tag) <br>
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/absolute")
     * <li>A relative URL - points to a file within a web site (like href="/relative")
     * </ul>
     * @param url A 'href' attribute value of the link
     * @param tooltip A 'title' attribute value of the link
     * @return <b>WebElement</b> An element (link) matching the requirements <br>
     * <b>null</b> - if there are no desired elements
    */
    @Then(value = "a link with the URL '$URL' and tooltip '$tooltip' exists", priority = 1)
    public WebElement ifLinkWithUrlAndTooltipExists(String url, String tooltip)
    {
        SearchAttributes attributes = new SearchAttributes(LINK_URL, url).addFilter(ActionAttributeType.TOOLTIP,
                tooltip);
        return linkValidations.assertIfLinkExists(getSearchContext(), attributes);
    }

    /**
     * Checks that a <b>link</b> with the expected <b>URL</b> and <b>'title' attribute</b> values exists
     * and it has expected state
     * <p>
     * An 'URL' is a 'href' attribute value of the <b>link</b> (<i>{@literal <a>}</i> tag) <br>
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/absolute")
     * <li>A relative URL - points to a file within a web site (like href="/relative")
     * </ul>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param url A 'href' attribute value of the link
     * @param tooltip A 'title' attribute value of the link
    */
    @Then(value = "a [$state] link with the URL '$URL' and tooltip '$tooltip' exists", priority = 1)
    public void ifStateLinkWithUrlAndTooltipExists(State state, String url, String tooltip)
    {
        WebElement link = ifLinkWithUrlAndTooltipExists(url, tooltip);
        baseValidations.assertElementState(THE_FOUND_LINK_IS + state, state, link);
    }

    /**
     * Checks that a <b>link</b> with the expected <b>text</b> and <b>URL</b> and <b>'title' attribute</b> values exists
     * <p>
     * An 'URL' is a 'href' attribute value of the <b>link</b> (<i>{@literal <a>}</i> tag) <br>
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/absolute")
     * <li>A relative URL - points to a file within a web site (like href="/relative")
     * </ul>
     * @param text An expected text of the link
     * @param url A 'href' attribute value of the link
     * @param tooltip A 'title' attribute value of the link
     * @return <b>WebElement</b> An element (link) matching the requirements <br>
     * <b>null</b> - if there are no desired elements
    */
    @Then(value = "a link with the text '$text' and URL '$URL' and tooltip '$tooltip' exists", priority = 2)
    public WebElement ifLinkWithTextAndUriAndTooltipExists(String text, String url, String tooltip)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, text).addFilter(LINK_URL, url)
                .addFilter(ActionAttributeType.TOOLTIP, tooltip);
        return linkValidations.assertIfLinkExists(getSearchContext(), attributes);
    }

    /**
     * Checks that a <b>link</b> with the expected <b>text</b> and <b>URL</b> and <b>'title' attribute</b> values exists
     * and it has expected state
     * <p>
     * An 'URL' is a 'href' attribute value of the <b>link</b> (<i>{@literal <a>}</i> tag) <br>
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/absolute")
     * <li>A relative URL - points to a file within a web site (like href="/relative")
     * </ul>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param text An expected text of the link
     * @param url A 'href' attribute value of the link
     * @param tooltip A 'title' attribute value of the link
     * @return link if found, otherwise null
    */
    @Then(value = "a [$state] link with the text '$text' and URL '$URL' and tooltip '$tooltip' exists", priority = 2)
    public WebElement ifStateLinkWithTextAndUriAndTooltipExists(State state, String text, String url, String tooltip)
    {
        WebElement link = ifLinkWithTextAndUriAndTooltipExists(text, url, tooltip);
        baseValidations.assertElementState(THE_FOUND_LINK_IS + state, state, link);
        return link;
    }

    /**
     * Checks that a <b>link</b> in the expected <b>state</b>
     * specified by <b>text</b> and by any <b>part of an URL</b> exists
     * <p>
     * An 'URL' is a 'href' attribute value of the <b>link</b> (<i>{@literal <a>}</i> tag) <br>
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/absolute")
     * <li>A relative URL - points to a file within a web site (like href="/relative")
     * </ul>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param text An expected text of the link
     * @param urlPart Any part of the 'href' attribute value of the link
    */
    @Then(value = "a [$state] link with text '$text' and URL containing '$URLpart' exists", priority = 1)
    public void ifStateLinkWithTextAndUrlPartExists(State state, String text, String urlPart)
    {
        WebElement link = ifLinkWithTextAndUrlPartExists(text, urlPart);
        baseValidations.assertElementState(THE_FOUND_LINK_IS + state, state, link);
    }

    /**
     * Checks that a <b>link</b> specified by <b>text</b> and by any <b>part of an URL</b> exists
     * <p>
     * An 'URL' is a 'href' attribute value of the <b>link</b> (<i>{@literal <a>}</i> tag) <br>
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/absolute")
     * <li>A relative URL - points to a file within a web site (like href="/relative")
     * </ul>
     * @param text An expected text of the link
     * @param urlPart Any part of the 'href' attribute value of the link
     * @return link if found, otherwise null
    */
    @Then(value = "a link with the text '$text' and URL containing '$URLpart' exists", priority = 1)
    public WebElement ifLinkWithTextAndUrlPartExists(String text, String urlPart)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, text).addFilter(LINK_URL_PART,
                urlPart);
        return linkValidations.assertIfLinkExists(getSearchContext(), attributes);
    }

    /**
     * Checks that previously set searchContext contains a link with the expected
     * <b>text and URL</b>
     * <p>
     * URL could be of the 2 types:
     * <ul>
     * <li><b>relative URL</b> - points to a file within a web site (like <i>'about.html'</i> or
     * <i>'/products'</i>)<br>
     * <li><b>absolute URL</b> - points to another web site (like src="http://www.example.com/image.gif")
     * </ul>
     * <p>
     * @param text Any attribute value
     * @param url Expected value of the <b>{@literal <href>}</b> attribute in a link tag
     * @return WebElement
     */
    @Then(value = "a link with the text '$text' and URL '$URL' exists", priority = 1)
    public WebElement ifLinkWithTextAndUrlExists(String text, String url)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, text).addFilter(LINK_URL,
                url);
        return linkValidations.assertIfLinkExists(getSearchContext(), attributes);
    }

    /**
     * Checks that previously set searchContext contains a link
     * with the expected <b>text and URL</b> and expected State
     * <p>
     * URL could be of the 2 types:
     * <ul>
     * <li><b>relative URL</b> - points to a file within a web site (like <i>'about.html'</i> or
     * <i>'/products'</i>)<br>
     * <li><b>absolute URL</b> - points to another web site (like src="http://www.example.com/image.gif")
     * </ul>
     * <p>
     * @param state A state value of the link
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param text Any attribute value
     * @param url Expected value of the <b>{@literal <href>}</b> attribute in a link tag
     */
    @Then(value = "a [$state] link with the text '$text' and URL '$URL' exists", priority = 1)
    public void ifLinkWithTextAndUrlExists(State state, String text, String url)
    {
        WebElement element = ifLinkWithTextAndUrlExists(text, url);
        baseValidations.assertElementState(THE_FOUND_LINK_IS + state, state, element);
    }

    /**
     * Checks that searchContext contains <b>linkItems</b> with expected text
     * <p>
     * A <b>menu</b> is defined by a {@literal <nav>} tag, which contains a list of <b>menu items</b>. The first level
     * of this list will be a <b>first-level menu</b>.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Iterate through <b>expected links</b> list
     * </ul>
     * Example:
     * <table border="1" style="width:10%">
     * <caption>A table of links</caption>
     * <thead>
     * <tr>
     * <td>
     * <h1>text</h1></td>
     * </tr>
     * </thead> <tbody>
     * <tr>
     * <td>linkItem1</td>
     * </tr>
     * <tr>
     * <td>linkItem2</td>
     * </tr>
     * <tr>
     * <td>linkItem3</td>
     * </tr>
     * </tbody>
     * </table>
     * @param expectedLinkItems A table of expected <b>link</b> items (<b>text</b> values):
     */
    @Then("context contains list of link items with the text: $expectedLinkItems")
    public void ifLinkItemsWithTextExists(ExamplesTable expectedLinkItems)
    {
        expectedLinkItems.getRowsAsParameters(true)
                .forEach(row -> ifLinkWithTextExists(row.valueAs(TEXT, String.class)));
    }

    /**
     * Checks that the code contains the only one <b>link</b> specified by a 'href' attribute
     * <p>
     * @param href A value of the <b>link's</b> 'href' attribute
     */
    @Then("a link tag with href '$href' exists")
    public void ifTagLinkExists(String href)
    {
        baseValidations.assertIfElementExists(String.format("A link tag with href '%s'", href),
                new SearchAttributes(ActionAttributeType.XPATH,
                        new SearchParameters(LocatorUtil.getXPath(".//link[@href='%s']", href))
                                .setVisibility(Visibility.ALL)));
    }

    /**
     * Checks whether there is a link
     * with the expected <b>tooltip</b> in the previously set
     * search context
     * <p>
     * <b>Row</b> is a {@literal <tr>} tag in a {@literal <table>} tag
     * <p>
     * @param tooltip Link's 'title' attribute value
     * @return WebElement
     */
    @Then("a link with the tooltip '$tooltip' exists")
    public WebElement ifLinkWithTooltipExists(String tooltip)
    {
        return linkValidations.assertIfLinkWithTooltipExists(getSearchContext(), tooltip);
    }

    /**
     * Checks whether previously set searchContext
     * contains a <b>link</b> in the expected <b>state</b> and specified by <b>'title' attribute</b> value
     * <p>
     * @param state A state value of the link
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param tooltip A 'title' attribute value of the link
    */
    @Then("a [$state] link with the tooltip '$tooltip' exists")
    public void ifLinkWithTooltipExists(State state, String tooltip)
    {
        WebElement webElement = ifLinkWithTooltipExists(tooltip);
        baseValidations.assertElementState(THE_FOUND_LINK_IS + state, state, webElement);
    }

    protected SearchContext getSearchContext()
    {
        return webUiContext.getSearchContext();
    }
}
