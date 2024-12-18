/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.steps;

import java.util.Set;

import com.microsoft.playwright.options.Cookie;

import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.CookieSteps;
import org.vividus.ui.web.action.CookieManager;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

public class PlaywrightCookieSteps extends CookieSteps<Cookie>
{
    private final UiContext uiContext;
    private final CookieManager<Cookie> cookieManager;
    private final VariableContext variableContext;

    public PlaywrightCookieSteps(UiContext uiContext, CookieManager<Cookie> cookieManager,
            VariableContext variableContext, JsonUtils jsonUtils, ISoftAssert softAssert)
    {
        super(cookieManager, cookie -> cookie.name, cookie -> cookie, variableContext, jsonUtils, softAssert);
        this.uiContext = uiContext;
        this.cookieManager = cookieManager;
        this.variableContext = variableContext;
    }

    /**
     * Adds the cookies provided in the input ExamplesTable.
     * <p>The cookie parameters to be defined in the ExamplesTable</p>
     * <ul>
     * <li><b>cookieName</b> - the name of the cookie to set</li>
     * <li><b>cookieValue</b> - the value of the cookie to set</li>
     * <li><b>path</b> - the path of the cookie to set</li>
     * </ul>
     * <p>Usage example:</p>
     * <code>
     * <br>When I set all cookies for current domain:
     * <br>|cookieName   |cookieValue |path |
     * <br>|cookieAgreed |2           |/    |
     * </code>
     *
     * @param parameters The parameters of the cookies to set as ExamplesTable
     */
    @When("I set all cookies for current domain:$parameters")
    public void setAllCookies(ExamplesTable parameters)
    {
        setCookies(uiContext.getCurrentPage().url(), parameters);
    }

    /**
     * Finds the cookie by the name and saves its value to a variable.
     *
     * @param cookieName   The name of the cookie to save the value.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                     scopes.<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The variable name to save the cookie value.
     */
    @When("I save value of cookie with name `$cookieName` to $scopes variable `$variableName`")
    public void saveCookieIntoVariable(String cookieName, Set<VariableScope> scopes, String variableName)
    {
        assertCookieIsSet(StringComparisonRule.IS_EQUAL_TO, cookieName)
                .ifPresent(cookie -> variableContext.putVariable(scopes, variableName, cookie.value));
    }

    /**
     * Removes all cookies from the current domain.
     */
    @When("I remove all cookies from current domain")
    public void removeAllCookies()
    {
        cookieManager.deleteAllCookies();
    }

    /**
     * Removes the certain cookie from the current domain.
     *
     * @param cookieName The name of the cookie to remove.
     */
    @When("I remove cookie with name `$cookieName` from current domain")
    public void removeCookie(String cookieName)
    {
        cookieManager.deleteCookie(cookieName);
    }
}
