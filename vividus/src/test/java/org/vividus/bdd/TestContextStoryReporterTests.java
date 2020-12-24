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

package org.vividus.bdd;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

import org.jbehave.core.reporters.StoryReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.testcontext.TestContext;

@ExtendWith(MockitoExtension.class)
class TestContextStoryReporterTests
{
    @Mock private StoryReporter nextStoryReporter;
    @Mock private TestContext testContext;
    @InjectMocks private TestContextStoryReporter testContextStoryReporter;

    @BeforeEach
    void beforeEach()
    {
        testContextStoryReporter.setNext(nextStoryReporter);
    }

    @ParameterizedTest
    @CsvSource({
        "false, 1",
        "true,  0"
    })
    void testAfterStory(boolean givenStory, int numberOfInvocations)
    {
        testContextStoryReporter.afterStory(givenStory);
        InOrder ordered = inOrder(testContext, nextStoryReporter);
        ordered.verify(nextStoryReporter).afterStory(givenStory);
        ordered.verify(testContext, times(numberOfInvocations)).clear();
        ordered.verifyNoMoreInteractions();
    }
}
