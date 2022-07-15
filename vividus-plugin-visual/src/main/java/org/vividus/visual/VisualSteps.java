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
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
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

import pazone.ashot.Screenshot;
import pazone.ashot.util.ImageTool;

public class VisualSteps extends AbstractVisualSteps
{
    private static final Type SET_BY = new TypeToken<Set<Locator>>() { }.getType();

    private static final ConvertedParameters EMPTY_CHECK_SETTINGS = new ConvertedParameters(Map.of(), null);

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

    /**
     * Step establishes baseline or compares against existing one.
     *
     * @param actionType   ESTABLISH, COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST.
     * @param baselineName The baseline name.
     * @param image        The image to check.
     */
    @When(value = "I $actionType baseline with name `$name` from image `$image`", priority = 1)
    public void runVisualTests(VisualActionType actionType, String baselineName, byte[] image)
    {
        performVisualAction(baselineName, actionType, Optional.empty(), EMPTY_CHECK_SETTINGS, image);
    }

    /**
     * Step establishes baseline or compares against existing one.
     *
     * @param actionType   ESTABLISH, COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST.
     * @param baselineName The baseline name.
     * @param image        The image to check.
     * @param storage      The baseline storage name
     */
    @When(value = "I $actionType baseline with name `$name` from image `$image` using storage `$storage`", priority = 2)
    public void runVisualTests(VisualActionType actionType, String baselineName, byte[] image, String storage)
    {
        performVisualAction(baselineName, actionType, Optional.of(storage), EMPTY_CHECK_SETTINGS, image);
    }

    /**
     * Step establishes baseline or compares against existing one.
     *
     * @param actionType    ESTABLISH, COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST.
     * @param baselineName  The baseline name.
     * @param image         The image to check.
     * @param checkSettings The examples table containing `ACCEPTABLE_DIFF_PERCENTAGE` or `REQUIRED_DIFF_PERCENTAGE`<br>
     *                      Example:<br>
     *                      |ACCEPTABLE_DIFF_PERCENTAGE |REQUIRED_DIFF_PERCENTAGE|<br>
     *                      |1                          |99                      |
     */
    @When(value = "I $actionType baseline with name `$name` from image `$image` ignoring:$checkSettings", priority = 2)
    public void runVisualTests(VisualActionType actionType, String baselineName, byte[] image,
            ExamplesTable checkSettings)
    {
        runVisualTests(actionType, baselineName, image, Optional.empty(), checkSettings);
    }

    /**
     * Step establishes baseline or compares against existing one.
     *
     * @param actionType    ESTABLISH, COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST.
     * @param baselineName  The baseline name.
     * @param image         The image to check.
     * @param storage       The baseline storage name
     * @param checkSettings The examples table containing `ACCEPTABLE_DIFF_PERCENTAGE` or `REQUIRED_DIFF_PERCENTAGE`<br>
     *                      Example:<br>
     *                      |ACCEPTABLE_DIFF_PERCENTAGE |REQUIRED_DIFF_PERCENTAGE|<br>
     *                      |1                          |99                      |
     */
    @When(value = "I $actionType baseline with name `$name` from image `$image` using storage "
        + "`$storage` and ignoring:$checkSettings", priority = 2)
    public void runVisualTests(VisualActionType actionType, String baselineName, byte[] image, String storage,
            ExamplesTable checkSettings)
    {
        runVisualTests(actionType, baselineName, image, Optional.of(storage), checkSettings);
    }

    private void runVisualTests(VisualActionType actionType, String baselineName, byte[] image,
            Optional<String> storage, ExamplesTable checkSettings)
    {
        Parameters parameters = toParameters(checkSettings);
        Map<String, String> values = parameters.values();
        Validate.isTrue(!values.containsKey("AREA") && !values.containsKey("ELEMENT"),
                "AREA and ELEMENT ignoring not supported for image based checks.");
        performVisualAction(baselineName, actionType, storage, parameters, image);
    }

    private void performVisualAction(String baselineName, VisualActionType actionType,
            Optional<String> baselineStorage, Parameters checkSettings, byte[] image)
    {
        performVisualAction(baselineName, actionType, baselineStorage, checkSettings, vc -> {
            try
            {
                vc.setScreenshot(Optional.of(new Screenshot(ImageTool.toBufferedImage(image))));
                return vc;
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
        }, vc -> executeWithoutContext(vc, getCheckResultProvider()));
    }

    private void performVisualAction(ExamplesTable checkSettingsTable, String baselineName, VisualActionType actionType,
            Optional<String> baselineStorage, Optional<ScreenshotConfiguration> screenshotConfiguration)
    {
        Parameters checkSettings = toParameters(checkSettingsTable);

        performVisualAction(baselineName, actionType, baselineStorage, screenshotConfiguration, checkSettings);
    }

    private Parameters toParameters(ExamplesTable checkSettingsTable)
    {
        int rowsSize = checkSettingsTable.getRows().size();
        Validate.isTrue(rowsSize == 1, "Only one row of locators to ignore supported, actual: %s", rowsSize);
        return checkSettingsTable.getRowAsParameters(0);
    }

    private void performVisualAction(String baselineName, VisualActionType actionType,
            Optional<String> baselineStorage, Optional<ScreenshotConfiguration> screenshotConfiguration)
    {
        performVisualAction(baselineName, actionType, baselineStorage, screenshotConfiguration, EMPTY_CHECK_SETTINGS);
    }

    private void performVisualAction(String baselineName, VisualActionType actionType, Optional<String> baselineStorage,
            Optional<ScreenshotConfiguration> screenshotConfiguration, Parameters checkSettings)
    {
        performVisualAction(baselineName, actionType, baselineStorage, checkSettings, vc -> {
            Map<IgnoreStrategy, Set<Locator>> ignores = getIgnores(checkSettings);

            ScreenshotParameters screenshotParameters = screenshotParametersFactory.create(screenshotConfiguration,
                    "ignores table", ignores);
            vc.setScreenshotParameters(Optional.of(screenshotParameters));
            return vc;
        }, vc -> execute(vc, getCheckResultProvider()));
    }

    @Override
    protected String getTemplateName()
    {
        return "visual-comparison.ftl";
    }

    private void performVisualAction(String baselineName, VisualActionType actionType,
            Optional<String> baselineStorage, Parameters checkSettings, UnaryOperator<VisualCheck> configurer,
            Consumer<Supplier<VisualCheck>> visualCheck)
    {
        Supplier<VisualCheck> visualCheckFactory = compose(configurer, initVisualCheck(baselineName,
                actionType, baselineStorage, checkSettings));
        visualCheck.accept(visualCheckFactory);
    }

    private Supplier<VisualCheck> initVisualCheck(String baselineName, VisualActionType actionType,
            Optional<String> baselineStorage, Parameters checkSettings)
    {
        return () -> {
            String indexedBaselineName = baselineIndexer.createIndexedBaseline(baselineName);
            VisualCheck visualCheck = new VisualCheck(indexedBaselineName, actionType);
            visualCheck.setBaselineStorage(baselineStorage);
            if (visualCheck.getAction() == VisualActionType.CHECK_INEQUALITY_AGAINST)
            {
                visualCheck.setRequiredDiffPercentage(
                        checkSettings.valueAs("REQUIRED_DIFF_PERCENTAGE", OptionalDouble.class,
                                OptionalDouble.empty()));
            }
            else
            {
                visualCheck.setAcceptableDiffPercentage(
                        checkSettings.valueAs("ACCEPTABLE_DIFF_PERCENTAGE", OptionalDouble.class,
                                OptionalDouble.empty()));
            }
            return visualCheck;
        };
    }

    private <T, R> Supplier<R> compose(Function<T, R> modifier, Supplier<T> initializer)
    {
        return () -> modifier.apply(initializer.get());
    }

    private Function<VisualCheck, VisualCheckResult> getCheckResultProvider()
    {
        return check -> {
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
    }

    private Map<IgnoreStrategy, Set<Locator>> getIgnores(Parameters checkParameters)
    {
        return Stream.of(IgnoreStrategy.values()).collect(Collectors.toMap(Function.identity(),
                s -> checkParameters.valueAs(s.name(), SET_BY, Set.of())));
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
