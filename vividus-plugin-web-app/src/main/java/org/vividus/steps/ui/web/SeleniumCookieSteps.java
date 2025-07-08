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

package org.vividus.steps.ui.web;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Set;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.openqa.selenium.Cookie;
import org.vividus.annotation.Replacement;
import org.vividus.context.VariableContext;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.CookieSteps;
import org.vividus.ui.web.action.CookieManager;
import org.vividus.ui.web.action.INavigateActions;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

public class SeleniumCookieSteps extends CookieSteps<Cookie>
{
    private static final String COOKIE_PRESENCE_PATTERN = "Cookie with the name '%s' is set";

    private final IWebDriverProvider webDriverProvider;
    private final CookieManager<Cookie> cookieManager;
    private final INavigateActions navigateActions;
    private final VariableContext variableContext;
    private final ISoftAssert softAssert;

    public SeleniumCookieSteps(IWebDriverProvider webDriverProvider, CookieManager<Cookie> cookieManager,
            INavigateActions navigateActions, VariableContext variableContext, JsonUtils jsonUtils,
            ISoftAssert softAssert)
    {
        super(cookieManager, Cookie::getName, Cookie::toJson, variableContext, jsonUtils, softAssert);
        this.webDriverProvider = webDriverProvider;
        this.cookieManager = cookieManager;
        this.navigateActions = navigateActions;
        this.variableContext = variableContext;
        this.softAssert = softAssert;
    }

    /**
     * Removes all cookies from the current domain
     * <p>The actions performed by the step:</p>
     * <ul>
     * <li>remove all cookies from the current domain;</li>
     * <li>refresh the current page (this action is required to apply the changes in cookies).</li>
     * </ul>
     */
    @When("I remove all cookies from current domain")
    public void removeAllCookies()
    {
        removeAllCookiesWithoutApply();
        navigateActions.refresh();
    }

    /**
     * Removes all cookies from the current domain, but does not apply the changes in cookies. The current page must be
     * refreshed or the navigation must be performed to apply the cookie changes.
     */
    @When("I remove all cookies from current domain without applying changes")
    public void removeAllCookiesWithoutApply()
    {
        cookieManager.deleteAllCookies();
    }

    /**
     * Removes the certain cookie from the current domain
     * <p>The actions performed by the step:</p>
     * <ul>
     * <li>remove the certain cookie the from current domain;</li>
     * <li>refresh the current page (this action is required to apply the changes in cookies).</li>
     * </ul>
     *
     * @param cookieName The name of the cookie to remove
     */
    @When("I remove cookie with name `$cookieName` from current domain")
    public void removeCookie(String cookieName)
    {
        removeCookieWithoutApply(cookieName);
        navigateActions.refresh();
    }

    /**
     * Removes the certain cookie from the current domain, but does not apply the changes in cookies. The current
     * page must be refreshed or the navigation must be performed to apply the cookie changes.
     *
     * @param cookieName The name of the cookie to remove
     */
    @When("I remove cookie with name `$cookieName` from current domain without applying changes")
    public void removeCookieWithoutApply(String cookieName)
    {
        cookieManager.deleteCookie(cookieName);
    }

    /**
     * Validates whether the certain cookie is set.
     *
     * @param cookieName The name of the cookie to check presence.
     * @deprecated Use step: Then cookie with name that is equal to `$cookieName` is set
     */
    @Deprecated(since = "0.6.12", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.8.0",
            replacementFormatPattern = "Then cookie with name that is equal to `%1$s` is set")
    @Then("cookie with name `$cookieName` is set")
    public void thenCookieWithNameIsSet(String cookieName)
    {
        softAssert.assertThat(String.format(COOKIE_PRESENCE_PATTERN, cookieName), cookieManager.getCookie(cookieName),
                notNullValue());
    }

    /**
     * Finds the cookie by the name and saves its value to a variable
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
    @SuppressWarnings("checkstyle:TodoComment")
    @When("I save value of cookie with name `$cookieName` to $scopes variable `$variableName`")
    public void saveCookieIntoVariable(String cookieName, Set<VariableScope> scopes, String variableName)
    {
        Cookie cookie = cookieManager.getCookie(cookieName);
        // TODO: need to change assertion message and move to common steps in 0.7.0
        softAssert.assertThat(String.format(COOKIE_PRESENCE_PATTERN, cookieName), cookie, notNullValue());
        if (cookie != null)
        {
            variableContext.putVariable(scopes, variableName, cookie.getValue());
        }
    }

    /**
     * Validates whether the certain cookie is not set.
     *
     * @param cookieName The name of the cookie to check absence.
     * @deprecated Use step: Then cookie with name that is equal to `$cookieName` is not set
     */
    @Deprecated(since = "0.6.12", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.8.0",
            replacementFormatPattern = "Then cookie with name that is equal to `%1$s` is not set")
    @Then("cookie with name `$cookieName` is not set")
    public void thenCookieWithNameIsNotSet(String cookieName)
    {
        Cookie cookie = cookieManager.getCookie(cookieName);
        softAssert.assertThat(String.format("Cookie with the name '%s' is not set", cookieName), cookie, nullValue());
    }

    /**
     * Adds the cookies provided in the input ExamplesTable. It's allowed to add the cookies for the current domain
     * only: make sure the web browser is opened at the expected domain.
     * <p>The actions performed by the step:</p>
     * <ul>
     * <li>add the cookies;</li>
     * <li>refresh the current page (this action is required to apply the changes in cookies).</li>
     * </ul>
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
        setAllCookiesWithoutApply(parameters);
        navigateActions.refresh();
    }

    /**
     * Adds the cookies provided in the input ExamplesTable, but does not apply the changes in cookies. The current
     * page must be refreshed or the navigation must be performed to apply the cookie changes. It's allowed to add
     * the cookies for the current domain only: make sure the web browser is opened at the expected domain.
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
    @When("I set all cookies for current domain without applying changes:$parameters")
    public void setAllCookiesWithoutApply(ExamplesTable parameters)
    {
        setCookies(webDriverProvider.get().getCurrentUrl(), parameters);
    }
}
