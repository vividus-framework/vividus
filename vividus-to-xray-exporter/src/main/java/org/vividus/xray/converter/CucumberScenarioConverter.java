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

package org.vividus.xray.converter;

import static java.lang.System.lineSeparator;

import java.util.List;
import java.util.stream.Collectors;

import org.vividus.model.jbehave.Parameters;
import org.vividus.model.jbehave.Scenario;
import org.vividus.model.jbehave.Step;

public final class CucumberScenarioConverter
{
    private static final String COMMENT_KEYWORD = "!-- ";
    private static final String CUCUMBER_SEPARATOR = "|";

    private CucumberScenarioConverter()
    {
    }

    public static CucumberScenario convert(Scenario scenario)
    {
        String cucumberScenario = getCucumberScenario(scenario);
        if (scenario.getExamples() == null)
        {
            return new CucumberScenario("Scenario", cucumberScenario);
        }
        String cucumberScenarioOutline = cucumberScenario + lineSeparator()
                + buildScenarioExamplesTable(scenario.getExamples().getParameters());
        return new CucumberScenario("Scenario Outline", cucumberScenarioOutline);
    }

    private static String getCucumberScenario(Scenario scenario)
    {
        return scenario.collectSteps()
                       .stream()
                       .map(Step::getValue)
                       .map(CucumberScenarioConverter::replaceCommentSign)
                       .collect(Collectors.joining(lineSeparator()));
    }

    private static String replaceCommentSign(String step)
    {
        return step.startsWith(COMMENT_KEYWORD) ? step.replace(COMMENT_KEYWORD, "# ") : step;
    }

    private static String buildScenarioExamplesTable(Parameters parameters)
    {
        return new StringBuilder("Examples:").append(lineSeparator())
                                             .append(joinTableRow(parameters.getNames()))
                                             .append(parameters.getValues().stream()
                                                                           .map(CucumberScenarioConverter::joinTableRow)
                                                                           .collect(Collectors.joining()))
                                             .toString();
    }

    private static String joinTableRow(List<String> values)
    {
        return values.stream().collect(
                Collectors.joining(CUCUMBER_SEPARATOR, CUCUMBER_SEPARATOR, CUCUMBER_SEPARATOR + lineSeparator()));
    }

    public static class CucumberScenario
    {
        private final String type;
        private final String scenario;

        public CucumberScenario(String type, String scenario)
        {
            this.type = type;
            this.scenario = scenario;
        }

        public String getType()
        {
            return type;
        }

        public String getScenario()
        {
            return scenario;
        }
    }
}
