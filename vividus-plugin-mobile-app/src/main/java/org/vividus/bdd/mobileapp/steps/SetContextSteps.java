/*
 * Copyright 2019-2021 the original author or authors.
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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.ContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.util.EnumUtils;
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
    public void switchToWebViewByIndex(int index)
    {
        performOnWebViews(webViews ->
        {
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
        });
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
        performOnWebViews(webViews ->
        {
            Matcher<String> webViewMatcher = rule.createMatcher(value);
            Predicate<String> webViewFilter = webViewMatcher::matches;

            List<String> matchedWebViews = webViews.stream()
                                                   .filter(webViewFilter)
                                                   .collect(Collectors.toList());

            if (matchedWebViews.size() == 1)
            {
                String webView = matchedWebViews.get(0);
                LOGGER.atInfo().addArgument(webView).log("Switching to web view with the name '{}'");
                getContextAware().context(webView);
                return;
            }

            softAssert.recordFailedAssertion(
                    String.format("The number of web views with name that %s '%s' is expected to be 1, but got %d",
                            EnumUtils.toHumanReadableForm(rule), value, matchedWebViews.size()));
        });
    }

    private void performOnWebViews(Consumer<List<String>> webViewConsumer)
    {
        List<String> webViews = getWebViews();
        if (webViews.isEmpty())
        {
            softAssert.recordFailedAssertion("No web views found");
            return;
        }

        LOGGER.atInfo().addArgument(webViews::toString).log("Web views found: {}");

        webViewConsumer.accept(webViews);
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
