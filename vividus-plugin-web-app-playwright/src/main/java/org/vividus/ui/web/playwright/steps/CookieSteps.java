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

import java.util.Optional;
import java.util.Set;

import com.microsoft.playwright.options.Cookie;

import org.apache.commons.lang3.Validate;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.action.CookieManager;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

public class CookieSteps
{
    private final WebApplicationConfiguration webApplicationConfiguration;
    private final VariableContext variableContext;
    private final ISoftAssert softAssert;
    private final CookieManager<Cookie> cookieManager;
    private final JsonUtils jsonUtils;

    public CookieSteps(VariableContext variableContext, WebApplicationConfiguration webApplicationConfiguration,
                       ISoftAssert softAssert, CookieManager<Cookie> cookieManager, JsonUtils jsonUtils)
    {
        this.variableContext = variableContext;
        this.webApplicationConfiguration = webApplicationConfiguration;
        this.softAssert = softAssert;
        this.cookieManager = cookieManager;
        this.jsonUtils = jsonUtils;
    }

    /**
     * Checks if cookie with name <code>cookieName</code> according the <b>string validation rule</b> is set.
     *
     * @param stringComparisonRule String comparison rule: "is equal to", "contains", "does not contain", "matches".
     * @param cookieName The name of the cookie to check presence.
     * @return The cookie if present.
     */
    @Then("cookie with name that $stringComparisonRule `$cookieName` is set")
    public Optional<Cookie> thenCookieWithMatchingNameIsSet(StringComparisonRule stringComparisonRule,
            String cookieName)
    {
        Optional<Cookie> cookie = getCookieMatchingRule(stringComparisonRule, cookieName);
        softAssert.assertTrue(
                String.format("Cookie with the name that %s '%s' is set", stringComparisonRule, cookieName),
                cookie.isPresent());
        return cookie;
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
        String currentUrl = webApplicationConfiguration.getMainApplicationPageUrl().toString();
        Validate.isTrue(currentUrl != null,
                "Unable to get current URL. Please make sure you've navigated to the right URL");
        parameters.getRows().forEach(row ->
                cookieManager.addCookie(row.get("cookieName"), row.get("cookieValue"), row.get("path"), currentUrl)
        );
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
        thenCookieWithMatchingNameIsSet(StringComparisonRule.IS_EQUAL_TO, cookieName)
                .ifPresent(cookie -> variableContext.putVariable(scopes, variableName, cookie.value));
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
                .ifPresent(cookie -> variableContext.putVariable(scopes, variableName, jsonUtils.toJson(cookie)));
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

    private Optional<Cookie> getCookieMatchingRule(StringComparisonRule validationRule, String cookieName)
    {
        Matcher<String> matcher = validationRule.createMatcher(cookieName);
        return cookieManager.getCookies().stream().filter(cookie -> matcher.matches(cookie.name)).findFirst();
    }
}
