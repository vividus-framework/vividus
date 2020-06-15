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
import java.util.stream.Collectors;

import org.apache.http.cookie.Cookie;
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
        List<Cookie> cookies = cookieStoreProvider.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals(cookieName))
                .collect(Collectors.toList());
        int cookiesNumber = cookies.size();
        if (softAssert.assertThat(String.format("Number of cookies with name '%s'", cookieName), cookiesNumber,
                greaterThan(0)))
        {
            if (cookiesNumber == 1)
            {
                bddVariableContext.putVariable(scopes, variableName, cookies.get(0).getValue());
            }
            else
            {
                String rootPath = "/";
                LOGGER.info("Filtering cookies by path attribute '{}'", rootPath);
                cookies = cookies.stream()
                        .filter(cookie -> rootPath.equals(cookie.getPath()))
                        .collect(Collectors.toList());
                if (softAssert.assertEquals(String.format("Number of cookies with name '%s' and path attribute '%s'",
                        cookieName, rootPath), 1, cookies.size()))
                {
                    bddVariableContext.putVariable(scopes, variableName, cookies.get(0).getValue());
                }
            }
        }
    }
}
