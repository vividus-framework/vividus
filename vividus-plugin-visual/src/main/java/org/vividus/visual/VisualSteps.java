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

package org.vividus.visual;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.reflect.TypeToken;

import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.vividus.bdd.resource.ResourceLoadException;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.screenshot.ScreenshotConfiguration;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.visual.bdd.AbstractVisualSteps;
import org.vividus.visual.engine.IVisualTestingEngine;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.model.VisualCheckResult;
import org.vividus.visual.screenshot.IgnoreStrategy;

public class VisualSteps extends AbstractVisualSteps
{
    private static final Type SET_BY = new TypeToken<Set<Locator>>() { }.getType();

    private final IVisualTestingEngine visualTestingEngine;
    private final ISoftAssert softAssert;
    private final IVisualCheckFactory visualCheckFactory;

    public VisualSteps(IUiContext uiContext, IAttachmentPublisher attachmentPublisher,
            IVisualTestingEngine visualTestingEngine, ISoftAssert softAssert, IVisualCheckFactory visualCheckFactory)
    {
        super(uiContext, attachmentPublisher);
        this.visualTestingEngine = visualTestingEngine;
        this.softAssert = softAssert;
        this.visualCheckFactory = visualCheckFactory;
    }

    /**
     * Step establishes baseline or compares against existing one.
     * @param actionType ESTABLISH, COMPARE_AGAINST
     * @param name of baseline
     */
    @When("I $actionType baseline with `$name`")
    public void runVisualTests(VisualActionType actionType, String name)
    {
        performVisualAction(() -> visualCheckFactory.create(name, actionType));
    }

    /**
     * Step establishes baseline or compares against existing one.
     * @param actionType ESTABLISH, COMPARE_AGAINST
     * @param name of baseline
     * @param screenshotConfiguration to make screenshot
     * Example:<br>
     * |scrollableElement  |webFooterToCut|webHeaderToCut|coordsProvider|<br>
     * |By.xpath(.//header)|100           |100           |CEILING       |
     */
    @When("I $actionType baseline with `$name` using screenshot configuration:$screenshotConfiguration")
    public void runVisualTests(VisualActionType actionType, String name,
            ScreenshotConfiguration screenshotConfiguration)
    {
        performVisualAction(() -> visualCheckFactory.create(name, actionType, screenshotConfiguration));
    }

    private void performVisualAction(Supplier<VisualCheck> visualCheckFactory)
    {
        execute(check -> {
            VisualCheckResult visualCheckResult = new VisualCheckResult(check);
            try
            {
                if (check.getAction() == VisualActionType.COMPARE_AGAINST)
                {
                    visualCheckResult = visualTestingEngine.compareAgainst(check);
                    if (visualCheckResult.getBaseline() == null)
                    {
                        softAssert.recordFailedAssertion(
                                "Unable to find baseline with name: " + check.getBaselineName());
                    }
                    else
                    {
                        softAssert.assertTrue("Visual check passed", visualCheckResult.isPassed());
                    }
                }
                else
                {
                    visualCheckResult = visualTestingEngine.establish(check);
                }
                return visualCheckResult;
            }
            catch (IOException | ResourceLoadException e)
            {
                softAssert.recordFailedAssertion(e);
            }
            return null;
        }, visualCheckFactory, "visual-comparison.ftl");
    }

    /**
     * Step establishes baseline or compares against existing one.
     * @param actionType ESTABLISH, COMPARE_AGAINST
     * @param name of baseline
     * @param ignoredElements examples table of strategies (ELEMENT, AREA) and locators to ignore<br>
     * Example:<br>
     * |ELEMENT            |AREA                  |<br>
     * |By.xpath(.//header)|By.cssSelector(footer)|
     */
    @When("I $actionType baseline with `$name` ignoring:$ignoredElements")
    public void runVisualTests(VisualActionType actionType, String name, ExamplesTable ignoredElements)
    {
        runVisualTests(() -> visualCheckFactory.create(name, actionType), ignoredElements);
    }

    /**
     * Step establishes baseline or compares against existing one.
     * @param actionType ESTABLISH, COMPARE_AGAINST
     * @param name of baseline
     * @param ignoredElements examples table of strategies (ELEMENT, AREA) and locators to ignore<br>
     * Example:<br>
     * |ELEMENT            |AREA                  |<br>
     * |By.xpath(.//header)|By.cssSelector(footer)|
     * @param screenshotConfiguration to make screenshot
     * Example:<br>
     * |scrollableElement  |webFooterToCut|webHeaderToCut|coordsProvider|<br>
     * |By.xpath(.//header)|100           |100           |CEILING       |
     */
    @When(value = "I $actionType baseline with `$name` ignoring:$ignoredElements using"
            + " screenshot configuration:$screenshotConfiguration", priority = 1)
    public void runVisualTests(VisualActionType actionType, String name, ExamplesTable ignoredElements,
            ScreenshotConfiguration screenshotConfiguration)
    {
        runVisualTests(() -> visualCheckFactory.create(name, actionType, screenshotConfiguration), ignoredElements);
    }

    private void runVisualTests(Supplier<VisualCheck> visualCheckFactory, ExamplesTable ignoredElements)
    {
        int rowsSize = ignoredElements.getRows().size();
        if (rowsSize != 1)
        {
            throw new IllegalArgumentException("Only one row of locators to ignore supported, actual: "
            + rowsSize);
        }
        Parameters rowAsParameters = ignoredElements.getRowAsParameters(0);
        Map<IgnoreStrategy, Set<Locator>> toIgnore = Stream.of(IgnoreStrategy.values())
                                                      .collect(Collectors.toMap(Function.identity(),
                                                          s -> getLocatorsSet(rowAsParameters, s)));
        performVisualAction(() -> {
            VisualCheck visualCheck = visualCheckFactory.get();
            visualCheck.setElementsToIgnore(toIgnore);
            return visualCheck;
        });
    }

    private Set<Locator> getLocatorsSet(Parameters rowAsParameters, IgnoreStrategy s)
    {
        return rowAsParameters.valueAs(s.name(), SET_BY, Set.of());
    }
}
