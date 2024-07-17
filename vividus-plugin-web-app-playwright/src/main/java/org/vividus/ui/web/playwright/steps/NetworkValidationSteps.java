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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Request;

import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.softassert.SoftAssert;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.action.WaitActions;
import org.vividus.ui.web.playwright.model.HttpMessagePart;
import org.vividus.ui.web.playwright.network.NetworkContext;
import org.vividus.variable.VariableScope;

public class NetworkValidationSteps
{
    private final BrowserContextProvider browserContextProvider;
    private final WaitActions waitActions;
    private final NetworkContext networkContext;
    private final SoftAssert softAssert;
    private final VariableContext variableContext;

    public NetworkValidationSteps(BrowserContextProvider browserContextProvider, WaitActions waitActions,
            NetworkContext networkContext, SoftAssert softAssert, VariableContext variableContext)
    {
        this.browserContextProvider = browserContextProvider;
        this.waitActions = waitActions;
        this.networkContext = networkContext;
        this.softAssert = softAssert;
        this.variableContext = variableContext;
    }

    /**
     * Saves the HTTP message part from the request with given URL-pattern into the variable with specified name and
     * the scopes.
     * <p>
     * This step requires requests recording turned on.
     * It can be done in properties or by switching on <code>@proxy</code> meta tag at the story or scenario level.
     * </p>
     * The actions performed by the step:
     * <ul>
     * <li>extract HTTP messages from the network recordings</li>
     * <li>find HTTP requests matching the provided HTTP methods and the URL regular expression</li>
     * <li>check that total number of the found HTTP messages is equal to 1</li>
     * <li>save the HTTP message part to the variable</li>
     * </ul>
     *
     * @param httpMethods     The comma-separated set of HTTP methods to filter by, e.g. 'GET, POST, PUT'
     * @param urlPattern      The regular expression to match HTTP request URL
     * @param httpMessagePart The data to get from response<br>
     *                        <i>Available data:</i>
     *                        <ul>
     *                       <li><b>URL</b> - the request URL
     *                       <li><b>URL_QUERY</b> - the request query parameters
     *                       <li><b>REQUEST_DATA</b> - the request body and the response status code
     *                       <li><b>RESPONSE_DATA</b> - the response body
     *                       </ul>
     * @param scopes         The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                       <i>Available scopes:</i>
     *                       <ul>
     *                       <li><b>STEP</b> - the variable will be available only within the step,
     *                       <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                       <li><b>STORY</b> - the variable will be available within the whole story,
     *                       <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                       </ul>
     * @param variableName The variable name to save the URL.
     */
    @When("I capture HTTP $httpMethods request with URL pattern `$urlPattern` and save $httpMessagePart to $scopes "
            + "variable `$variableName`")
    public void captureRequestAndSaveHttpMessagePart(Set<String> httpMethods, Pattern urlPattern,
            HttpMessagePart httpMessagePart, Set<VariableScope> scopes, String variableName)
    {
        List<Request> matchedRecordings = findRecordings(httpMethods, urlPattern);
        String description = String.format("Number of HTTP %s requests matching URL pattern %s",
                methodsToString(httpMethods), urlPattern);
        softAssert.assertThat(description, matchedRecordings, hasSize(equalTo(1)));

        if (matchedRecordings.size() == 1)
        {
            Request request = matchedRecordings.get(0);
            variableContext.putVariable(scopes, variableName, httpMessagePart.get(request));
        }
    }

    /**
     * Waits for appearance of HTTP request matched <b>httpMethod</b> and <b>urlPattern</b> in network recordings.
     * @param httpMethods The comma-separated HTTP methods to filter by, e.g. 'GET, POST, PUT'
     * @param urlPattern  The regular expression to match HTTP request URL
     */
    @When("I wait until HTTP $httpMethods request with URL pattern `$urlPattern` is captured")
    public void waitRequestIsCaptured(Set<String> httpMethods, Pattern urlPattern)
    {
        BrowserContext browserContext = browserContextProvider.get();

        BooleanSupplier waitCondition = () -> !findRecordings(httpMethods, urlPattern).isEmpty();
        String assertionMessage = String.format("waiting for HTTP %s request with URL pattern %s",
                methodsToString(httpMethods), urlPattern);

        waitActions.runWithTimeoutAssertion(assertionMessage, () -> browserContext.waitForCondition(waitCondition));
    }

    /**
     * Clears the network recordings.
     */
    @When("I clear network recordings")
    public void clearNetworkRecordings()
    {
        List<Request> networkRecordings = networkContext.getNetworkRecordings();
        networkRecordings.clear();
    }

    private List<Request> findRecordings(Set<String> httpMethods, Pattern urlPattern)
    {
        List<Request> networkRecordings = networkContext.getNetworkRecordings();
        return networkRecordings.isEmpty() ? List.of()
                : networkRecordings.stream()
                        .filter(r -> httpMethods.contains(r.method()) && urlPattern.matcher(r.url()).matches())
                        .toList();
    }

    private String methodsToString(Set<String> httpMethods)
    {
        return String.join(", ", httpMethods);
    }
}
