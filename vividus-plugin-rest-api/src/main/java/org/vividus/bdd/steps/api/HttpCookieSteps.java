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

package org.vividus.bdd.steps.api;

import static org.hamcrest.Matchers.greaterThan;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.SetCookie;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.http.CookieStoreProvider;
import org.vividus.softassert.ISoftAssert;

public class HttpCookieSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCookieSteps.class);
    private static final BiPredicate<Cookie, String> FILTER_BY_NAME = (cookie, cookieName) -> cookie.getName()
            .equals(cookieName);

    private final IBddVariableContext bddVariableContext;
    private final CookieStoreProvider cookieStoreProvider;
    private final ISoftAssert softAssert;

    public HttpCookieSteps(IBddVariableContext bddVariableContext, CookieStoreProvider cookieStoreProvider,
            ISoftAssert softAssert)
    {
        this.bddVariableContext = bddVariableContext;
        this.cookieStoreProvider = cookieStoreProvider;
        this.softAssert = softAssert;
    }

    /**
     * Saves cookie to scope variable.
     * If present several cookies with the same name will be stored cookie with the root path value (path is '/'),
     * @param cookieName   name of cookie to save
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName name of variable
     */
    @When("I save value of HTTP cookie with name `$cookieName` to $scopes variable `$variableName`")
    public void saveHttpCookieIntoVariable(String cookieName, Set<VariableScope> scopes, String variableName)
    {
        List<Cookie> cookies = findCookiesBy(FILTER_BY_NAME, cookieName);
        int cookiesNumber = cookies.size();
        if (assertCookiesPresent(cookieName, cookiesNumber))
        {
            if (cookiesNumber == 1)
            {
                bddVariableContext.putVariable(scopes, variableName, cookies.get(0).getValue());
            }
            else
            {
                String rootPath = "/";
                BiPredicate<Cookie, String> filterByPath = (cookie, cookiePath) -> cookie.getPath().equals(cookiePath);
                LOGGER.info("Filtering cookies by path attribute '{}'", rootPath);
                cookies = findCookiesBy(filterByPath, rootPath);
                if (softAssert.assertEquals(String.format("Number of cookies with name '%s' and path attribute '%s'",
                        cookieName, rootPath), 1, cookies.size()))
                {
                    bddVariableContext.putVariable(scopes, variableName, cookies.get(0).getValue());
                }
            }
        }
    }

    /**
     * Change cookie value.
     * If several cookies with the same name exist in cookie store, the value will be changed for all of them,
     * @param cookieName     name of cookie
     * @param newCookieValue value to set
     */
    @When("I change value of all HTTP cookies with name `$cookieName` to `$newCookieValue`")
    public void changeHttpCookieValue(String cookieName, String newCookieValue)
    {
        List<Cookie> cookies = findCookiesBy(FILTER_BY_NAME, cookieName);
        if (assertCookiesPresent(cookieName, cookies.size()))
        {
            cookies.forEach(cookie -> {
                if (cookie instanceof SetCookie)
                {
                    ((SetCookie) cookie).setValue(newCookieValue);
                }
                else
                {
                    throw new IllegalStateException(
                            String.format("Unable to change value of cookie with name '%s' of type '%s'", cookieName,
                                    cookie.getClass().getName()));
                }
            });
        }
    }

    private boolean assertCookiesPresent(String cookieName, int size)
    {
        return softAssert.assertThat(String.format("Number of cookies with name '%s'", cookieName), size,
                greaterThan(0));
    }

    private List<Cookie> findCookiesBy(BiPredicate<Cookie, String> filter, String expectedValue)
    {
        return cookieStoreProvider.getCookieStore().getCookies().stream()
                .filter(cookie -> filter.test(cookie, expectedValue))
                .collect(Collectors.toList());
    }
}
