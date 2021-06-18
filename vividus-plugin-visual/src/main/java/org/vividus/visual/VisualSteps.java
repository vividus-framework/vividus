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

package org.vividus.visual;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.OptionalInt;
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

    private static final String ACCEPTABLE_DIFF_PERCENTAGE_COLUMN_NAME = "ACCEPTABLE_DIFF_PERCENTAGE";
    private static final String REQUIRED_DIFF_PERCENTAGE_COLUMN_NAME = "REQUIRED_DIFF_PERCENTAGE";

    private final IVisualTestingEngine visualTestingEngine;
    private final IVisualCheckFactory visualCheckFactory;

    public VisualSteps(IUiContext uiContext, IAttachmentPublisher attachmentPublisher,
            IVisualTestingEngine visualTestingEngine, ISoftAssert softAssert, IVisualCheckFactory visualCheckFactory)
    {
        super(uiContext, attachmentPublisher, softAssert);
        this.visualTestingEngine = visualTestingEngine;
        this.visualCheckFactory = visualCheckFactory;
    }

    /**
     * Step establishes baseline or compares against existing one.
     * @param actionType ESTABLISH, COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST
     * @param name of baseline
     */
    @When("I $actionType baseline with `$name`")
    public void runVisualTests(VisualActionType actionType, String name)
    {
        performVisualAction(() -> visualCheckFactory.create(name, actionType));
    }

    /**
     * Step establishes baseline or compares against existing one.
     * @param actionType ESTABLISH, COMPARE_AGAINST, or CHECK_INEQUALITY_AGAINST
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
            try
            {
                return check.getAction() == VisualActionType.ESTABLISH
                                         ? visualTestingEngine.establish(check)
                                         : visualTestingEngine.compareAgainst(check);
            }
            catch (IOException | ResourceLoadException e)
            {
                getSoftAssert().recordFailedAssertion(e);
            }
            return null;
        }, visualCheckFactory, "visual-comparison.ftl");
    }

    /**
     * Step establishes baseline or compares against existing one.
     * @param actionType ESTABLISH, COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST
     * @param name of baseline
     * @param checkSettings examples table of `ELEMENT`, `AREA`, `ACCEPTABLE_DIFF_PERCENTAGE`
     *                      or `REQUIRED_DIFF_PERCANTAGE`<br>
     * Example:<br>
     * |ELEMENT            |AREA                  |<br>
     * |By.xpath(.//header)|By.cssSelector(footer)|
     */
    @When("I $actionType baseline with `$name` ignoring:$checkSettings")
    public void runVisualTests(VisualActionType actionType, String name, ExamplesTable checkSettings)
    {
        runVisualTests(() -> visualCheckFactory.create(name, actionType), checkSettings);
    }

    /**
     * Step establishes baseline or compares against existing one.
     * @param actionType ESTABLISH, COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST
     * @param name of baseline
     * @param checkSettings examples table of `ELEMENT`, `AREA`, `ACCEPTABLE_DIFF_PERCENTAGE`
     *                      or `REQUIRED_DIFF_PERCANTAGE`<br>
     * Example:<br>
     * |ELEMENT            |AREA                  |<br>
     * |By.xpath(.//header)|By.cssSelector(footer)|
     * @param screenshotConfiguration to make screenshot
     * Example:<br>
     * |scrollableElement  |webFooterToCut|webHeaderToCut|coordsProvider|<br>
     * |By.xpath(.//header)|100           |100           |CEILING       |
     */
    @When(value = "I $actionType baseline with `$name` ignoring:$checkSettings using"
            + " screenshot configuration:$screenshotConfiguration", priority = 1)
    public void runVisualTests(VisualActionType actionType, String name, ExamplesTable checkSettings,
            ScreenshotConfiguration screenshotConfiguration)
    {
        runVisualTests(() -> visualCheckFactory.create(name, actionType, screenshotConfiguration), checkSettings);
    }

    private void runVisualTests(Supplier<VisualCheck> visualCheckFactory, ExamplesTable checkSettings)
    {
        int rowsSize = checkSettings.getRows().size();
        if (rowsSize != 1)
        {
            throw new IllegalArgumentException("Only one row of locators to ignore supported, actual: "
            + rowsSize);
        }
        Parameters rowAsParameters = checkSettings.getRowAsParameters(0);
        Map<IgnoreStrategy, Set<Locator>> toIgnore = Stream.of(IgnoreStrategy.values())
                                                      .collect(Collectors.toMap(Function.identity(),
                                                          s -> getLocatorsSet(rowAsParameters, s)));

        performVisualAction(() -> {
            VisualCheck visualCheck = visualCheckFactory.get();
            visualCheck.setElementsToIgnore(toIgnore);
            setDiffPercentage(visualCheck, rowAsParameters);
            return visualCheck;
        });
    }

    private void setDiffPercentage(VisualCheck visualCheck, Parameters rowAsParameters)
    {
        if (visualCheck.getAction() == VisualActionType.CHECK_INEQUALITY_AGAINST)
        {
            visualCheck.setRequiredDiffPercentage(getParameter(rowAsParameters, REQUIRED_DIFF_PERCENTAGE_COLUMN_NAME));
        }
        else
        {
            visualCheck.setAcceptableDiffPercentage(getParameter(rowAsParameters,
                    ACCEPTABLE_DIFF_PERCENTAGE_COLUMN_NAME));
        }
    }

    private OptionalInt getParameter(Parameters rowAsParameters, String paramterName)
    {
        return rowAsParameters.values().containsKey(paramterName)
                ? OptionalInt.of(rowAsParameters.valueAs(paramterName, Integer.TYPE))
                : OptionalInt.empty();
    }

    private Set<Locator> getLocatorsSet(Parameters rowAsParameters, IgnoreStrategy s)
    {
        return rowAsParameters.valueAs(s.name(), SET_BY, Set.of());
    }

    @Override
    protected void verifyResult(VisualCheckResult result)
    {
        if (result.getActionType() == VisualActionType.ESTABLISH)
        {
            return;
        }
        if (result.getBaseline() == null)
        {
            getSoftAssert().recordFailedAssertion(
                    "Unable to find baseline with name: " + result.getBaselineName());
        }
        else
        {
            super.verifyResult(result);
        }
    }
}
