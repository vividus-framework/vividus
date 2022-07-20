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

package org.vividus.azure.devops.exporter;

import static java.util.Map.entry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.vividus.azure.devops.client.model.WorkItem;
import org.vividus.azure.devops.configuration.AzureDevOpsExporterOptions;
import org.vividus.azure.devops.facade.AzureDevOpsFacade;
import org.vividus.model.jbehave.NotUniqueMetaValueException;
import org.vividus.model.jbehave.Scenario;
import org.vividus.model.jbehave.Story;
import org.vividus.output.OutputReader;
import org.vividus.output.SyntaxException;

@Component
public class AzureDevOpsExporter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureDevOpsExporter.class);

    private final AzureDevOpsExporterOptions options;
    private final AzureDevOpsFacade facade;

    public AzureDevOpsExporter(AzureDevOpsExporterOptions options, AzureDevOpsFacade facade)
    {
        this.options = options;
        this.facade = facade;
    }

    public void exportResults() throws IOException
    {
        Map<Integer, Scenario> exportedScenarios = new HashMap<>();

        for (Story story : OutputReader.readStoriesFromJsons(options.getJsonResultsDirectory()))
        {
            LOGGER.atInfo().addArgument(story::getPath).log("Exporting scenarios from {} story");

            story.getFoldedScenarios().stream()
                                      .filter(scenario ->
                                      {
                                          boolean skipped = scenario.hasMetaWithName("azure-devops.skip-export");
                                          if (skipped)
                                          {
                                              LOGGER.atInfo().addArgument(scenario::getTitle)
                                                             .log("Skip export of {} scenario");
                                          }
                                          return !skipped;
                                      })
                                      .map(scenario -> exportScenario(story.getPath(), scenario))
                                      .flatMap(Optional::stream)
                                      .forEach(entry -> exportedScenarios.put(entry.getKey(), entry.getValue()));
        }

        if (exportedScenarios.isEmpty())
        {
            LOGGER.atInfo().log("No scenarios were exported");
            return;
        }

        if (options.isCreateTestRun())
        {
            facade.createTestRun(exportedScenarios);
        }
    }

    private Optional<Entry<Integer, Scenario>> exportScenario(String storyPath, Scenario scenario)
    {
        try
        {
            LOGGER.atInfo().addArgument(scenario::getTitle).log("Exporting {} scenario");

            Optional<Integer> testCaseId = scenario.getUniqueMetaValue("testCaseId").map(Integer::valueOf);

            if (testCaseId.isPresent())
            {
                facade.updateTestCase(testCaseId.get(), storyPath, scenario);
            }
            else
            {
                testCaseId = Optional.of(facade.createTestCase(storyPath, scenario)).map(WorkItem::getId);
            }

            return testCaseId.map(id -> entry(id, scenario));
        }
        catch (IOException | NotUniqueMetaValueException | SyntaxException e)
        {
            LOGGER.atError().setCause(e).log("Got an error while exporting");
            return Optional.empty();
        }
    }
}
