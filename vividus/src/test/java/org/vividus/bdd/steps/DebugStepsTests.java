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

package org.vividus.bdd.steps;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.Sleeper;

@ExtendWith(MockitoExtension.class)
public class DebugStepsTests
{
    private static final Duration TIMEOUT = Duration.ofSeconds(1L);

    @Mock private ISoftAssert softAssert;
    @InjectMocks private DebugSteps debugSteps;

    @Test
    void shouldWaitForDebug()
    {
        try (MockedStatic<Sleeper> sleeper = mockStatic(Sleeper.class))
        {
            debugSteps.waitForDebug(TIMEOUT);
            sleeper.verify(() -> Sleeper.sleep(TIMEOUT));
            verify(softAssert).recordFailedAssertion("Use this step for debug purposes only");
        }
    }
}
