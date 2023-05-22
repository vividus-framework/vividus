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

package org.vividus.visual.eyes.converter;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.applitools.eyes.AccessibilitySettings;
import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.config.Configuration;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.openqa.selenium.Dimension;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParametersFactory;
import org.vividus.visual.eyes.factory.ApplitoolsVisualCheckFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.screenshot.BaselineIndexer;

public class ExamplesTableToApplitoolsVisualChecksConverter extends
        AbstractParameterConverter<ExamplesTable, List<ApplitoolsVisualCheck>> implements ApplitoolsVisualCheckFactory
{
    private static final String BASELINE_NAME_OPTION = "baselineName";
    private static final String ACTION_OPTION = "action";
    private static final String EXECUTE_API_KEY_OPTION = "executeApiKey";
    private static final String READ_API_KEY_OPTION = "readApiKey";
    private static final String HOST_APP_OPTION = "hostApp";
    private static final String HOST_OS_OPTION = "hostOS";
    private static final String VIEWPORT_SIZE_OPTION = "viewportSize";
    private static final String MATCH_LEVEL_OPTION = "matchLevel";
    private static final String SERVER_URI_OPTION = "serverUri";
    private static final String APP_NAME_OPTION = "appName";
    private static final String BATCH_NAME_OPTION = "batchName";
    private static final String BASELINE_ENV_NAME_OPTION = "baselineEnvName";
    private static final String ELEMENTS_TO_IGNORE_OPTION = "elementsToIgnore";
    private static final String AREAS_TO_IGNORE_OPTION = "areasToIgnore";
    private static final String ACCESSIBILITY_STANDARD_OPTION = "accessibilityStandard";

    private static final List<String> SUPPORTED_OPTIONS = List.of(BASELINE_NAME_OPTION, ACTION_OPTION,
            EXECUTE_API_KEY_OPTION, READ_API_KEY_OPTION, HOST_APP_OPTION, HOST_OS_OPTION, VIEWPORT_SIZE_OPTION,
            MATCH_LEVEL_OPTION, SERVER_URI_OPTION, APP_NAME_OPTION, BATCH_NAME_OPTION, BASELINE_ENV_NAME_OPTION,
            ELEMENTS_TO_IGNORE_OPTION, AREAS_TO_IGNORE_OPTION, ACCESSIBILITY_STANDARD_OPTION);

    private String executeApiKey;
    private String readApiKey;
    private String hostApp;
    private String hostOS;
    private Dimension viewportSize;
    private MatchLevel matchLevel;
    private URI serverUri;
    private String appName = "Application";
    private String baselineEnvName;

    private final Map<String, BatchInfo> batchStorage = new ConcurrentHashMap<>();

    private final ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory;
    private final BaselineIndexer baselineIndexer;

    public ExamplesTableToApplitoolsVisualChecksConverter(
            ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory,
            BaselineIndexer baselineIndexer)
    {
        this.screenshotParametersFactory = screenshotParametersFactory;
        this.baselineIndexer = baselineIndexer;
    }

    @Override
    public List<ApplitoolsVisualCheck> convertValue(ExamplesTable table, Type type)
    {
        List<String> unsupportedOptions = table.getHeaders().stream()
                .filter(option -> !SUPPORTED_OPTIONS.contains(option))
                .collect(Collectors.toList());
        Validate.isTrue(unsupportedOptions.isEmpty(), "Unknown Applitools configuration options: %s",
                String.join(", ", unsupportedOptions));

        return table.getRowsAsParameters(true).stream().map(params ->
        {
            String batchName = params.valueAs(BATCH_NAME_OPTION, String.class);
            String baselineName = params.valueAs(BASELINE_NAME_OPTION, String.class);
            VisualActionType action = params.valueAs(ACTION_OPTION, VisualActionType.class);

            ApplitoolsVisualCheck check = createCheck(batchName, baselineName, action);
            Type targetType = new TypeLiteral<Set<Locator>>() { }.value;

            configureEyes(
                check,
                params.valueAs(EXECUTE_API_KEY_OPTION, String.class, executeApiKey),
                params.valueAs(HOST_APP_OPTION, String.class, hostApp),
                params.valueAs(HOST_OS_OPTION, String.class, hostOS),
                params.valueAs(VIEWPORT_SIZE_OPTION, Dimension.class, viewportSize),
                params.valueAs(MATCH_LEVEL_OPTION, MatchLevel.class, matchLevel),
                params.valueAs(SERVER_URI_OPTION, URI.class, serverUri),
                params.valueAs(APP_NAME_OPTION, String.class, appName),
                params.valueAs(BASELINE_ENV_NAME_OPTION, String.class, baselineEnvName),
                params.valueAs(READ_API_KEY_OPTION, String.class, readApiKey),
                params.valueAs(ELEMENTS_TO_IGNORE_OPTION, targetType, Set.of()),
                params.valueAs(AREAS_TO_IGNORE_OPTION, targetType, Set.of()),
                params.valueAs(ACCESSIBILITY_STANDARD_OPTION, AccessibilitySettings.class, null)
            );

            return check;
        }).collect(Collectors.toList());
    }

    @Override
    public ApplitoolsVisualCheck create(String batchName, String baselineName, VisualActionType action)
    {
        ApplitoolsVisualCheck check = createCheck(batchName, baselineName, action);
        configureEyes(check, executeApiKey, hostApp, hostOS, viewportSize, matchLevel, serverUri, appName,
                baselineEnvName, readApiKey, Set.of(), Set.of(), null);
        return check;
    }

    private ApplitoolsVisualCheck createCheck(String batchName, String baselineName, VisualActionType action)
    {
        return new ApplitoolsVisualCheck(batchName, baselineIndexer.createIndexedBaseline(baselineName), action);
    }

    @SuppressWarnings("paramNum")
    private void configureEyes(ApplitoolsVisualCheck check, String executeApiKey, String hostApp, String hostOS,
            Dimension viewportSize, MatchLevel matchLevel, URI serverUri, String appName, String baselineEnvName,
            String readApiKey, Set<Locator> elements, Set<Locator> areas, AccessibilitySettings settings)
    {
        check.setScreenshotParameters(screenshotParametersFactory.create());
        check.setReadApiKey(readApiKey);
        check.setElementsToIgnore(elements);
        check.setAreasToIgnore(areas);

        RectangleSize viewport = Optional.ofNullable(viewportSize)
                .map(d -> new RectangleSize(d.getWidth(), d.getHeight()))
                .orElse(null);

        Configuration configuration = new Configuration()
                .setApiKey(executeApiKey)
                .setHostApp(hostApp)
                .setHostOS(hostOS)
                .setViewportSize(viewport)
                .setMatchLevel(matchLevel)
                .setServerUrl(Optional.ofNullable(serverUri).map(URI::toString).orElse(null))
                .setAppName(appName)
                .setBaselineEnvName(baselineEnvName)
                .setAccessibilityValidation(settings);

        boolean saveTests = check.getAction() == VisualActionType.ESTABLISH;
        configuration.setSaveFailedTests(saveTests);
        configuration.setSaveNewTests(saveTests);
        String batchName = check.getBatchName();
        configuration.setBatch(batchStorage.computeIfAbsent(batchName, BatchInfo::new));

        check.setConfiguration(configuration);
    }

    public void setExecuteApiKey(String executeApiKey)
    {
        this.executeApiKey = executeApiKey;
    }

    public void setReadApiKey(String readApiKey)
    {
        this.readApiKey = readApiKey;
    }

    public void setHostApp(String hostApp)
    {
        this.hostApp = hostApp;
    }

    public void setHostOS(String hostOS)
    {
        this.hostOS = hostOS;
    }

    public void setViewportSize(Dimension viewportSize)
    {
        this.viewportSize = viewportSize;
    }

    public void setMatchLevel(MatchLevel matchLevel)
    {
        this.matchLevel = matchLevel;
    }

    public void setServerUri(URI serverUri)
    {
        this.serverUri = serverUri;
    }

    public void setAppName(String appName)
    {
        this.appName = appName;
    }

    public void setBaselineEnvName(String baselineEnvName)
    {
        this.baselineEnvName = baselineEnvName;
    }
}
