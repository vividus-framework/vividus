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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.Attachment;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;
import io.qameta.allure.tree.TestResultTreeGroup;
import io.qameta.allure.tree.TestResultTreeLeaf;

@ExtendWith(MockitoExtension.class)
class VisualPluginTests
{
    private static final String VISUAL_COMPARISON_BASELINE_2 = "Visual comparison: baseline2";

    @Test
    void shouldCreateTree()
    {
        var visualAttachment1 = createAttachment("Visual comparison: baseline1");
        var attachment1 = createAttachment("Attachment1");
        var attachment2 = createAttachment("Attachment2");
        var attachment3 = createAttachment("Attachment3");
        var visualAttachment2 = createAttachment(VISUAL_COMPARISON_BASELINE_2);
        var visualAttachment3 = createAttachment("Visual comparison: baseline3");
        var visualAttachment4 = createAttachment(VISUAL_COMPARISON_BASELINE_2);

        var step1 = createStep("Step1", Status.PASSED, visualAttachment1);
        var step2 = createStep("Step2", Status.SKIPPED, attachment1);
        var step3 = createStep("Step3", Status.BROKEN, visualAttachment3, attachment2);
        var step4 = createStep("Step4", Status.PASSED, attachment3);
        var innerStep = createStep("InnerStep", Status.PASSED, attachment1, visualAttachment2);
        step4.setSteps(List.of(innerStep));
        var step5 = createStep("Step5", Status.UNKNOWN);
        var step6 = createStep("Step6", Status.PASSED, visualAttachment4);

        var testResult1Time = new Time().setStart(1L);
        var testResult2Time = new Time().setStart(2L);
        var testResult3Time = new Time().setStart(3L);

        var testResult1 = createTestResult("TestResult1", Status.FAILED, testResult1Time, step1, step2, step3, step6);
        var testResult2 = createTestResult("TestResult2", Status.PASSED, testResult2Time, step4);
        var testResult3 = createTestResult("TestResult3", Status.FAILED, testResult3Time, step5);

        var launchResults = mock(LaunchResults.class);
        when(launchResults.getResults()).thenReturn(Set.of(testResult1, testResult2, testResult3));

        var resultTree = new VisualPlugin().getData(List.of(launchResults));

        assertEquals("visual", resultTree.getName());

        var treeGroups =  resultTree.getChildren();

        assertThat(treeGroups, allOf(hasSize(3), everyItem(instanceOf(TestResultTreeGroup.class))));

        var group1 = (TestResultTreeGroup) treeGroups.get(0);
        var group2 = (TestResultTreeGroup) treeGroups.get(1);
        var group3 = (TestResultTreeGroup) treeGroups.get(2);

        var group1TestResults = List.of(SerializationUtils.clone(testResult1).setStatus(Status.PASSED));
        var group2TestResults = List.of(SerializationUtils.clone(testResult1).setStatus(Status.PASSED), testResult2);
        var group3TestResults = List.of(SerializationUtils.clone(testResult1).setStatus(Status.BROKEN));

        assertTestResultTreeGroup(group1, "baseline1", group1TestResults);
        assertTestResultTreeGroup(group2, "baseline2", group2TestResults);
        assertTestResultTreeGroup(group3, "baseline3", group3TestResults);
    }

    private Attachment createAttachment(String name)
    {
        Attachment attachment = new Attachment();
        attachment.setName(name);
        return attachment;
    }

    private Step createStep(String name, Status status, Attachment... attachments)
    {
        Step step = new Step();
        step.setName(name);
        step.setStatus(status);
        step.setAttachments(Arrays.asList(attachments));
        return step;
    }

    private TestResult createTestResult(String uid, Status status, Time time, Step... steps)
    {
        TestResult testResult = new TestResult();
        testResult.setStatus(status);
        testResult.setUid(uid);
        testResult.setTime(time);
        testResult.setTestStage(new StageResult().setSteps(Arrays.asList(steps)));
        return testResult;
    }

    private void assertTestResultTreeGroup(TestResultTreeGroup actualGroup, String expectedName,
            List<TestResult> expectedTestResults)
    {
        assertEquals(expectedName, actualGroup.getName());

        var actualLeaves = actualGroup.getChildren();

        assertEquals(expectedTestResults.size(), actualLeaves.size());

        List<TestResultTreeLeaf> expectedLeaves = expectedTestResults.stream()
                .map(child -> new TestResultTreeLeaf(actualGroup.getUid(), child)).collect(Collectors.toList());

        for (int i = 0; i < expectedLeaves.size(); i++)
        {
            var expectedLeaf = expectedLeaves.get(i);
            var actualLeaf = actualLeaves.get(i);
            assertThat(actualLeaf, samePropertyValuesAs(expectedLeaf));
        }
    }
}
