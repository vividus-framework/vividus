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

package org.vividus.report.allure.plugin;

import static io.qameta.allure.entity.TestResult.comparingByTimeAsc;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.SerializationUtils;

import io.qameta.allure.CommonJsonAggregator;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.tree.DefaultTreeLayer;
import io.qameta.allure.tree.TestResultGroupFactory;
import io.qameta.allure.tree.TestResultLeafFactory;
import io.qameta.allure.tree.TestResultTree;
import io.qameta.allure.tree.TestResultTreeGroup;
import io.qameta.allure.tree.TestResultTreeLeaf;
import io.qameta.allure.tree.Tree;
import io.qameta.allure.tree.TreeLayer;

public class VisualPlugin extends CommonJsonAggregator
{
    private static final String VISUAL = "visual";
    private static final Pattern VISUAL_PATTERN = Pattern.compile("Visual comparison: (.+?)$");

    protected VisualPlugin()
    {
        super("visual.json");
    }

    @Override
    protected Tree<TestResult> getData(List<LaunchResults> launches)
    {
        final Tree<TestResult> visual = new TestResultTree(VISUAL, VisualPlugin::groupByBaselines,
                new TestResultGroupFactory(), new VisualTestResultLeafFactory());

        launches.stream()
                .map(LaunchResults::getResults)
                .flatMap(Collection::stream)
                .filter(getTestResultWithVisualAttachments())
                .sorted(comparingByTimeAsc())
                .forEach(visual::add);
        return visual;
    }

    private static Stream<Step> getSteps(TestResult testResult)
    {
        return testResult.getTestStage()
                .getSteps()
                .stream()
                .flatMap(VisualPlugin::flattenSteps);
    }

    private static Stream<Step> flattenSteps(Step step)
    {
        return Stream.concat(Stream.of(step), step.getSteps().stream().flatMap(VisualPlugin::flattenSteps));
    }

    private static String extractBaselineName(String attachmentName)
    {
        Matcher matcher = VISUAL_PATTERN.matcher(attachmentName);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static boolean isVisualAttachment(String attachmentName)
    {
        return VISUAL_PATTERN.matcher(attachmentName).find();
    }

    private static Predicate<TestResult> getTestResultWithVisualAttachments()
    {
        return testResult -> getAttachmentNames(testResult).stream()
                .anyMatch(VisualPlugin::isVisualAttachment);
    }

    private static Set<String> getAttachmentNames(TestResult testResult)
    {
        return getSteps(testResult)
                .map(Step::getAttachments)
                .flatMap(Collection::stream)
                .map(Attachment::getName)
                .filter(VisualPlugin::isVisualAttachment)
                .collect(Collectors.toSet());
    }

    private static List<TreeLayer> groupByBaselines(final TestResult testResult)
    {
        final List<String> baselines = getAttachmentNames(testResult).stream()
                .map(VisualPlugin::extractBaselineName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        final TreeLayer baselinesLayer = new DefaultTreeLayer(baselines);
        return List.of(baselinesLayer);
    }

    private static class VisualTestResultLeafFactory extends TestResultLeafFactory
    {
        private static final List<Status> STATUS_LIST = List.of(Status.BROKEN, Status.FAILED, Status.SKIPPED,
                Status.UNKNOWN, Status.PASSED);

        private static Optional<Status> getStatus(TestResult testResult, String baselineName)
        {
            return getSteps(testResult)
                    .filter(step -> step.getAttachments().stream().map(Attachment::getName)
                            .map(VisualPlugin::extractBaselineName).filter(Objects::nonNull)
                            .anyMatch(name -> name.equals(baselineName)))
                    .map(Step::getStatus).min(Comparator.comparingInt(STATUS_LIST::indexOf));
        }

        @Override
        public TestResultTreeLeaf create(final TestResultTreeGroup parent, final TestResult item)
        {
            TestResult groupTestResult = SerializationUtils.clone(item);
            getStatus(item, parent.getName()).ifPresent(groupTestResult::setStatus);
            return new TestResultTreeLeaf(parent.getUid(), groupTestResult);
        }
    }
}
