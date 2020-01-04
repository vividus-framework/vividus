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

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.util.LocatorUtil;

public class ScriptSteps
{
    @Inject private IBaseValidations baseValidations;

    /**
     * Checks that there is exactly one <b>javascript file</b> (JS file) with the given <b>filename</b>
     * in the source of the active page within search context. By default context is set to the whole page.
     * <p>
     * JS files are placed under <i>{@literal <script>}</i> tag having attribute <i>type</i> = "text/javascript". Their
     * <b>filename</b> is specified in <i>src</i> attribute.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li><i>Checks</i> if the javascript file with the given filename exists in the page source code
     * <li>Step passes if only one such JS file was found. Otherwise step fails.
     * </ul>
     * <p>
     * @param jsFileName Value of the <i>src</i> attribute
     * @return webElement found js script
     */
    @Then("a javascript file with the name '$jsFileName' is included in the source code")
    public WebElement thenJavascriptFileWithNameIsIncludedInTheSourceCode(String jsFileName)
    {
        return baseValidations.assertIfElementExists(String.format("Script with the name '%s'", jsFileName),
                new SearchAttributes(ActionAttributeType.XPATH,
                        new SearchParameters(LocatorUtil.getXPath(".//script[contains(@src, %s)]", jsFileName),
                                Visibility.ALL)));
    }

    /**
     * Checks that there is exactly one <b>javascript file</b> (JS file) with the given <b>text</b>
     * in the source of the active page within search context. By default context is set to the whole page.
     * <p>
     * JS files are placed under <i>{@literal <script>}</i> tag having attribute <i>type</i> = "text/javascript".
     * <b>Text</b> is actual content of <i>{@literal <script>}</i> tag.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li><i>Checks</i> if the javascript file with the given text exists in the page source code
     * <li>Step passes if only one such JS file was found. Otherwise step fails.
     * </ul>
     * <p>
     * @param jsText Content of the <i>{@literal <script>}</i> tag.
     * @return webElement found js script
     */
    @Then("a javascript with the text '$jsText' is included in the source code")
    public WebElement thenJavascriptFileWithTextIsIncludedInTheSourceCode(String jsText)
    {
        return baseValidations.assertIfElementExists(String.format("Script with text '%s'", jsText),
                new SearchAttributes(ActionAttributeType.XPATH,
                        new SearchParameters(LocatorUtil.getXPath(".//script[text()=%s]", jsText), Visibility.ALL)));
    }

    /**
     * Checks that there is exactly one <b>javascript file</b> (JS file) containing the given <b>text</b>
     * in the source of the active page within search context. By default context is set to the whole page.
     * <p>
     * JS files are placed under <i>{@literal <script>}</i> tag having attribute <i>type</i> = "text/javascript".
     * <b>Text</b> is actual content of <i>{@literal <script>}</i> tag.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li><i>Checks</i> if the javascript file containing the given text exists in the page source code.
     * <li>Step passes if only one such JS file was found. Otherwise step fails.
     * </ul>
     * <p>
     * @param jsTextPart String which should be contained in the <i>{@literal <script>}</i> tag.
     * @return webElement found js script
     */
    @Then("a javascript with the textpart '$jsTextPart' is included in the source code")
    public WebElement thenJavascriptWithTextPartIsIncludedInTheSourceCode(String jsTextPart)
    {
        return baseValidations.assertIfElementExists(String.format("Script with the text part '%s'", jsTextPart),
                new SearchAttributes(ActionAttributeType.XPATH,
                        new SearchParameters(LocatorUtil.getXPath(".//script[contains(text(),%s)]", jsTextPart),
                                Visibility.ALL)));
    }
}
