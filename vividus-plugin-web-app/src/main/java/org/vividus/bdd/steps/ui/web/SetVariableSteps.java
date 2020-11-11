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
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.LocatorUtil;
import org.vividus.util.UriUtils;

@TakeScreenshotOnFailure
public class SetVariableSteps
{
    private final IWebDriverProvider webDriverProvider;
    private final ISoftAssert softAssert;
    private final ISearchActions searchActions;
    private final IBaseValidations baseValidations;
    private final IBddVariableContext bddVariableContext;
    private final IUiContext uiContext;
    private final WebJavascriptActions javascriptActions;

    public SetVariableSteps(IWebDriverProvider webDriverProvider, ISoftAssert softAssert, ISearchActions searchActions,
            IBaseValidations baseValidations, IBddVariableContext bddVariableContext, IUiContext uiContext,
            WebJavascriptActions javascriptActions)
    {
        this.webDriverProvider = webDriverProvider;
        this.softAssert = softAssert;
        this.searchActions = searchActions;
        this.baseValidations = baseValidations;
        this.bddVariableContext = bddVariableContext;
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
     */
    @When("I get the value from the URL and set it to the '$scopes' variable '$variable'")
    public void gettingValueFromUrl(Set<VariableScope> scopes, String variable)
    {
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
     */
    @When("I get the URL path and set it to the $scopes variable '$variable'")
    public void savePathFromUrl(Set<VariableScope> scopes, String variable)
    {
        String value = UriUtils.createUri(getWebDriver().getCurrentUrl()).getPath();
        saveVariable(scopes, variable, value);
    }

    /**
     * Gets an expected <b>value</b> from the <b>number of open windows</b>
     * and saves it into the <b>variable</b>
     * <p>
     * A <b>value</b> is everything after the last <b>/</b>. <br>
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
    @When("I get the number of open windows and set it to the $scopes variable '$variable'")
    public void saveNumberOfOpenWindow(Set<VariableScope> scopes, String variable)
    {
        int value = webDriverProvider.get().getWindowHandles().size();
        bddVariableContext.putVariable(scopes, variable, value);
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
    public void getUrlValueOfVideoWithNumber(int number, Set<VariableScope> scopes, String variable)
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
     */
    @When("I get the URL value of a video with the name '$name' and set it to the '$scopes' variable '$variable'")
    public void getUrlValueOfVideoWithName(String name, Set<VariableScope> scopes, String variable)
    {
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
     * Extracts the <b>number</b> of elements found by the specified <b>attribute value</b> in the context
     * and saves it into the <b>variable</b> with the specified <b>name</b>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the elements by the <b>attribute value</b>
     * <li>Saves the number of found elements into the <i>variable</i>
     * </ul>
     * @param attributeType A type of the element's attribute
     * @param attributeValue A value of the element's attribute
     * @param scopes The type of the variable
     * (<i>Possible values:</i>
     * <ul>
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>STEP</b> - the variable will be available within the step)
     * </ul>
     * @param variableName A name under which the value should be saved
     */
    @When("I set the number of elements found by the attribute '$attributeType'='$attributeValue'"
            + " to the '$scopes' variable '$variableName'")
    public void getNumberOfElementsByAttributeValueToVariable(String attributeType, String attributeValue,
            Set<VariableScope> scopes, String variableName)
    {
        Locator locator = new Locator(WebLocatorType.XPATH,
                LocatorUtil.getXPathByAttribute(attributeType, attributeValue));
        List<WebElement> elements = searchActions.findElements(getSearchContext(), locator);
        saveVariable(scopes, variableName, elements.size());
    }

    /**
     * Performs passed javascript code on the opened page
     * and saves returned value into the <b>variable</b>
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
     * @param jsCode Code in javascript that returns some value as result
     * (e.g. var a=1; return a;)
     */
    @When("I perform javascript '$jsCode' and save result to the '$scopes' variable '$variableName'")
    public void gettingValueFromJS(String jsCode, Set<VariableScope> scopes, String variableName)
    {
        assertAndSaveResult(() -> javascriptActions.executeScript(jsCode), scopes, variableName);
    }

    /**
     * Performs passed async javascript code on the opened page
     * and saves returned value into the <b>variable</b>
     * See {@link org.openqa.selenium.JavascriptExecutor#executeAsyncScript(String, Object[])}
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
     * @param jsCode Code in javascript that returns some value as result
     * (e.g. var a=1; return a;)
     */
    @When("I perform async javascript '$jsCode' and save result to the '$scopes' variable '$variableName'")
    public void gettingValueFromAsyncJS(String jsCode, Set<VariableScope> scopes, String variableName)
    {
        assertAndSaveResult(() -> javascriptActions.executeAsyncScript(jsCode), scopes, variableName);
    }

    private void assertAndSaveResult(Supplier<Object> resultProvider, Set<VariableScope> scopes, String variableName)
    {
        Object result = resultProvider.get();
        if (softAssert.assertNotNull("Returned result is not null", result))
        {
            saveVariable(scopes, variableName, result);
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
    @When("I save table to $scopes variable '$variableName'")
    public void saveTableToContext(Set<VariableScope> scopes, String variableName)
    {
        List<Map<String, Object>> table = javascriptActions.executeScriptFromResource(SetVariableSteps.class,
                "parse-table.js", uiContext.getSearchContext(WebElement.class));
        bddVariableContext.putVariable(scopes, variableName, table);
    }

    private List<WebElement> getVideoIFrames(int leastNumber)
    {
        Locator locator = new Locator(WebLocatorType.XPATH,
                LocatorUtil.getXPath("div[contains(@class,'video')]/iframe"));
        return baseValidations.assertIfAtLeastNumberOfElementsExist("The number of found video frames", locator,
                leastNumber);
    }

    private void saveSrcAttributeValueToVariable(WebElement element, Set<VariableScope> scopes, String variableName)
    {
        String value = getAssertedAttributeValue(element, "src");
        saveVariable(scopes, variableName, value);
    }

    private String getAssertedAttributeValue(WebElement element, String attributeName)
    {
        String value = element.getAttribute(attributeName);
        softAssert.assertNotNull("The '" + attributeName + "' attribute value was found", value);
        return value;
    }

    private void saveVariable(Set<VariableScope> scopes, String variableName, Object value)
    {
        bddVariableContext.putVariable(scopes, variableName, value);
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }

    private SearchContext getSearchContext()
    {
        return uiContext.getSearchContext();
    }
}
