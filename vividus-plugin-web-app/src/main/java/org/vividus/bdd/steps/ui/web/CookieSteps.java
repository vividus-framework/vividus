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

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.openqa.selenium.Cookie;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.ICookieManager;
import org.vividus.ui.web.action.INavigateActions;
import org.vividus.util.json.IJsonUtils;

public class CookieSteps
{
    @Inject private ISoftAssert softAssert;
    @Inject private ICookieManager cookieManager;
    @Inject private INavigateActions navigateActions;
    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IBddVariableContext bddVariableContext;
    private IJsonUtils jsonUtils;

    /**
     * Removes all cookies from the current domain
     * <p>
     * <b>Cookie</b> is a text file, which contains data sent from a website and
     * stored in the user's web browser while the user is browsing it.
     * </p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Removes all cookies from the current domain</li>
     * <li>Refreshes the current page</li>
     * </ul>
     */
    @When("I remove all cookies from the current domain")
    public void whenIRemoveAllCookiesFromTheCurrentDomain()
    {
        cookieManager.deleteAllCookies();
        navigateActions.refresh();
    }

    /**
     * Removes certain cookie from current domain
     * <p>
     * <b>Cookie</b> is a text file, which contains data sent from a website and
     * stored in the user's web browser while the user is browsing it.
     * </p>
     * <p>
     * <b>Domain</b> is an identification string that defines a realm of
     * administrative autonomy, authority or control within the Internet.
     * </p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Removes certain cookie from current domain</li>
     * <li>Refreshes the current page</li>
     * </ul>
     * @param cookieName Cookie name
     */
    @When("I remove a cookie with the name '$cookieName' from the current domain")
    public void whenIRemoveCookieWithNameFromCurrentDomain(String cookieName)
    {
        cookieManager.deleteCookie(cookieName);
        navigateActions.refresh();
    }

    /**
     * Checks if cookie with name <code>cookieName</code> is set
     * @param cookieName Cookie name
     * @return Optional containing cookie
     */
    @Then("a cookie with the name '$cookieName' is set")
    public Optional<Cookie> thenCookieWithNameIsSet(String cookieName)
    {
        Cookie cookie = cookieManager.getCookie(cookieName);
        softAssert.assertThat(String.format("Cookie with the name '%s' is set", cookieName), cookie, notNullValue());
        return Optional.ofNullable(cookie);
    }

    /**
    * Saves cookie value into variable
    * @param name Cookie name
    * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
    * <i>Available scopes:</i>
    * <ul>
    * <li><b>STEP</b> - the variable will be available only within the step,
    * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
    * <li><b>STORY</b> - the variable will be available within the whole story,
    * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
    * </ul>
    * @param variableName to save
    */
    @When("I save value of cookie with name `$name` to $scopes variable `$variableName`")
    public void saveCookieIntoVariable(String name, Set<VariableScope> scopes, String variableName)
    {
        thenCookieWithNameIsSet(name).map(Cookie::getValue)
                .ifPresent(v -> bddVariableContext.putVariable(scopes, variableName, v));

    }

    /**
     * Checks if cookie with name <code>cookieName</code> is not set
     * @param cookieName Cookie name
     */
    @Then("a cookie with the name '$cookieName' is not set")
    public void thenCookieWithNameIsNotSet(String cookieName)
    {
        Cookie cookie = cookieManager.getCookie(cookieName);
        softAssert.assertThat(String.format("Cookie with the name '%s' is not set", cookieName), cookie, nullValue());
    }

    /**
     * Adds certain cookies with parameters described in examples table
     * <p>
     * <b>cookieName</b> name of cookie to set
     * </p>
     * <p>
     * <b>cookieValue</b> cookie value to set
     * </p>
     * <p>
     * <b>path</b> of cookie to set
     * </p>
     * <ul>
     * <li>Adds cookies</li>
     * <li>Refreshes the current page</li>
     * </ul>
     * <p><b>Example:</b></p>
     * <table border="1" style="width:70%">
     * <caption>Table of parameters</caption>
     * <thead>
     * <tr>
     * <td><h1>cookieName</h1></td>
     * <td><h1>cookieValue</h1></td>
     * <td><h1>path</h1></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>cookieAgreed</td>
     * <td>2</td>
     * <td>/</td>
     * </tr>
     * </tbody>
     * </table>
     * <br>Usage example:
     * <code>
     * <br>When I set all cookies for current domain:
     * <br>|cookieName    |cookieValue|path|
     * <br>|cookieAgreed  |2          |/   |
     * </code>
     * @param parameters cookies description
     */
    @When("I set all cookies for current domain:$parameters")
    public void setAllCookies(ExamplesTable parameters)
    {
        String currentUrl = webDriverProvider.get().getCurrentUrl();
        parameters.getRows().forEach(row ->
        {
            cookieManager.addCookie(row.get("cookieName"), row.get("cookieValue"), row.get("path"), currentUrl);
        });
        navigateActions.refresh();
    }

    /**
     * Saves cookie as JSON to scope variable
     * @param cookieName name of cookie to save
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName name of variable
     */
    @When("I save cookie with name `$cookieName` as JSON to $scopes variable `$variableName`")
    public void saveCookieAsJson(String cookieName, Set<VariableScope> scopes, String variableName)
    {
        Optional.ofNullable(cookieManager.getCookie(cookieName))
                .map(Cookie::toJson)
                .ifPresent(cam -> bddVariableContext.putVariable(scopes, variableName, jsonUtils.toJson(cam)));
    }

    public void setJsonUtils(IJsonUtils jsonUtils)
    {
        this.jsonUtils = jsonUtils;
    }
}
