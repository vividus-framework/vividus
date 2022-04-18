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

package org.vividus.log;

import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.reporter.environment.EnvironmentConfigurer;
import org.vividus.results.ResultsProvider;
import org.vividus.results.model.ExecutableEntity;
import org.vividus.results.model.Failure;
import org.vividus.results.model.Statistic;
import org.vividus.util.ResourceUtils;

import de.vandermeer.asciitable.AT_Context;
import de.vandermeer.asciitable.AT_Renderer;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

public final class TestInfoLogger
{
    private static final int MAX_CELL_WIDTH = 50;
    private static final int MARGIN = 3;
    private static final String HYPHEN = "-";
    private static final int HEADER_SIZE = 40;
    private static final String CATEGORY_FORMAT = "%s%n %s:%n";
    private static final String NEW_LINE = "%n";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestInfoLogger.class);
    private static final Pattern SECURE_KEY_PATTERN = Pattern
            .compile(".*(password|((access|api|private)-)?(key|secret|token)).*", Pattern.CASE_INSENSITIVE);
    private static final int HORIZONTAL_RULE_LENGTH = 60;
    private static final String HORIZONTAL_RULE = HYPHEN.repeat(HORIZONTAL_RULE_LENGTH);

    private final ResultsProvider resultsProvider;

    public TestInfoLogger(ResultsProvider resultsProvider)
    {
        this.resultsProvider = resultsProvider;
    }

    public static void drawBanner()
    {
        LOGGER.atInfo().addArgument(() -> ResourceUtils.loadResource("banner.vividus")).log("\n{}");
    }

    public void logTestExecutionResults()
    {
        logInfoMessage(() ->
        {
            int maxKeyLength = EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION
                    .values()
                    .stream()
                    .map(Map::keySet)
                    .flatMap(Set::stream)
                    .mapToInt(String::length)
                    .max().orElse(0);
            String propertyFormat = "   %-" + maxKeyLength + "s %s%n";
            try (Formatter message = new Formatter())
            {
                message.format(NEW_LINE);
                EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.forEach((category, properties) -> {
                    message.format(CATEGORY_FORMAT, HORIZONTAL_RULE, category.getCategoryName());
                    properties.forEach((name, value) -> message.format(propertyFormat, name, value));
                });
                logExecutionStatistics(message);
                return message.toString();
            }
        });
    }

    public static void logExecutionPlan(Map<String, List<String>> executionPlan)
    {
        logInfoMessage(() ->
        {
            String storyFormat = "%n     %s";
            String separatorFormat = "%n%s";

            try (Formatter message = new Formatter())
            {
                message.format(separatorFormat, HORIZONTAL_RULE);
                message.format("%n Execution plan (before filtering by meta):");

                executionPlan.forEach((batchKey, stories) ->
                {
                    message.format("%n   %s:", batchKey);

                    if (!stories.isEmpty())
                    {
                        stories.forEach(story -> message.format(storyFormat, story));
                        return;
                    }
                    message.format(storyFormat, "[no stories found]");
                });

                message.format(separatorFormat, HORIZONTAL_RULE);

                return message.toString();
            }
        });
    }

    private static void logInfoMessage(Supplier<String> messageSupplier)
    {
        LOGGER.atInfo().log(messageSupplier);
    }

    private void logExecutionStatistics(Formatter message)
    {
        Map<ExecutableEntity, Statistic> statistics = resultsProvider.getStatistics();
        Statistic story = statistics.get(ExecutableEntity.STORY);
        Statistic scenario = statistics.get(ExecutableEntity.SCENARIO);
        Statistic step = statistics.get(ExecutableEntity.STEP);
        String row = "%n   %-12s %6s %10s %8s";
        message.format("%n Execution statistics:");
        String rowsSeparator = "%n   " + HYPHEN.repeat(HEADER_SIZE);
        message.format(rowsSeparator);
        message.format(row, "", "Story", "Scenario", "Step");
        message.format(rowsSeparator);
        message.format(row, "Passed", story.getPassed(), scenario.getPassed(), step.getPassed());
        message.format(row, "Failed", story.getFailed(), scenario.getFailed(), step.getFailed());
        message.format(row, "Broken", story.getBroken(), scenario.getBroken(), step.getBroken());
        message.format(row, "Known Issue", story.getKnownIssue(), scenario.getKnownIssue(), step.getKnownIssue());
        message.format(row, "Pending", story.getPending(), scenario.getPending(), step.getPending());
        message.format(row, "Skipped", story.getSkipped(), scenario.getSkipped(), step.getSkipped());
        message.format(rowsSeparator);
        message.format(row, "TOTAL", story.getTotal(), scenario.getTotal(), step.getTotal());
        addFailureTable(message);
    }

    private void addFailureTable(Formatter message)
    {
        Optional<List<Failure>> failureMessages = resultsProvider.getFailures();
        if (failureMessages.isEmpty())
        {
            return;
        }
        List<Failure> failures = failureMessages.get();
        if (failures.isEmpty())
        {
            message.format("%n   No Failures & Errors!");
            return;
        }
        Collections.sort(failures, Comparator.comparing(Failure::getStory)
                                             .thenComparing(Failure::getScenario)
                                             .thenComparing(Failure::getStep)
                                             .thenComparing(Failure::getMessage)
        );
        message.format("%n Failures & Errors:%n");
        AT_Context context = new AT_Context();
        AsciiTable table = new AsciiTable(context);
        context.setFrameLeftMargin(MARGIN);
        table.addRule();
        table.addRow("STORY", "SCENARIO", "STEP", "ERROR MESSAGE");
        table.addRule();
        failures.forEach(
                f -> table.addRow(
                    f.getStory(), wrap(f.getScenario()), f.getStep().replaceAll("\n|\r\n", "<br>"), wrap(f.getMessage())
                )
        );
        table.addRule();
        table.setRenderer(AT_Renderer.create().setCWC(new CWC_LongestLine()));
        table.setPaddingLeftRight(1);
        table.setPaddingBottom(1);
        table.setTextAlignment(TextAlignment.LEFT);
        message.format("%s", table.render());
    }

    private static String wrap(String words)
    {
        return WordUtils.wrap(words, MAX_CELL_WIDTH);
    }

    public static void logPropertiesSecurely(Properties properties)
    {
        properties.entrySet()
                .stream()
                .map(e -> Map.entry((String) e.getKey(), e.getValue()))
                .sorted(Entry.comparingByKey())
                .forEach(TestInfoLogger::logPropertySecurely);
    }

    private static void logPropertySecurely(Entry<String, Object> entry)
    {
        String key = entry.getKey();
        LOGGER.atInfo()
                .addArgument(key)
                .addArgument(() -> SECURE_KEY_PATTERN.matcher(key).matches() ? "****" : entry.getValue())
                .log("{}={}");
    }
}
