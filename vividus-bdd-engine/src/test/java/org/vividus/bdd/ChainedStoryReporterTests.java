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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.reporters.StoryReporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChainedStoryReporterTests
{
    @Mock
    private StoryReporter nextStoryReporter;

    @InjectMocks
    private ChainedStoryReporter chainedStoryReporter;

    @Test
    void testDryRun()
    {
        chainedStoryReporter.setNext(nextStoryReporter);
        chainedStoryReporter.dryRun();
        verify(nextStoryReporter).dryRun();
    }

    @Test
    void testUnwrap()
    {
        Throwable cause = new Throwable();
        Throwable throwable = new UUIDExceptionWrapper(cause);
        assertEquals(cause, chainedStoryReporter.unwrap(throwable));
    }
}
