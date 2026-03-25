/*
 * Copyright 2019-2025 the original author or authors.
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

import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.reporter.metadata.MetadataCategory;
import org.vividus.reporter.metadata.MetadataEntry;
import org.vividus.reporter.metadata.MetadataProvider;
import org.vividus.results.ResultsProvider;
import org.vividus.results.model.ExecutableEntity;
import org.vividus.results.model.Failure;
import org.vividus.results.model.Statistic;
import org.vividus.util.ResourceUtils;

public final class TestInfoLogger
{
    private static final String HYPHEN = "-";
    private static final int HEADER_SIZE = 40;
    private static final String CATEGORY_FORMAT = "%s%n %s:%n";
    private static final String NEW_LINE = "%n";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestInfoLogger.class);
    private static final Pattern SECURE_KEY_PATTERN = Pattern.compile(
            "password|secret|token|key([^s]|$)", Pattern.CASE_INSENSITIVE);
    private static final int HORIZONTAL_RULE_LENGTH = 80;
    private static final String HORIZONTAL_RULE = HYPHEN.repeat(HORIZONTAL_RULE_LENGTH);
    private static final String CONFIGURATION_SET = "Set";

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
            int maxKeyLength = Stream.of(MetadataCategory.values())
                    .map(MetadataProvider::getMetaDataByCategory)
                    .flatMap(List::stream)
                    .map(MetadataEntry::getName)
                    .mapToInt(String::length)
                    .max().orElse(0);
            String propertyFormat = "   %-" + maxKeyLength + "s %s%n";
            try (Formatter message = new Formatter(Locale.ROOT))
            {
                message.format(NEW_LINE);
                Stream.of(MetadataCategory.values()).forEach(category -> {
                    Map<String, String> asMap = MetadataProvider.getMetaDataByCategoryAsMap(category);
                    if (category == MetadataCategory.CONFIGURATION && asMap.get(CONFIGURATION_SET) != null)
                    {
                        String configurationSetFormat = "%s%n %-" + maxKeyLength + "s   %s%n";
                        message.format(configurationSetFormat, HORIZONTAL_RULE, "Configuration set:",
                                asMap.get(CONFIGURATION_SET));
                        asMap.remove(CONFIGURATION_SET);
                    }
                    else
                    {
                        String categoryName = category.name();
                        if (category != MetadataCategory.VIVIDUS)
                        {
                            categoryName = WordUtils.capitalize(categoryName.toLowerCase());
                        }
                        message.format(CATEGORY_FORMAT, HORIZONTAL_RULE, categoryName);
                    }
                    asMap.forEach((description, value) -> message.format(propertyFormat, description, value));
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

            try (Formatter message = new Formatter(Locale.ROOT))
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
        String row = "%n %-12s %6s %10s %8s";
        message.format("%n Execution statistics:");
        String rowsSeparator = "%n " + HYPHEN.repeat(HEADER_SIZE);
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
        message.format(rowsSeparator);
        resultsProvider.getFailures().ifPresent(failures -> addFailures(message, failures));
    }

    private void addFailures(Formatter message, List<Failure> failures)
    {
        if (failures.isEmpty())
        {
            message.format("%n%n No Failures & Errors!");
            return;
        }
        failures.sort(Comparator.comparing(Failure::getStory)
                                .thenComparing(Failure::getScenario)
                                .thenComparing(Failure::getStep)
                                .thenComparing(Failure::getMessage));
        message.format("%n%n Failures & Errors:");
        String errorIndent = " ".repeat("         └── Error: ".length());
        String currentStory = null;
        String currentScenario = null;
        for (Failure f : failures)
        {
            if (!f.getStory().equals(currentStory))
            {
                currentStory = f.getStory();
                currentScenario = null;
                message.format("%n%n Story: %s", currentStory);
            }
            if (!f.getScenario().equals(currentScenario))
            {
                currentScenario = f.getScenario();
                message.format("%n └── Scenario: %s", currentScenario);
            }
            String step = f.getStep().replaceAll("[\\r\\n]+", " ");
            message.format("%n     └── Step: %s", step);
            String indentedError = f.getMessage().replaceAll("\\r?\\n", "\n" + errorIndent);
            message.format("%n         └── Error: %s", indentedError);
        }
    }

    public static void logPropertiesSecurely(Properties properties)
    {
        try (Formatter message = new Formatter(Locale.ROOT))
        {
            properties.entrySet()
                    .stream()
                    .map(e -> Map.entry((String) e.getKey(), e.getValue()))
                    .sorted(Entry.comparingByKey())
                    .forEach(property -> {
                        String key = property.getKey();
                        Object value = SECURE_KEY_PATTERN.matcher(key).find() ? "****" : property.getValue();
                        message.format("%n%s=%s", key, value);
                    });
            LOGGER.atInfo().addArgument(message::toString).log("Properties and environment variables:{}");
        }
    }
}
