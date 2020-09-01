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

import static org.vividus.ui.web.action.search.WebLocatorType.LINK_URL;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.web.action.search.WebLocatorType;

@TakeScreenshotOnFailure
public class LinkSteps
{
    private static final String TEXT = "text";

    @Inject private IBaseValidations baseValidations;

    /**
     * Checks that searchContext contains <b>linkItems</b> with expected text and url
     * (<b>text</b> and <b>href attribute</b> values):
     * <table border="1" style="width:10%">
     * <caption>A table of attributes</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>text</b></td>
     * <td>
     * <b>link</b></td>
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
            String text = row.valueAs(TEXT, String.class);
            String url = row.valueAs("link", String.class);
            Locator attributes = new Locator(WebLocatorType.LINK_TEXT, text).addFilter(
                    LINK_URL, url);
            baseValidations.assertIfElementExists("Link with attributes: " + attributes, attributes);
        }
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
     * <thead><tr><td><b>text</b></td></tr></thead>
     * <tbody>
     * <tr><td>linkItem1</td></tr>
     * <tr><td>linkItem2</td></tr>
     * <tr><td>linkItem3</td></tr>
     * </tbody>
     * </table>
     * @param expectedLinkItems A table of expected <b>link</b> items (<b>text</b> values):
     */
    @Then("context contains list of link items with the text: $expectedLinkItems")
    public void ifLinkItemsWithTextExists(ExamplesTable expectedLinkItems)
    {
        expectedLinkItems.getRowsAsParameters(true).stream()
                .<String>map(row -> row.valueAs(TEXT, String.class))
                .forEach(text -> {
                    Locator attributes = new Locator(WebLocatorType.LINK_TEXT, text);
                    baseValidations.assertIfElementExists(String.format("Link with text '%s'", text), attributes);
                });
    }
}
