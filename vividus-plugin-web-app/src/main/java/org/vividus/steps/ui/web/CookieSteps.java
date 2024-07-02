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

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.openqa.selenium.Cookie;
import org.vividus.annotation.Replacement;
import org.vividus.context.VariableContext;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.action.CookieManager;
import org.vividus.ui.web.action.INavigateActions;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

public class CookieSteps
{
    private static final String COOKIE_PRESENCE_PATTERN = "Cookie with the name '%s' is set";

    private final ISoftAssert softAssert;
    private final CookieManager<Cookie> cookieManager;
    private final INavigateActions navigateActions;
    private final IWebDriverProvider webDriverProvider;
    private final VariableContext variableContext;
    private final JsonUtils jsonUtils;

    public CookieSteps(ISoftAssert softAssert, CookieManager<Cookie> cookieManager, INavigateActions navigateActions,
                       IWebDriverProvider webDriverProvider, VariableContext variableContext, JsonUtils jsonUtils)
    {
        this.softAssert = softAssert;
        this.cookieManager = cookieManager;
        this.navigateActions = navigateActions;
        this.webDriverProvider = webDriverProvider;
        this.variableContext = variableContext;
        this.jsonUtils = jsonUtils;
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
     * Checks if cookie with name <code>cookieName</code> according the <b>string validation rule</b> is set.
     *
     * @param stringComparisonRule String comparison rule: "is equal to", "contains", "does not contain", "matches".
     * @param cookieName The name of the cookie to check presence.
     */
    @Then("cookie with name that $stringComparisonRule `$cookieName` is set")
    public void thenCookieWithMatchingNameIsSet(StringComparisonRule stringComparisonRule, String cookieName)
    {
        Optional<Cookie> cookie = getCookieMatchingRule(stringComparisonRule, cookieName);
        softAssert.assertTrue(
                String.format("Cookie with the name that %s '%s' is set", stringComparisonRule, cookieName),
                cookie.isPresent());
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
    @When("I save value of cookie with name `$cookieName` to $scopes variable `$variableName`")
    public void saveCookieIntoVariable(String cookieName, Set<VariableScope> scopes, String variableName)
    {
        Cookie cookie = cookieManager.getCookie(cookieName);
        softAssert.assertThat(String.format(COOKIE_PRESENCE_PATTERN, cookieName), cookie, notNullValue());
        if (cookie != null)
        {
            variableContext.putVariable(scopes, variableName, cookie.getValue());
        }
    }

    /**
     * Checks if cookie with name <code>cookieName</code> according the <b>string validation rule</b> is not set.
     *
     * @param cookieName The name of the cookie to check absence.
     * @param stringComparisonRule String comparison rule: "is equal to", "contains", "does not contain", "matches".
     */
    @Then("cookie with name that $stringComparisonRule `$cookieName` is not set")
    public void thenCookieWithMatchingNameIsNotSet(StringComparisonRule stringComparisonRule, String cookieName)
    {
        Optional<Cookie> cookie = getCookieMatchingRule(stringComparisonRule, cookieName);
        softAssert.assertTrue(
                String.format("Cookie with the name that %s '%s' is not set", stringComparisonRule, cookieName),
                cookie.isEmpty());
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
        String currentUrl = webDriverProvider.get().getCurrentUrl();
        Validate.isTrue(null != currentUrl,
                "Unable to get current URL. Please make sure you've navigated to the right URL");
        parameters.getRows().forEach(row ->
        {
            cookieManager.addCookie(row.get("cookieName"), row.get("cookieValue"), row.get("path"), currentUrl);
        });
    }

    /**
     * Finds the cookie by the name and saves all its parameters as JSON to a variable.
     *
     * @param cookieName   The name of the cookie to save.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                     scopes.<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The variable name to save the cookie.
     */
    @When("I save cookie with name `$cookieName` as JSON to $scopes variable `$variableName`")
    public void saveCookieAsJson(String cookieName, Set<VariableScope> scopes, String variableName)
    {
        Optional.ofNullable(cookieManager.getCookie(cookieName))
                .map(Cookie::toJson)
                .ifPresent(cam -> variableContext.putVariable(scopes, variableName, jsonUtils.toJson(cam)));
    }

    private Optional<Cookie> getCookieMatchingRule(StringComparisonRule validationRule, String cookieName)
    {
        Matcher<String> matcher = validationRule.createMatcher(cookieName);
        return cookieManager.getCookies().stream().filter(cookie -> matcher.matches(cookie.getName())).findFirst();
    }
}
