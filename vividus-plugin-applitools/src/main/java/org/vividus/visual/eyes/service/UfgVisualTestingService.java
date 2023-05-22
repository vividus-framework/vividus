/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.visual.eyes.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.applitools.eyes.Padding;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import com.applitools.eyes.selenium.fluent.Target;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverUtils;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.visual.eyes.factory.UfgEyesFactory;
import org.vividus.visual.eyes.model.ApplitoolsTestResults;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.eyes.model.UfgApplitoolsVisualCheckResult;
import org.vividus.visual.eyes.ufg.UfgEyes;

public class UfgVisualTestingService implements VisualTestingService<UfgApplitoolsVisualCheckResult>
{
    private static final int PADDING = 10_000;

    private final UfgEyesFactory eyesFactory;
    private final IWebDriverProvider webDriverProvider;
    private final ISearchActions searchActions;

    public UfgVisualTestingService(UfgEyesFactory eyesFactory, IWebDriverProvider webDriverProvider,
            ISearchActions searchActions)
    {
        this.eyesFactory = eyesFactory;
        this.webDriverProvider = webDriverProvider;
        this.searchActions = searchActions;
    }

    @Override
    public UfgApplitoolsVisualCheckResult run(ApplitoolsVisualCheck applitoolsVisualCheck)
    {
        UfgEyes eyes = eyesFactory.createEyes(applitoolsVisualCheck);
        List<ApplitoolsTestResults> testResults = List.of();

        eyes.open(webDriverProvider.getUnwrapped(RemoteWebDriver.class),
                applitoolsVisualCheck.getConfiguration().getAppName(), applitoolsVisualCheck.getBaselineName());

        try
        {
            SearchContext searchContext = applitoolsVisualCheck.getSearchContext();
            SeleniumCheckSettings target = searchContext instanceof WebElement
                    ? Target.region(asRemote((WebElement) searchContext))
                    : Target.window();
            target = computeIgnores(target, applitoolsVisualCheck.getElementsToIgnore(), new Padding());
            target = computeIgnores(target, applitoolsVisualCheck.getAreasToIgnore(),
                    new Padding(0, PADDING, 0, PADDING));
            eyes.check(target);
        }
        finally
        {
            eyes.close(false);
            testResults = eyes.getTestResults();
        }
        return createVisualCheckResult(testResults, applitoolsVisualCheck);
    }

    private SeleniumCheckSettings computeIgnores(SeleniumCheckSettings base, Set<Locator> ignores, Padding padding)
    {
        return ignores.stream().map(searchActions::findElements)
                               .flatMap(Collection::stream)
                               .map(this::asRemote)
                               .reduce(base, (settings, element) -> settings.ignore(element, padding), (l, r) -> l);
    }

    private RemoteWebElement asRemote(WebElement element)
    {
        return WebDriverUtils.unwrap(element, RemoteWebElement.class);
    }

    private UfgApplitoolsVisualCheckResult createVisualCheckResult(List<ApplitoolsTestResults> testResults,
            ApplitoolsVisualCheck applitoolsVisualCheck)
    {
        UfgApplitoolsVisualCheckResult visualCheckResult = new UfgApplitoolsVisualCheckResult(applitoolsVisualCheck);
        boolean passed = testResults.stream().map(ApplitoolsTestResults::isPassed).allMatch(testPassed -> testPassed);
        visualCheckResult.setPassed(passed);
        visualCheckResult.setTestResults(testResults);
        return visualCheckResult;
    }
}
