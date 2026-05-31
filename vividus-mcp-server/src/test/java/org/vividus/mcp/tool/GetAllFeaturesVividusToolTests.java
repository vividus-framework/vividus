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

package org.vividus.mcp.tool;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbehave.core.expressions.DelegatingExpressionProcessor;
import org.jbehave.core.expressions.SingleArgExpressionProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.vividus.mcp.VividusMcpServer.StepInfo;
import org.vividus.mcp.tool.GetAllFeaturesVividusTool.GetAllFeaturesResponse;
import org.vividus.runner.StepsCollector;
import org.vividus.runner.StepsCollector.Step;
import org.vividus.variable.DynamicVariable;

class GetAllFeaturesVividusToolTests
{
    @Test
    void shouldReturnContentObject()
    {
        SingleArgExpressionProcessor<String> directProcessor =
                new SingleArgExpressionProcessor<>("directExpr", s -> s);
        DelegatingExpressionProcessor delegatingProcessor = new DelegatingExpressionProcessor(
                List.of(new SingleArgExpressionProcessor<>("delegatedExpr", s -> s)));
        DynamicVariable dynamicVariable = () -> null;

        Step step = mock();
        when(step.getStartingWord()).thenReturn("When");
        when(step.getPattern()).thenReturn("I perform action");
        String module = "module";
        when(step.getLocation()).thenReturn(module);

        Step debugStep = mock();
        when(debugStep.getPattern()).thenReturn("I debug");

        Step deprecatedStep = mock();
        when(deprecatedStep.isDeprecated()).thenReturn(true);

        GetAllFeaturesVividusTool tool = new GetAllFeaturesVividusTool(
                List.of(directProcessor, delegatingProcessor),
                Map.of("my-variable", dynamicVariable));

        try (MockedStatic<StepsCollector> stepCollector = Mockito.mockStatic(StepsCollector.class))
        {
            stepCollector.when(StepsCollector::getSteps).thenReturn(Set.of(step, deprecatedStep, debugStep));

            GetAllFeaturesResponse response = (GetAllFeaturesResponse) tool.getContent();
            assertEquals(List.of("#{delegatedExpr(...)}", "#{directExpr(...)}"), response.expressions());
            assertEquals(List.of("${my-variable}"), response.dynamicVariables());
            assertThat(response.stepParameters().keySet(), hasSize(7));
            List<StepInfo> stepInfos = response.steps();
            assertThat(stepInfos, hasSize(1));
            StepInfo stepInfo = stepInfos.get(0);
            assertEquals("When I perform action", stepInfo.getName());
            assertEquals(module, stepInfo.getModule());
        }
    }
}
