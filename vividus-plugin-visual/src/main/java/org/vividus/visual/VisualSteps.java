/*
 * Copyright 2019-2022 the original author or authors.
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
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ConvertedParameters;
import org.jbehave.core.steps.Parameters;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.resource.ResourceLoadException;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.ui.screenshot.ScreenshotParametersFactory;
import org.vividus.visual.engine.IVisualTestingEngine;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.model.VisualCheckResult;
import org.vividus.visual.screenshot.BaselineIndexer;
import org.vividus.visual.steps.AbstractVisualSteps;

public class VisualSteps extends AbstractVisualSteps
{
    private static final Type SET_BY = new TypeToken<Set<Locator>>() { }.getType();

    private static final String ACCEPTABLE_DIFF_PERCENTAGE_COLUMN_NAME = "ACCEPTABLE_DIFF_PERCENTAGE";
    private static final String REQUIRED_DIFF_PERCENTAGE_COLUMN_NAME = "REQUIRED_DIFF_PERCENTAGE";

    private final IVisualTestingEngine visualTestingEngine;
    private final ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory;
    private final BaselineIndexer baselineIndexer;

    public VisualSteps(IUiContext uiContext, IAttachmentPublisher attachmentPublisher,
            IVisualTestingEngine visualTestingEngine, ISoftAssert softAssert,
            ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory,
            BaselineIndexer baselineIndexer)
    {
        super(uiContext, attachmentPublisher, softAssert);
        this.visualTestingEngine = visualTestingEngine;
        this.screenshotParametersFactory = screenshotParametersFactory;
        this.baselineIndexer = baselineIndexer;
    }

    /**
     * Step establishes baseline or compares against existing one.
     *
     * @param actionType ESTABLISH, COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST
     * @param name       The baseline name
     */
    @When("I $actionType baseline with name `$name`")
    public void runVisualTests(VisualActionType actionType, String name)
    {
        performVisualAction(name, actionType, Optional.empty(), Optional.empty());
    }

    /**
     * Step establishes baseline or compares against existing one.
     *
     * @param actionType ESTABLISH, COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST
     * @param name       The baseline name
     * @param storage    The baseline storage name
     */
    @When(value = "I $actionType baseline with name `$name` using storage `$storage`", priority = 1)
    public void runVisualTests(VisualActionType actionType, String name, String storage)
    {
        performVisualAction(name, actionType, Optional.of(storage), Optional.empty());
    }

    /**
     * Step establishes baseline or compares against existing one.
     *
     * @param actionType              ESTABLISH, COMPARE_AGAINST, or CHECK_INEQUALITY_AGAINST
     * @param name                    The baseline name
     * @param screenshotConfiguration configuration to make screenshot
     *                                Example:<br>
     *                                |scrollableElement  |webFooterToCut|webHeaderToCut|coordsProvider|<br>
     *                                |By.xpath(.//header)|100           |100           |CEILING       |
     */
    @When("I $actionType baseline with name `$name` using screenshot configuration:$screenshotConfiguration")
    public void runVisualTests(VisualActionType actionType, String name,
            ScreenshotConfiguration screenshotConfiguration)
    {
        performVisualAction(name, actionType, Optional.empty(), Optional.of(screenshotConfiguration));
    }

    /**
     * Step establishes baseline or compares against existing one.
     *
     * @param actionType              ESTABLISH, COMPARE_AGAINST, or CHECK_INEQUALITY_AGAINST
     * @param name                    The baseline name
     * @param storage                 The baseline storage name
     * @param screenshotConfiguration configuration to make screenshot
     *                                Example:<br>
     *                                |scrollableElement  |webFooterToCut|webHeaderToCut|coordsProvider|<br>
     *                                |By.xpath(.//header)|100           |100           |CEILING       |
     */
    @When(value = "I $actionType baseline with name `$name` using storage `$storage` and"
            + " screenshot configuration:$screenshotConfiguration", priority = 1)
    public void runVisualTests(VisualActionType actionType, String name, String storage,
            ScreenshotConfiguration screenshotConfiguration)
    {
        performVisualAction(name, actionType, Optional.of(storage), Optional.of(screenshotConfiguration));
    }

    /**
     * Step establishes baseline or compares against existing one.
     * @param actionType ESTABLISH, COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST
     * @param name of baseline
     * @param checkSettings examples table of `ELEMENT`, `AREA`, `ACCEPTABLE_DIFF_PERCENTAGE`
     *                      or `REQUIRED_DIFF_PERCENTAGE`<br>
     * Example:<br>
     * |ELEMENT            |AREA                  |<br>
     * |By.xpath(.//header)|By.cssSelector(footer)|
     */
    @When("I $actionType baseline with name `$name` ignoring:$checkSettings")
    public void runVisualTests(VisualActionType actionType, String name, ExamplesTable checkSettings)
    {
        performVisualAction(checkSettings, name, actionType, Optional.empty(), Optional.empty());
    }

    /**
     * Step establishes baseline or compares against existing one.
     *
     * @param actionType    ESTABLISH, COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST
     * @param name          The name of baseline
     * @param storage       The baseline storage name
     * @param checkSettings examples table of `ELEMENT`, `AREA`, `ACCEPTABLE_DIFF_PERCENTAGE`
     *                      or `REQUIRED_DIFF_PERCENTAGE`<br>
     *                      Example:<br>
     *                      |ELEMENT            |AREA                  |<br>
     *                      |By.xpath(.//header)|By.cssSelector(footer)|
     */
    @When(value = "I $actionType baseline with name `$name` using storage `$storage` and ignoring:$checkSettings",
        priority = 1)
    public void runVisualTests(VisualActionType actionType, String name, String storage, ExamplesTable checkSettings)
    {
        performVisualAction(checkSettings, name, actionType, Optional.of(storage), Optional.empty());
    }

    /**
     * Step establishes baseline or compares against existing one.
     *
     * @param actionType              ESTABLISH, COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST
     * @param name                    The baseline name
     * @param checkSettings           examples table of `ELEMENT`, `AREA`, `ACCEPTABLE_DIFF_PERCENTAGE`
     *                                or `REQUIRED_DIFF_PERCENTAGE`<br>
     *                                Example:<br>
     *                                |ELEMENT            |AREA                  |<br>
     *                                |By.xpath(.//header)|By.cssSelector(footer)|
     * @param screenshotConfiguration configuration to make screenshot
     *                                Example:<br>
     *                                |scrollableElement  |webFooterToCut|webHeaderToCut|coordsProvider|<br>
     *                                |By.xpath(.//header)|100           |100           |CEILING       |
     */
    @When(value = "I $actionType baseline with name `$name` ignoring:$checkSettings using"
            + " screenshot configuration:$screenshotConfiguration", priority = 1)
    public void runVisualTests(VisualActionType actionType, String name, ExamplesTable checkSettings,
            ScreenshotConfiguration screenshotConfiguration)
    {
        performVisualAction(checkSettings, name, actionType, Optional.empty(), Optional.of(screenshotConfiguration));
    }

    /**
     * Step establishes baseline or compares against existing one.
     *
     * @param actionType              ESTABLISH, COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST
     * @param name                    The baseline name
     * @param storage                 The baseline storage name
     * @param checkSettings           examples table of `ELEMENT`, `AREA`, `ACCEPTABLE_DIFF_PERCENTAGE`
     *                                or `REQUIRED_DIFF_PERCENTAGE`<br>
     *                                Example:<br>
     *                                |ELEMENT            |AREA                  |<br>
     *                                |By.xpath(.//header)|By.cssSelector(footer)|
     * @param screenshotConfiguration configuration to make screenshot
     *                                Example:<br>
     *                                |scrollableElement  |webFooterToCut|webHeaderToCut|coordsProvider|<br>
     *                                |By.xpath(.//header)|100           |100           |CEILING       |
     */
    @When(value = "I $actionType baseline with name `$name` using storage `$storage` and ignoring"
            + ":$checkSettings and screenshot configuration:$screenshotConfiguration", priority = 2)
    public void runVisualTests(VisualActionType actionType, String name, String storage, ExamplesTable checkSettings,
            ScreenshotConfiguration screenshotConfiguration)
    {
        performVisualAction(checkSettings, name, actionType, Optional.of(storage),
                Optional.of(screenshotConfiguration));
    }

    private void performVisualAction(ExamplesTable checkSettingsTable, String baselineName, VisualActionType actionType,
            Optional<String> baselineStorage, Optional<ScreenshotConfiguration> screenshotConfiguration)
    {
        int rowsSize = checkSettingsTable.getRows().size();
        Validate.isTrue(rowsSize == 1, "Only one row of locators to ignore supported, actual: %s", rowsSize);
        Parameters checkSettings = checkSettingsTable.getRowAsParameters(0);

        performVisualAction(baselineName, actionType, baselineStorage, screenshotConfiguration, checkSettings);
    }

    private void performVisualAction(String baselineName, VisualActionType actionType,
            Optional<String> baselineStorage, Optional<ScreenshotConfiguration> screenshotConfiguration)
    {
        performVisualAction(baselineName, actionType, baselineStorage, screenshotConfiguration,
                new ConvertedParameters(Map.of(), null));
    }

    private void performVisualAction(String baselineName, VisualActionType actionType,
            Optional<String> baselineStorage, Optional<ScreenshotConfiguration> screenshotConfiguration,
            Parameters checkSettings)
    {
        Supplier<VisualCheck> visualCheckFactory = () -> {
            Map<IgnoreStrategy, Set<Locator>> ignores = getIgnores(checkSettings);

            Optional<ScreenshotParameters> screenshotParameters;
            if (screenshotConfiguration.isPresent())
            {
                patchIgnores("ignores table", screenshotConfiguration.get(), ignores);
                screenshotParameters = screenshotParametersFactory.create(screenshotConfiguration);
            }
            else
            {
                screenshotParameters = screenshotParametersFactory.create(ignores);
            }
            String indexedBaselineName = baselineIndexer.createIndexedBaseline(baselineName);

            VisualCheck visualCheck = new VisualCheck(indexedBaselineName, actionType);
            visualCheck.setScreenshotParameters(screenshotParameters);
            visualCheck.setBaselineStorage(baselineStorage);
            setDiffPercentage(visualCheck, checkSettings);
            return visualCheck;
        };
        Function<VisualCheck, VisualCheckResult> checkResultProvider = check -> {
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
        };
        execute(visualCheckFactory, checkResultProvider, "visual-comparison.ftl");
    }

    private Map<IgnoreStrategy, Set<Locator>> getIgnores(Parameters checkParameters)
    {
        return Stream.of(IgnoreStrategy.values()).collect(Collectors.toMap(Function.identity(),
                s -> checkParameters.valueAs(s.name(), SET_BY, Set.of())));
    }

    private void setDiffPercentage(VisualCheck visualCheck, Parameters rowAsParameters)
    {
        if (visualCheck.getAction() == VisualActionType.CHECK_INEQUALITY_AGAINST)
        {
            configureThresholdIfPresent(rowAsParameters, REQUIRED_DIFF_PERCENTAGE_COLUMN_NAME,
                    visualCheck::setRequiredDiffPercentage);
        }
        else
        {
            configureThresholdIfPresent(rowAsParameters, ACCEPTABLE_DIFF_PERCENTAGE_COLUMN_NAME,
                    visualCheck::setAcceptableDiffPercentage);
        }
    }

    private void configureThresholdIfPresent(Parameters rowAsParameters, String parameterName,
            Consumer<OptionalDouble> thresholdSetter)
    {
        if (rowAsParameters.values().containsKey(parameterName))
        {
            thresholdSetter.accept(OptionalDouble.of(rowAsParameters.valueAs(parameterName, Double.TYPE)));
        }
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
