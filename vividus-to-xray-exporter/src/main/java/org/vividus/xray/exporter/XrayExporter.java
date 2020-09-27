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

import static java.lang.System.lineSeparator;
import static org.apache.commons.lang3.Validate.notEmpty;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vividus.bdd.model.jbehave.Meta;
import org.vividus.bdd.model.jbehave.Scenario;
import org.vividus.bdd.model.jbehave.Story;
import org.vividus.xray.configuration.XrayExporterOptions;
import org.vividus.xray.converter.ManualStepConverter;
import org.vividus.xray.exception.SyntaxException;
import org.vividus.xray.facade.TestCaseParameters;
import org.vividus.xray.facade.XrayFacade;
import org.vividus.xray.facade.XrayFacade.NonEditableIssueStatusException;
import org.vividus.xray.reader.JsonResourceReader;
import org.vividus.xray.reader.JsonResourceReader.FileEntry;

@Component
public class XrayExporter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(XrayExporter.class);

    private static final String SEMICOLON = ";";

    @Autowired private XrayExporterOptions xrayExporterOptions;
    @Autowired private XrayFacade xrayFacade;

    private final List<ErrorExportEntry> errors = new ArrayList<>();

    public void exportResults() throws IOException
    {
        List<String> testCaseIds = new ArrayList<>();
        for (Story story : readStories())
        {
            LOGGER.atInfo().addArgument(story::getPath).log("Exporting scenarios from {} story");

            for (Scenario scenario : story.getScenarios())
            {
                exportScenario(story.getPath(), scenario).ifPresent(testCaseIds::add);
            }
        }

        addTestCasesToTestExecution(testCaseIds);
        publishErrors();
    }

    private Optional<String> exportScenario(String storyTitle, Scenario scenario) throws IOException
    {
        String scenarioTitle = scenario.getTitle();

        List<Meta> scenarioMeta = scenario.getMeta();
        if (isSkipped(scenarioMeta))
        {
            LOGGER.atInfo().addArgument(scenarioTitle).log("Skip export of {} scenario");
            return Optional.empty();
        }
        LOGGER.atInfo().addArgument(scenarioTitle).log("Exporting {} scenario");

        try
        {
            TestCaseParameters parameters = createTestCaseParameters(storyTitle, scenario);

            String testCaseId = ensureOneValueOrNull(scenarioMeta, "testCaseId");
            if (testCaseId != null)
            {
                xrayFacade.updateTestCase(testCaseId, parameters);
            }
            else
            {
                testCaseId = xrayFacade.createTestCase(parameters);
            }
            createTestsLink(testCaseId, scenarioMeta);
            return Optional.of(testCaseId);
        }
        catch (IOException | SyntaxException | NonEditableIssueStatusException e)
        {
            errors.add(new ErrorExportEntry(storyTitle, scenarioTitle, e.getMessage()));
            LOGGER.atError().setCause(e).log("Got an error while exporting");
        }
        return Optional.empty();
    }

    private TestCaseParameters createTestCaseParameters(String storyTitle, Scenario scenario) throws SyntaxException
    {
        String scenarioTitle = scenario.getTitle();
        List<Meta> scenarioMeta = scenario.getMeta();

        TestCaseParameters parameters = new TestCaseParameters();
        parameters.setLabels(getMetaValues(scenarioMeta, "xray.labels"));
        parameters.setComponents(getMetaValues(scenarioMeta, "xray.components"));
        parameters.setSummary(scenarioTitle);
        parameters.setSteps(ManualStepConverter.convert(storyTitle, scenarioTitle, scenario.collectSteps()));
        return parameters;
    }

    private void addTestCasesToTestExecution(List<String> testCaseIds) throws IOException
    {
        String testExecutionKey = xrayExporterOptions.getTestExecutionKey();
        if (testExecutionKey != null)
        {
            xrayFacade.updateTestExecution(testExecutionKey, testCaseIds);
        }
    }

    private void publishErrors()
    {
        if (!errors.isEmpty())
        {
            LOGGER.atError().addArgument(System::lineSeparator).addArgument(() ->
            {
                StringBuilder errorBuilder = new StringBuilder();
                IntStream.range(0, errors.size()).forEach(index ->
                {
                    ErrorExportEntry errorEntry = errors.get(index);
                    errorBuilder.append("Error #").append(index + 1).append(lineSeparator())
                                .append("Story: ").append(errorEntry.getStory()).append(lineSeparator())
                                .append("Scenario: ").append(errorEntry.getScenario()).append(lineSeparator())
                                .append("Error: ").append(errorEntry.getError()).append(lineSeparator());
                });
                return errorBuilder.toString();
            }).log("Export failed:{}{}");
            return;
        }
        LOGGER.atInfo().log("Export successful");
    }

    private void createTestsLink(String testCaseId, List<Meta> scenarioMeta) throws IOException, SyntaxException
    {
        String requirementId = ensureOneValueOrNull(scenarioMeta, "requirementId");
        if (requirementId != null)
        {
            xrayFacade.createTestsLink(testCaseId, requirementId);
        }
    }

    private List<Story> readStories() throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);

        List<Story> stories = new ArrayList<>();
        for (FileEntry jsonResult : getJsonResultsFiles())
        {
            LOGGER.atInfo().addArgument(jsonResult::getPath).log("Parsing {}");
            stories.add(objectMapper.readValue(jsonResult.getContent(), Story.class));
        }
        return stories;
    }

    private List<FileEntry> getJsonResultsFiles() throws IOException
    {
        Path jsonResiltsDirectory = xrayExporterOptions.getJsonResultsDirectory();
        List<FileEntry> jsonFiles = JsonResourceReader.readFrom(jsonResiltsDirectory);

        notEmpty(jsonFiles, "The directory '%s' does not contain needed JSON files", jsonResiltsDirectory);
        String jsonFilePaths = jsonFiles.stream().map(FileEntry::getPath)
                .collect(Collectors.collectingAndThen(Collectors.toList(), XrayExporter::join));
        LOGGER.atInfo().addArgument(jsonFilePaths).log("JSON files: {}");
        return jsonFiles;
    }

    private static Set<String> getMetaValues(List<Meta> scenarioMeta, String metaName)
    {
        return getMetaValuesStream(scenarioMeta, metaName).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String ensureOneValueOrNull(List<Meta> scenarioMeta, String metaName) throws SyntaxException
    {
        List<String> values = getMetaValuesStream(scenarioMeta, metaName).collect(Collectors.toList());
        if (values.size() > 1)
        {
            throw new SyntaxException(String.format("Only one '%s' can be specified for a test case, but got: %s",
                    metaName, join(values)));
        }
        return values.isEmpty() ? null : values.iterator().next();
    }

    private static String join(Iterable<String> iterable)
    {
        return StringUtils.join(iterable, ", ");
    }

    private static Stream<String> getMetaValuesStream(List<Meta> scenarioMeta, String metaName)
    {
        return asStream(scenarioMeta).filter(m -> metaName.equals(m.getName()) && !m.getValue().isEmpty())
                                     .map(Meta::getValue)
                                     .map(String::trim)
                                     .map(value -> StringUtils.splitPreserveAllTokens(value, SEMICOLON))
                                     .flatMap(Stream::of)
                                     .map(String::trim);
    }

    private static boolean isSkipped(List<Meta> scenarioMeta)
    {
        return asStream(scenarioMeta).anyMatch(m -> "xray.skip-export".equals(m.getName()));
    }

    private static Stream<Meta> asStream(List<Meta> scenarioMeta)
    {
        return Optional.ofNullable(scenarioMeta)
                       .map(List::stream)
                       .orElseGet(Stream::empty);
    }

    private static final class ErrorExportEntry
    {
        private final String story;
        private final String scenario;
        private final String error;

        private ErrorExportEntry(String story, String scenario, String error)
        {
            this.story = story;
            this.scenario = scenario;
            this.error = error;
        }

        public String getStory()
        {
            return story;
        }

        public String getScenario()
        {
            return scenario;
        }

        public String getError()
        {
            return error;
        }
    }
}
