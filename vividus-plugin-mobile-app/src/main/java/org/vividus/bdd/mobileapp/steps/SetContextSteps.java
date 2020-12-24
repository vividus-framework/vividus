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

package org.vividus.bdd.mobileapp.steps;

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.stream.Collectors;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.ContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IGenericWebDriverManager;
import org.vividus.softassert.ISoftAssert;

public class SetContextSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SetContextSteps.class);

    private final IWebDriverProvider webDriverProvider;
    private final ISoftAssert softAssert;

    public SetContextSteps(IWebDriverProvider webDriverProvider, ISoftAssert softAssert)
    {
        this.webDriverProvider = webDriverProvider;
        this.softAssert = softAssert;
    }

    /**
     * Switches context to a mobile native context
     */
    @When("I switch to native context")
    public void switchToNativeContext()
    {
        getContextAware().context(IGenericWebDriverManager.NATIVE_APP_CONTEXT);
    }

    /**
     * Switches context to a web view by the index, it starts from 1
     * @param index index of web view
     */
    @When("I switch to web view with index `$index`")
    public void switchToWebView(int index)
    {
        List<String> webViews = getWebViews();
        if (webViews.isEmpty())
        {
            softAssert.recordFailedAssertion("No web views found");
            return;
        }

        LOGGER.atInfo().addArgument(webViews::toString).log("Web views found: {}");

        int webViewIndex = index - 1;
        int size = webViews.size();
        if (webViewIndex >= 0 && webViewIndex < size)
        {
            String webview = webViews.get(webViewIndex);
            LOGGER.atInfo().addArgument(webview::toString)
                           .addArgument(index)
                           .log("Switching to '{}' web view found by the index {}");
            getContextAware().context(webview);
            return;
        }

        softAssert.recordFailedAssertion(String.format("Web view with index %s does not exist", index));
    }

    private List<String> getWebViews()
    {
        return getContextAware().getContextHandles()
                                .stream()
                                .filter(not(IGenericWebDriverManager.NATIVE_APP_CONTEXT::equals))
                                .collect(Collectors.toList());
    }

    private ContextAware getContextAware()
    {
        return webDriverProvider.getUnwrapped(ContextAware.class);
    }
}
