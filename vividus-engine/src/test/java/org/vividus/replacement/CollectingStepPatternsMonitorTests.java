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

package org.vividus.replacement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.hamcrest.Matchers;
import org.jbehave.core.model.StepPattern;
import org.jbehave.core.steps.StepType;
import org.junit.jupiter.api.Test;

class CollectingStepPatternsMonitorTests
{
    private final CollectingStepPatternsMonitor collectingStepPatternsMonitor = new CollectingStepPatternsMonitor();

    @Test
    void shouldCollectMatchedPatterns()
    {
        String step1 = "GIVEN step1";
        String step2 = "THEN step2";
        String stepPatternAsString1 = "step1";
        String stepPatternAsString2 = "step2";

        StepPattern stepPattern1 = new StepPattern(StepType.GIVEN, stepPatternAsString1, stepPatternAsString1);
        StepPattern stepPattern2 = new StepPattern(StepType.THEN, stepPatternAsString2, stepPatternAsString2);
        collectingStepPatternsMonitor.stepMatchesPattern(step1, true, stepPattern1, null, null);
        collectingStepPatternsMonitor.stepMatchesPattern("WHEN not matched step", false, null, null, null);
        collectingStepPatternsMonitor.stepMatchesPattern(step2, true, stepPattern2, null, null);

        Map<String, Pattern> expectedStepPatternsCache = new ConcurrentHashMap<>();
        Pattern pattern1 = Pattern.compile(stepPatternAsString1, Pattern.DOTALL);
        Pattern pattern2 = Pattern.compile(stepPatternAsString2, Pattern.DOTALL);
        expectedStepPatternsCache.put(step1, pattern1);
        expectedStepPatternsCache.put(step2, pattern2);
        Map<String, Pattern> actualStepPatterns = collectingStepPatternsMonitor.getRegisteredStepPatterns();
        assertEquals(expectedStepPatternsCache.keySet(), actualStepPatterns.keySet());
        assertEquals(Pattern.DOTALL, expectedStepPatternsCache.get(step1).flags());
        assertThat(List.of(pattern1.pattern(), pattern2.pattern()), Matchers.containsInAnyOrder(
                actualStepPatterns.values().stream().map(Pattern::pattern).toArray()));
    }
}
