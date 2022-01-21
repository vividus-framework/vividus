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

package org.vividus.xray.databind;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;

import org.springframework.stereotype.Component;
import org.vividus.output.ManualTestStep;
import org.vividus.xray.model.ManualTestCase;

@Component
public class ManualTestCaseSerializer extends AbstractTestCaseSerializer<ManualTestCase>
{
    private static final String MANUAL_STEPS_FIELD_KEY = "manual-steps";

    @Override
    protected void serializeCustomFields(ManualTestCase testCase, Map<String, String> mapping, JsonGenerator generator)
            throws IOException
    {
        List<ManualTestStep> manualTestSteps = testCase.getManualTestSteps();

        generator.writeObjectFieldStart(getSafely(MANUAL_STEPS_FIELD_KEY, mapping));
        generator.writeArrayFieldStart("steps");
        for (int stepIndex = 0; stepIndex < manualTestSteps.size(); stepIndex++)
        {
            ManualTestStep manualTestStep = manualTestSteps.get(stepIndex);
            generator.writeStartObject();
            generator.writeNumberField("index", stepIndex + 1);
            generator.writeObjectFieldStart("fields");
            generator.writeStringField("Action", manualTestStep.getAction());
            generator.writeStringField("Data", manualTestStep.getData());
            generator.writeStringField("Expected Result", manualTestStep.getExpectedResult());
            generator.writeEndObject();
            generator.writeEndObject();
        }
        generator.writeEndArray();
    }
}
