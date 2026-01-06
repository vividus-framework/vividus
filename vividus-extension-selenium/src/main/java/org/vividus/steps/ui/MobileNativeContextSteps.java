/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.steps.ui;

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IGenericWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.SubSteps;

import io.appium.java_client.remote.SupportsContextSwitching;

public class MobileNativeContextSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MobileNativeContextSteps.class);

    private final IWebDriverProvider webDriverProvider;
    private final IGenericWebDriverManager webDriverManager;
    private final ISoftAssert softAssert;

    public MobileNativeContextSteps(IWebDriverProvider webDriverProvider, IGenericWebDriverManager webDriverManager,
            ISoftAssert softAssert)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverManager = webDriverManager;
        this.softAssert = softAssert;
    }

    /**
     * Switches context to a mobile native context
     */
    @When("I switch to native context")
    public void switchToNativeContext()
    {
        getContextSwitchingDriver().context(IGenericWebDriverManager.NATIVE_APP_CONTEXT);
    }

    /**
     * Switches context to a web view where name matches the rule
     *
     * @param rule  The web view name comparison rule: "is equal to", "contains", "does not contain" or "matches"
     * @param value The value to find the target web view
     */
    @When("I switch to web view with name that $comparisonRule `$value`")
    public void switchToWebViewByName(StringComparisonRule rule, String value)
    {
        SupportsContextSwitching contextSwitchingDriver = getContextSwitchingDriver();
        List<String> webViews = contextSwitchingDriver.getContextHandles()
                .stream()
                .filter(not(IGenericWebDriverManager.NATIVE_APP_CONTEXT::equals))
                .toList();
        if (webViews.isEmpty())
        {
            softAssert.recordFailedAssertion("No web views found");
            return;
        }

        LOGGER.atInfo().addArgument(webViews::toString).log("Web views found: {}");

        Matcher<String> webViewMatcher = rule.createMatcher(value);
        Predicate<String> webViewFilter = webViewMatcher::matches;

        List<String> matchedWebViews = webViews.stream().filter(webViewFilter).toList();

        if (matchedWebViews.size() != 1)
        {
            softAssert.recordFailedAssertion(
                    String.format("The number of web views with name that %s '%s' is expected to be 1, but got %d",
                            rule, value, matchedWebViews.size()));
        }
        else
        {
            String webView = matchedWebViews.get(0);
            LOGGER.atInfo().addArgument(webView).log("Switching to web view with the name '{}'");
            contextSwitchingDriver.context(webView);
        }
    }

    /**
     * Executes the provided steps in the mobile native context. If the current context is not the mobile native
     * context, it switches to the mobile native context, executes the provided steps, and then switches back to the
     * original context.
     *
     * @param stepsToExecute The ExamplesTable with a single column containing the steps to execute
     */
    @When("I execute steps in native context:$stepsToExecute")
    public void executeStepsInNativeContext(SubSteps stepsToExecute)
    {
        webDriverManager.performActionInNativeContext(driver -> stepsToExecute.execute(Optional.empty()));
    }

    private SupportsContextSwitching getContextSwitchingDriver()
    {
        return webDriverProvider.getUnwrapped(SupportsContextSwitching.class);
    }
}
