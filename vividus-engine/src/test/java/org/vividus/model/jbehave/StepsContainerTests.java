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

package org.vividus.model.jbehave;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class StepsContainerTests
{
    @Test
    void shouldCreateStreamOfAllSteps()
    {
        AbstractStepsContainer container = new AbstractStepsContainer() { };

        Step beforeSystemStep = mock(Step.class);
        container.setBeforeSystemScenarioSteps(List.of(beforeSystemStep));

        Step beforeUserStep = mock(Step.class);
        container.setBeforeUserScenarioSteps(List.of(beforeUserStep));

        Step step = mock(Step.class);
        container.setSteps(List.of(step));

        Step afterUserStep = mock(Step.class);
        container.setAfterUserScenarioSteps(List.of(afterUserStep));

        Step afterSystemStep = mock(Step.class);
        container.setAfterSystemScenarioSteps(List.of(afterSystemStep));

        assertEquals(List.of(beforeSystemStep, beforeUserStep, step, afterUserStep, afterSystemStep),
                container.createStreamOfAllSteps().collect(Collectors.toList()));
    }
}
