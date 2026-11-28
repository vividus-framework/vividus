/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.ui.web;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.action.CookieManager;
import org.vividus.ui.web.action.NavigateActions;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

public class CookieSteps<T>
{
    private final CookieManager<T> cookieManager;
    private final Function<T, String> cookieNameMapper;
    private final Function<T, Object> cookieToJsonObjectMapper;
    private final NavigateActions navigateActions;
    private final VariableContext variableContext;
    private final JsonUtils jsonUtils;
    private final ISoftAssert softAssert;

    public CookieSteps(CookieManager<T> cookieManager, Function<T, String> cookieNameMapper,
            Function<T, Object> cookieToJsonObjectMapper, NavigateActions navigateActions,
            VariableContext variableContext, JsonUtils jsonUtils, ISoftAssert softAssert)
    {
        this.cookieManager = cookieManager;
        this.cookieNameMapper = cookieNameMapper;
        this.cookieToJsonObjectMapper = cookieToJsonObjectMapper;
        this.navigateActions = navigateActions;
        this.variableContext = variableContext;
        this.jsonUtils = jsonUtils;
        this.softAssert = softAssert;
    }

    /**
     * Adds the cookies provided in the input ExamplesTable with domain of the opened currently page. After adding
     * the cookies the opened page is refreshed (this is required to apply the new cookies).
     * <p>The cookie parameters defined in the ExamplesTable:</p>
     * <ul>
     * <li><b>cookieName</b> - the name of the cookie to set;</li>
     * <li><b>cookieValue</b> - the value of the cookie to set;</li>
     * <li><b>path</b> - the path of the cookie to set.</li>
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
     * Adds the cookies provided in the input ExamplesTable with domain of the opened currently page, but does not
     * apply the changes in cookies instantly. The current page must be refreshed, or navigation must be performed to
     * apply the cookie changes.
     * <p>The cookie parameters defined in the ExamplesTable:</p>
     * <ul>
     * <li><b>cookieName</b> - the name of the cookie to set;</li>
     * <li><b>cookieValue</b> - the value of the cookie to set;</li>
     * <li><b>path</b> - the path of the cookie to set.</li>
     * </ul>
     * <p>Usage example:</p>
     * <code>
     * <br>When I set all cookies for current domain without applying changes:
     * <br>|cookieName   |cookieValue |path |
     * <br>|cookieAgreed |2           |/    |
     * </code>
     *
     * @param parameters The parameters of the cookies to set as ExamplesTable
     */
    @When("I set all cookies for current domain without applying changes:$parameters")
    public void setAllCookiesWithoutApply(ExamplesTable parameters)
    {
        String currentPageUrl = navigateActions.getCurrentUrl();
        Validate.isTrue(null != currentPageUrl,
                "Current page URL is not provided. Please make sure to navigate to the correct page");
        parameters.getRows().forEach(row ->
                cookieManager.addCookie(row.get("cookieName"), row.get("cookieValue"), row.get("path"), currentPageUrl)
        );
    }

    /**
     * Removes the certain cookie from the current domain. After removing the cookies the opened page is refreshed
     * (this is required to apply the new cookies).
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
     * Removes the certain cookie from the current domain, but does not apply the changes in cookies instantly. The
     * current page must be refreshed, or navigation must be performed to apply the cookie changes.
     *
     * @param cookieName The name of the cookie to remove
     */
    @When("I remove cookie with name `$cookieName` from current domain without applying changes")
    public void removeCookieWithoutApply(String cookieName)
    {
        cookieManager.deleteCookie(cookieName);
    }

    /**
     * Removes all cookies from the current domain. After removing the cookies the opened page is refreshed
     * (this is required to apply the new cookies).
     */
    @When("I remove all cookies from current domain")
    public void removeAllCookies()
    {
        removeAllCookiesWithoutApply();
        navigateActions.refresh();
    }

    /**
     * Removes all cookies from the current domain, but does not apply the changes in cookies instantly. The
     * current page must be refreshed, or navigation must be performed to apply the cookie changes.
     */
    @When("I remove all cookies from current domain without applying changes")
    public void removeAllCookiesWithoutApply()
    {
        cookieManager.deleteAllCookies();
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
                .map(cookieToJsonObjectMapper)
                .ifPresent(cam -> variableContext.putVariable(scopes, variableName, jsonUtils.toJson(cam)));
    }

    /**
     * Checks if cookie with name <code>cookieName</code> matching the comparison rule is set.
     *
     * @param stringComparisonRule Comparison rule: "is equal to", "contains", "does not contain", "matches".
     * @param cookieName           The name of the cookie to check presence.
     * @return Cookie if it's present
     */
    @Then("cookie with name that $stringComparisonRule `$cookieName` is set")
    public Optional<T> assertCookieIsSet(StringComparisonRule stringComparisonRule, String cookieName)
    {
        Optional<T> cookie = getCookieMatchingRule(stringComparisonRule, cookieName);
        softAssert.assertTrue(
                String.format("Cookie with the name that %s '%s' is set", stringComparisonRule, cookieName),
                cookie.isPresent());
        return cookie;
    }

    /**
     * Checks if cookie with name <code>cookieName</code> matching the comparison rule is not set.
     *
     * @param stringComparisonRule Comparison rule: "is equal to", "contains", "does not contain", "matches".
     * @param cookieName           The name of the cookie to check absence.
     */
    @Then("cookie with name that $stringComparisonRule `$cookieName` is not set")
    public void assertCookieIsNotSet(StringComparisonRule stringComparisonRule, String cookieName)
    {
        Optional<T> cookie = getCookieMatchingRule(stringComparisonRule, cookieName);
        softAssert.assertTrue(
                String.format("Cookie with the name that %s '%s' is not set", stringComparisonRule, cookieName),
                cookie.isEmpty());
    }

    private Optional<T> getCookieMatchingRule(StringComparisonRule comparisonRule, String cookieName)
    {
        Matcher<String> matcher = comparisonRule.createMatcher(cookieName);
        return cookieManager.getCookies().stream()
                .filter(cookie -> matcher.matches(cookieNameMapper.apply(cookie)))
                .findFirst();
    }
}
