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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.applitools.eyes.AccessibilityGuidelinesVersion;
import com.applitools.eyes.AccessibilityLevel;
import com.applitools.eyes.AccessibilitySettings;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.config.Configuration;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.FunctionalParameterConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.vividus.converter.FluentTrimmedEnumConverter;
import org.vividus.converter.ui.web.StringToDimensionParameterConverter;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.ui.screenshot.ScreenshotParametersFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.screenshot.BaselineIndexer;

@ExtendWith(MockitoExtension.class)
class ExamplesTableToApplitoolsVisualChecksConverterTests
{
    private static final String BASELINE = "baseline-name";
    private static final String BATCH = "batch-name";
    private static final RectangleSize DIMENSION = new RectangleSize(1, 1);

    @Mock private ScreenshotParameters screenshotParameters;
    @Mock private Locator locator;
    @Mock private ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory;
    @Mock private BaselineIndexer baselineIndexer;
    @InjectMocks private ExamplesTableToApplitoolsVisualChecksConverter converter;

    @Test
    void shouldConvertExamplesTableIntoApplitoolsVisualChecksAllParameters()
    {
        when(screenshotParametersFactory.create()).thenReturn(Optional.of(screenshotParameters));
        when(baselineIndexer.createIndexedBaseline(BASELINE)).thenReturn(BASELINE);

         // CHECKSTYLE:OFF
        String table = "|executeApiKey  |readApiKey  |hostApp |hostOS |viewportSize|matchLevel|serverUri          |appName |batchName |baselineEnvName  |elementsToIgnore|areasToIgnore|baselineName |action   |accessibilityStandard|" + System.lineSeparator()
                     + "|execute-api-key|read-api-key|host-app|host-os|1x1         |EXACT     |https://example.com|app-name|batch-name|baseline-env-name|elements        |areas        |baseline-name|ESTABLISH|WCAG 2.1 - AA        |";
        // CHECKSTYLE:ON
        ExamplesTable applitoolsCheckTable = createTable(table);

        List<ApplitoolsVisualCheck> checks = converter.convertValue(applitoolsCheckTable, null);
        assertThat(checks, hasSize(1));
        ApplitoolsVisualCheck check = checks.get(0);
        Configuration configuration = check.getConfiguration();
        AccessibilitySettings settings = configuration.getAccessibilityValidation();
        assertAll(
            () -> assertEquals("execute-api-key", configuration.getApiKey()),
            () -> assertEquals("read-api-key", check.getReadApiKey()),
            () -> assertEquals("host-app", configuration.getHostApp()),
            () -> assertEquals("host-os", configuration.getHostOS()),
            () -> assertEquals(DIMENSION, configuration.getViewportSize()),
            () -> assertEquals(MatchLevel.EXACT, configuration.getMatchLevel()),
            () -> assertEquals(URI.create("https://example.com"), configuration.getServerUrl()),
            () -> assertEquals("app-name", configuration.getAppName()),
            () -> assertEquals(BATCH, check.getBatchName()),
            () -> assertEquals("baseline-env-name", configuration.getBaselineEnvName()),
            () -> assertEquals(Set.of(locator), check.getElementsToIgnore()),
            () -> assertEquals(Set.of(locator), check.getAreasToIgnore()),
            () -> assertEquals(BASELINE, check.getBaselineName()),
            () -> assertEquals(VisualActionType.ESTABLISH, check.getAction()),
            () -> assertEquals(Optional.of(screenshotParameters), check.getScreenshotParameters()),
            () -> assertTrue(configuration.getSaveFailedTests()),
            () -> assertTrue(configuration.getSaveNewTests()),
            () -> assertEquals(BATCH, configuration.getBatch().getName()),
            () -> assertEquals(AccessibilityLevel.AA, settings.getLevel()),
            () -> assertEquals(AccessibilityGuidelinesVersion.WCAG_2_1, settings.getGuidelinesVersion())
        );
    }

    @Test
    void shouldCreateApplitoolsVisualChecksWithDefaultParameters()
    {
        testDefaultParameters(() -> List.of(converter.create(BATCH, BASELINE, VisualActionType.COMPARE_AGAINST)));
    }

    @Test
    void shouldConvertExamplesTableIntoApplitoolsVisualChecksWithDefaultParameters()
    {
        String table = "|batchName |baselineName |action         |" + System.lineSeparator()
                     + "|batch-name|baseline-name|COMPARE_AGAINST|";
        ExamplesTable applitoolsCheckTable = createTable(table);

        testDefaultParameters(() -> converter.convertValue(applitoolsCheckTable, null));
    }

    private void testDefaultParameters(Supplier<List<ApplitoolsVisualCheck>> checksSupplier)
    {
        when(screenshotParametersFactory.create()).thenReturn(Optional.of(screenshotParameters));
        when(baselineIndexer.createIndexedBaseline(BASELINE)).thenReturn(BASELINE);

        String executeApiKey = "default-execute-api-key";
        converter.setExecuteApiKey(executeApiKey);
        String readApiKey = "default-read-api-key";
        converter.setReadApiKey(readApiKey);
        String hostApp = "default-host-app";
        converter.setHostApp(hostApp);
        String hostOs = "default-host-os";
        converter.setHostOS(hostOs);
        converter.setViewportSize(new Dimension(0, 0));
        converter.setMatchLevel(MatchLevel.NONE);
        URI uri = URI.create("https://dev.by");
        converter.setServerUri(uri);
        String appName = "default-app-name";
        converter.setAppName(appName);
        String baselineEnvName = "default-baseline-env-name";
        converter.setBaselineEnvName(baselineEnvName);

        List<ApplitoolsVisualCheck> checks = checksSupplier.get();
        assertThat(checks, hasSize(1));
        ApplitoolsVisualCheck check = checks.get(0);
        Configuration configuration = check.getConfiguration();
        assertAll(
            () -> assertEquals(executeApiKey, configuration.getApiKey()),
            () -> assertEquals(readApiKey, check.getReadApiKey()),
            () -> assertEquals(hostApp, configuration.getHostApp()),
            () -> assertEquals(hostOs, configuration.getHostOS()),
            () -> assertEquals(new RectangleSize(0, 0), configuration.getViewportSize()),
            () -> assertEquals(MatchLevel.NONE, configuration.getMatchLevel()),
            () -> assertEquals(uri, configuration.getServerUrl()),
            () -> assertEquals(appName, configuration.getAppName()),
            () -> assertEquals(BATCH, check.getBatchName()),
            () -> assertEquals(baselineEnvName, configuration.getBaselineEnvName()),
            () -> assertEquals(Set.of(), check.getElementsToIgnore()),
            () -> assertEquals(Set.of(), check.getAreasToIgnore()),
            () -> assertEquals(BASELINE, check.getBaselineName()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, check.getAction()),
            () -> assertEquals(Optional.of(screenshotParameters), check.getScreenshotParameters()),
            () -> assertFalse(configuration.getSaveFailedTests()),
            () -> assertFalse(configuration.getSaveNewTests()),
            () -> assertEquals(BATCH, configuration.getBatch().getName()),
            () -> assertNull(configuration.getAccessibilityValidation())
        );
    }

    @Test
    void shouldFailOnUnknownOptions()
    {
        ExamplesTable applitoolsCheckTable = createTable(
                "|batchName |param1|param2|" + System.lineSeparator() + "|batch-name|val1  |val2  |");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> converter.convertValue(applitoolsCheckTable, null));
        assertEquals("Unknown Applitools configuration options: param1, param2", thrown.getMessage());
    }

    private ExamplesTable createTable(String table)
    {
        ParameterConverters parameterConverters = new ParameterConverters();
        parameterConverters.addConverters(
            new FunctionalParameterConverter<String, Set<Locator>>(value -> Set.of(locator)) { },
            new FunctionalParameterConverter<String, Dimension>(StringToDimensionParameterConverter::convert) { },
            new FunctionalParameterConverter<String, URI>(URI::create) { },
            new StringToAccessibilitySettingsConverter(new FluentTrimmedEnumConverter())
        );
        return new ExamplesTableFactory(new Keywords(), null, parameterConverters, new ParameterControls(),
                new TableParsers(parameterConverters), new TableTransformers()).createExamplesTable(table);
    }
}
