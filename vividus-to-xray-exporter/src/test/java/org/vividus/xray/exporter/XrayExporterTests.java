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

package org.vividus.xray.exporter;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.ResourceUtils;
import org.vividus.xray.configuration.XrayExporterOptions;
import org.vividus.xray.exception.SyntaxException;
import org.vividus.xray.facade.AbstractTestCaseParameters;
import org.vividus.xray.facade.CucumberTestCaseParameters;
import org.vividus.xray.facade.ManualTestCaseParameters;
import org.vividus.xray.facade.XrayFacade;
import org.vividus.xray.facade.XrayFacade.NonEditableIssueStatusException;
import org.vividus.xray.factory.TestCaseFactory;
import org.vividus.xray.model.CucumberTestCase;
import org.vividus.xray.model.ManualTestCase;
import org.vividus.xray.model.ManualTestStep;
import org.vividus.xray.model.TestCaseType;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class XrayExporterTests
{
    private static final String ISSUE_ID = "STUB-0";
    private static final String SCENARIO_TITLE = "Dummy scenario";
    private static final String STORY_TITLE = "storyPath";
    private static final String ERROR_MESSAGE = "Got an error while exporting";
    private static final String GIVEN_STEP = "Given I setup test environment";
    private static final String WHEN_STEP = "When I perform action on test environment";
    private static final String THEN_STEP = "Then I verify changes on test environment";

    @Captor private ArgumentCaptor<ManualTestCaseParameters> manualTestCaseParametersCaptor;
    @Captor private ArgumentCaptor<CucumberTestCaseParameters> cucumberTestCaseParametersCaptor;

    @Spy private XrayExporterOptions xrayExporterOptions;
    @Mock private TestCaseFactory testCaseFactory;
    @Mock private XrayFacade xrayFacade;
    @InjectMocks private XrayExporter xrayExporter;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(XrayExporter.class);

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(xrayFacade, testCaseFactory);
    }

    @Test
    void shouldExportCucumberTestCaseWithoutTestCaseId() throws URISyntaxException, IOException
    {
        URI jsonResultsUri = getJsonResultsUri("createcucumber");
        xrayExporterOptions.setJsonResultsDirectory(Paths.get(jsonResultsUri));
        CucumberTestCase testCase = mock(CucumberTestCase.class);

        when(xrayFacade.createTestCase(testCase)).thenReturn(ISSUE_ID);
        when(testCaseFactory.createCucumberTestCase(cucumberTestCaseParametersCaptor.capture())).thenReturn(testCase);

        xrayExporter.exportResults();

        String scenario = GIVEN_STEP + lineSeparator()
            + WHEN_STEP + lineSeparator()
            + THEN_STEP + lineSeparator()
            + "Examples:" + lineSeparator()
            + "|parameter-key|" + lineSeparator()
            + "|parameter-value-1|" + lineSeparator()
            + "|parameter-value-2|" + lineSeparator()
            + "|parameter-value-3|" + lineSeparator();
        verifyCucumberTestCaseParameters("Scenario Outline", scenario);
        validateLogs(jsonResultsUri, getExportingScenarioEvent(), getExportSuccessfulEvent());
    }

    @Test
    void shouldUpdateExistingCucumberTestCase() throws URISyntaxException, IOException, NonEditableIssueStatusException
    {
        URI jsonResultsUri = getJsonResultsUri("updatecucumber");
        xrayExporterOptions.setJsonResultsDirectory(Paths.get(jsonResultsUri));
        CucumberTestCase testCase = mock(CucumberTestCase.class);

        when(testCaseFactory.createCucumberTestCase(cucumberTestCaseParametersCaptor.capture())).thenReturn(testCase);

        xrayExporter.exportResults();

        verify(xrayFacade).updateTestCase(ISSUE_ID, testCase);
        String scenario = GIVEN_STEP + lineSeparator() + WHEN_STEP + lineSeparator() + THEN_STEP;
        verifyCucumberTestCaseParameters("Scenario", scenario);
        validateLogs(jsonResultsUri, getExportingScenarioEvent(), getExportSuccessfulEvent());
    }

    @Test
    void shouldExportTestWithLabelsAndComponentsAndUpdatableTestCaseId()
            throws URISyntaxException, IOException, NonEditableIssueStatusException
    {
        URI jsonResultsUri = getJsonResultsUri("componentslabelsupdatabletci");
        xrayExporterOptions.setJsonResultsDirectory(Paths.get(jsonResultsUri));
        String testExecutionKey = "TEST-0";
        xrayExporterOptions.setTestExecutionKey(testExecutionKey);
        ManualTestCase testCase = mock(ManualTestCase.class);

        when(testCaseFactory.createManualTestCase(manualTestCaseParametersCaptor.capture())).thenReturn(testCase);

        xrayExporter.exportResults();

        verify(xrayFacade).updateTestCase(ISSUE_ID, testCase);
        verifyManualTestCaseParameters(Set.of("dummy-label-1", "dummy-label-2"),
                Set.of("dummy-component-1", "dummy-component-2"));
        verify(xrayFacade).updateTestExecution(testExecutionKey, List.of(ISSUE_ID));
        validateLogs(jsonResultsUri, getExportingScenarioEvent(), getExportSuccessfulEvent());
    }

    @Test
    void shouldExportTestCaseIfPreviousTestCaseExportAttemptThrownIOException()
            throws URISyntaxException, IOException, NonEditableIssueStatusException
    {
        URI jsonResultsUri = getJsonResultsUri("continueiferror");
        xrayExporterOptions.setJsonResultsDirectory(Paths.get(jsonResultsUri));
        IOException exception = mock(IOException.class);
        String errorIssueId = "STUB-ERROR";

        String errorMessage = "error message";
        when(exception.getMessage()).thenReturn(errorMessage);
        doThrow(exception).when(xrayFacade).updateTestCase(eq(errorIssueId), any(ManualTestCase.class));
        ManualTestCase testCase = mock(ManualTestCase.class);
        when(testCaseFactory.createManualTestCase(manualTestCaseParametersCaptor.capture())).thenReturn(testCase);

        xrayExporter.exportResults();

        verify(xrayFacade).updateTestCase(ISSUE_ID, testCase);
        verifyManualTestCaseParameters(Set.of(), Set.of());
        validateLogs(jsonResultsUri, getExportingScenarioEvent(), error(exception, ERROR_MESSAGE),
                getExportingScenarioEvent(), getReportErrorEvent(errorMessage));
    }

    @Test
    void shouldNotExportSkippedTest() throws URISyntaxException, IOException
    {
        URI jsonResultsUri = getJsonResultsUri("skipped");
        xrayExporterOptions.setJsonResultsDirectory(Paths.get(jsonResultsUri));

        xrayExporter.exportResults();

        validateLogs(jsonResultsUri, info("Skip export of {} scenario", SCENARIO_TITLE), getExportSuccessfulEvent());
    }

    @Test
    void shouldFailIfResultsDirectoryIsEmpty(@TempDir Path sourceDirectory) throws IOException
    {
        xrayExporterOptions.setJsonResultsDirectory(sourceDirectory);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, xrayExporter::exportResults);

        assertEquals(String.format("The directory '%s' does not contain needed JSON files", sourceDirectory),
                exception.getMessage());
        assertThat(logger.getLoggingEvents(), empty());
    }

    @Test
    void shouldExportNewTestAndLinkToRequirements() throws URISyntaxException, IOException
    {
        URI jsonResultsUri = getJsonResultsUri("createandlink");
        xrayExporterOptions.setJsonResultsDirectory(Paths.get(jsonResultsUri));
        ManualTestCase testCase = mock(ManualTestCase.class);

        when(xrayFacade.createTestCase(testCase)).thenReturn(ISSUE_ID);
        when(testCaseFactory.createManualTestCase(manualTestCaseParametersCaptor.capture())).thenReturn(testCase);

        xrayExporter.exportResults();

        verify(xrayFacade).createTestsLink(ISSUE_ID, "STUB-REQ-0");

        verifyManualTestCaseParameters(Set.of(), Set.of());
        validateLogs(jsonResultsUri, getExportingScenarioEvent(), getExportSuccessfulEvent());
    }

    @Test
    void shouldFailIfMoreThanOneIdIsSpecified() throws URISyntaxException, IOException
    {
        URI jsonResultsUri = getJsonResultsUri("morethanoneid");
        xrayExporterOptions.setJsonResultsDirectory(Paths.get(jsonResultsUri));

        xrayExporter.exportResults();

        List<LoggingEvent> loggingEvents = new ArrayList<>(logger.getLoggingEvents());
        LoggingEvent errorEvent = loggingEvents.remove(4);
        assertEquals(ERROR_MESSAGE, errorEvent.getMessage());
        Optional<Throwable> eventThorable = errorEvent.getThrowable();
        assertTrue(eventThorable.isPresent());
        Throwable throwable = eventThorable.get();
        assertThat(throwable, instanceOf(SyntaxException.class));
        String errorMessage = "Only one 'testCaseId' can be specified for a test case, but got: STUB-0, STUB-1, STUB-2";
        assertEquals(errorMessage, throwable.getMessage());
        validateLogs(loggingEvents, jsonResultsUri, getExportingScenarioEvent(), getReportErrorEvent(errorMessage));
    }

    private void verifyCucumberTestCaseParameters(String scenarioType, String scenario)
    {
        CucumberTestCaseParameters parameters = cucumberTestCaseParametersCaptor.getValue();
        assertEquals(scenarioType, parameters.getScenarioType());
        assertEquals(scenario, parameters.getScenario());
        verifyTestCaseParameters(parameters, Set.of(), Set.of(), TestCaseType.CUCUMBER);
    }

    private void verifyManualTestCaseParameters(Set<String> labels, Set<String> components)
    {
        ManualTestCaseParameters parameters = manualTestCaseParametersCaptor.getValue();
        assertThat(parameters.getSteps(), hasSize(1));
        ManualTestStep step = parameters.getSteps().get(0);
        assertEquals("Step", step.getAction());
        assertEquals("Data", step.getData());
        assertEquals("Result", step.getExpectedResult());
        verifyTestCaseParameters(parameters, labels, components, TestCaseType.MANUAL);
    }

    private void verifyTestCaseParameters(AbstractTestCaseParameters testCase, Set<String> labels,
            Set<String> components, TestCaseType type)
    {
        assertEquals(type, testCase.getType());
        assertEquals(SCENARIO_TITLE, testCase.getSummary());
        assertEquals(labels, testCase.getLabels());
        assertEquals(components, testCase.getComponents());
    }

    private void validateLogs(URI jsonResultsUri, LoggingEvent... additionalEvents)
    {
        validateLogs(logger.getLoggingEvents(), jsonResultsUri, additionalEvents);
    }

    private void validateLogs(List<LoggingEvent> loggingEvents, URI jsonResultsUri, LoggingEvent... additionalEvents)
    {
        String absolutePath = Paths.get(jsonResultsUri).toFile().getAbsolutePath();
        String filePath = Paths.get(absolutePath, "test-case-execution-report.json").toString();
        List<LoggingEvent> events = new ArrayList<>();
        events.add(info("JSON files: {}", filePath));
        events.add(info("Parsing {}", filePath));
        events.add(info("Exporting scenarios from {} story", STORY_TITLE));
        Stream.of(additionalEvents).forEach(events::add);
        assertThat(loggingEvents, is(events));
    }

    private static LoggingEvent getReportErrorEvent(String error)
    {
        String errorFormat = "Error #1%1$sStory: storyPath%1$sScenario: Dummy scenario%1$sError: %2$s%1$s";
        return error("Export failed:{}{}", lineSeparator(), String.format(errorFormat, lineSeparator(), error));
    }

    private static LoggingEvent getExportSuccessfulEvent()
    {
        return info("Export successful");
    }

    private static LoggingEvent getExportingScenarioEvent()
    {
        return info("Exporting {} scenario", SCENARIO_TITLE);
    }

    public URI getJsonResultsUri(String resource) throws URISyntaxException
    {
        return ResourceUtils.findResource(getClass(), resource).toURI();
    }
}
