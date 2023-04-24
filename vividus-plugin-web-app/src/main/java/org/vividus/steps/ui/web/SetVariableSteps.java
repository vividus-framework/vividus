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
import java.util.Map;
import java.util.Set;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.VariableContext;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.util.XpathLocatorUtils;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.util.UriUtils;
import org.vividus.variable.VariableScope;

@TakeScreenshotOnFailure
public class SetVariableSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SetVariableSteps.class);

    private static final String DEPRECATED_LOG_MESSAGE_TEMPLATE = "The step: \"{}\" is deprecated "
            + "and will be removed in VIVIDUS 0.7.0. "
            + "Use combination of step and expression: "
            + "\"Given I initialize $scopes variable `$variableName` with value "
            + "`#{{}}`\"";

    private final IWebDriverProvider webDriverProvider;
    private final ISoftAssert softAssert;
    private final ISearchActions searchActions;
    private final IBaseValidations baseValidations;
    private final VariableContext variableContext;
    private final IUiContext uiContext;
    private final WebJavascriptActions javascriptActions;

    public SetVariableSteps(IWebDriverProvider webDriverProvider, ISoftAssert softAssert, ISearchActions searchActions,
            IBaseValidations baseValidations, VariableContext variableContext, IUiContext uiContext,
            WebJavascriptActions javascriptActions)
    {
        this.webDriverProvider = webDriverProvider;
        this.softAssert = softAssert;
        this.searchActions = searchActions;
        this.baseValidations = baseValidations;
        this.variableContext = variableContext;
        this.uiContext = uiContext;
        this.javascriptActions = javascriptActions;
    }

    /**
     * Gets an expected <b>value</b> from the <b>URL</b>
     * and saves it into the <b>variable</b>
     * <p>
     * A <b>value</b> is everything after the last <b>/</b>. <br>
     * An 'URL' is a 'href' attribute value of the <b>link</b> (<i>{@literal <a>}</i> tag) <br>
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/absolute")
     * <li>A relative URL - points to a file within a web site (like href="/relative")
     * </ul>
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variable A name under which the value should be saved
     * @deprecated Use combination of step and expression:
     * "Given I initialize $scopes variable `$variableName`
     *  with value `#{replaceFirstByRegExp(.*\/(?=[^\/?#]+(?:\?.+)?(?:#.*)?$),,${current-page-url})}`"
     */
    @Deprecated(since = "0.5.9", forRemoval = true)
    @When("I get the value from the URL and set it to the '$scopes' variable '$variable'")
    public void saveValueFromUrl(Set<VariableScope> scopes, String variable)
    {
        LOGGER.warn(DEPRECATED_LOG_MESSAGE_TEMPLATE,
                "When I get the value from the URL and set it to the '$scopes' variable '$variable'",
                "replaceFirstByRegExp(.*\\/(?=[^\\/?#]+(?:\\?.+)?(?:#.*)?$),,${current-page-url})");
        String url = getWebDriver().getCurrentUrl();
        int valueIndex = url.lastIndexOf('/') + 1;
        if (valueIndex != 0 && valueIndex != url.length())
        {
            saveVariable(scopes, variable, url.substring(valueIndex));
        }
        else
        {
            softAssert.recordFailedAssertion("Any appropriate value wasn't found in the URL: " + url);
        }
    }

    /**
     * Gets an path's <b>value</b> from the <b>URL</b>
     * and saves it into the <b>variable</b>
     * Example:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/page/new")</li>
     * <li>A path of URL - "/page/new")</li>
     * </ul>
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variable A name under which the value should be saved
     * @deprecated Use combination of step and expression:
     * "Given I initialize $scopes variable `$variableName` with value `#{extractPathFromUrl(${current-page-url})}`"
     */
    @Deprecated(since = "0.5.9", forRemoval = true)
    @When("I get the URL path and set it to the $scopes variable '$variable'")
    public void savePathFromUrl(Set<VariableScope> scopes, String variable)
    {
        LOGGER.warn(DEPRECATED_LOG_MESSAGE_TEMPLATE,
                "When I get the URL path and set it to the $scopes variable '$variable'",
                "extractPathFromUrl(${current-page-url})");
        String value = UriUtils.createUri(getWebDriver().getCurrentUrl()).getPath();
        saveVariable(scopes, variable, value);
    }

    /**
     * Saves the number of currently opened tabs in the browser to the variable
     * <p>
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name under which the value should be saved
     */
    @When("I save number of open tabs to $scopes variable `$variableName`")
    public void saveNumberOfOpenTabs(Set<VariableScope> scopes, String variableName)
    {
        int value = webDriverProvider.get().getWindowHandles().size();
        variableContext.putVariable(scopes, variableName, value);
    }

    /**
     * Extracts an <b>URL</b> of a video with the specified <b>sequence number</b> in the context
     * and saves it into the <b>variable</b>
     * <p>
     * The <i>URL</i> of a video lies into the value of the attribute 'src'
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Finds a frame with video by its sequence number
     * <li>Extracts the URL value of the video
     * <li>Saves the value into the <i>variable</i>
     * </ul>
     * @param number A sequence number of video how it appears on the page <i>(numbering starts with '1')</i>
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variable A name under which the value should be saved
     */
    @When("I get the URL value of a video with sequence number '$number' and set it to the '$scopes'"
            + " variable '$variable'")
    public void saveUrlValueOfVideoWithNumber(int number, Set<VariableScope> scopes, String variable)
    {
        List<WebElement> frames = getVideoIFrames(number);
        if (!frames.isEmpty())
        {
            WebElement frame = frames.get(number - 1);
            saveSrcAttributeValueToVariable(frame, scopes, variable);
        }
    }

    /**
     * Extracts an <b>URL</b> of a video with the specified <b>name</b> in the context
     * and saves it into the <b>variable</b>
     * <p>
     * The <i>URL</i> of a video lies into the value of the attribute 'src'
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Finds a frame with video by its name
     * <li>Extracts the URL value of the video
     * <li>Saves the value into the <i>variable</i>
     * </ul>
     * @param name The text in the top left corner
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variable A name under which the value should be saved
     * @deprecated This step will be removed in VIVIDUS 0.7.0
     */
    @When("I get the URL value of a video with the name '$name' and set it to the '$scopes' variable '$variable'")
    @Deprecated(since = "0.5.9", forRemoval = true)
    public void saveUrlValueOfVideoWithName(String name, Set<VariableScope> scopes, String variable)
    {
        LOGGER.warn("The step: \"When I get the URL value of a video with the name '$name' and set it "
                + "to the '$scopes' variable '$variable'\" is deprecated and will be removed in VIVIDUS 0.7.0.");
        List<WebElement> frames = getVideoIFrames(1);
        if (!frames.isEmpty())
        {
            Locator attributes = new Locator(WebLocatorType.LINK_TEXT, name);
            for (WebElement frame : frames)
            {
                getWebDriver().switchTo().frame(frame);
                List<WebElement> links = searchActions.findElements(getWebDriver(), attributes);
                if (!links.isEmpty())
                {
                    getWebDriver().switchTo().defaultContent();
                    saveSrcAttributeValueToVariable(frame, scopes, variable);
                    return;
                }
            }
            getWebDriver().switchTo().defaultContent();
            softAssert.recordFailedAssertion(String.format("A video with the name '%s' was not found", name));
        }
    }

    /**
     * Saves table to the variable context.
     * Table should be in the context.
     * Table rows' cells values will be available with following pattern variableName[0].columnName
     * Where:
     *     "variableName" - name of the variable;
     *     "[0]" - row index
     *     "columnName" - name of the tables' column
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name of variable to assign the values
     */
    @When("I save table to $scopes variable `$variableName`")
    public void saveTableToContext(Set<VariableScope> scopes, String variableName)
    {
        uiContext.getSearchContext(WebElement.class).ifPresent(tableElement -> {
            List<Map<String, Object>> table = javascriptActions.executeScriptFromResource(SetVariableSteps.class,
                    "parse-table.js", tableElement);
            variableContext.putVariable(scopes, variableName, table);
        });
    }

    private List<WebElement> getVideoIFrames(int leastNumber)
    {
        Locator locator = new Locator(WebLocatorType.XPATH,
                XpathLocatorUtils.getXPath("div[contains(@class,'video')]/iframe"));
        return baseValidations.assertIfAtLeastNumberOfElementsExist("The number of found video frames", locator,
                leastNumber);
    }

    private void saveSrcAttributeValueToVariable(WebElement element, Set<VariableScope> scopes, String variableName)
    {
        String attributeName = "src";
        String value = element.getAttribute(attributeName);
        softAssert.assertNotNull("The '" + attributeName + "' attribute value was found", value);
        saveVariable(scopes, variableName, value);
    }

    private void saveVariable(Set<VariableScope> scopes, String variableName, Object value)
    {
        variableContext.putVariable(scopes, variableName, value);
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }
}
