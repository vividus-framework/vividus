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

package org.vividus.azure.devops.exporter;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
        for (Story story : OutputReader.readStoriesFromJsons(options.getJsonResultsDirectory()))
        {
            LOGGER.atInfo().addArgument(story::getPath).log("Exporting scenarios from {} story");

            story.getFoldedScenarios().forEach(scenario -> exportScenario(story.getPath(), scenario));
        }
    }

    private void exportScenario(String storyPath, Scenario scenario)
    {
        if (scenario.hasMetaWithName("azure-devops.skip-export"))
        {
            LOGGER.atInfo().addArgument(scenario::getTitle).log("Skip export of {} scenario");
            return;
        }

        LOGGER.atInfo().addArgument(scenario::getTitle).log("Exporting {} scenario");

        try
        {
            Optional<String> testCaseId = scenario.getUniqueMetaValue("testCaseId");

            if (testCaseId.isPresent())
            {
                facade.updateTestCase(testCaseId.get(), storyPath, scenario);
            }
            else
            {
                facade.createTestCase(storyPath, scenario);
            }
        }
        catch (IOException | NotUniqueMetaValueException | SyntaxException e)
        {
            LOGGER.atError().setCause(e).log("Got an error while exporting");
        }
    }
}
