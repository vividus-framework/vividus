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
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;

import org.springframework.stereotype.Component;
import org.vividus.xray.model.CucumberTestCase;

@Component
public class CucumberTestCaseSerializer extends AbstractTestCaseSerializer<CucumberTestCase>
{
    private static final String CUCUMBER_SCENARIO_TYPE_FIELD_KEY = "cucumber-scenario-type";
    private static final String CUCUMBER_SCENARIO_FIELD_KEY = "cucumber-scenario";

    @Override
    protected void serializeCustomFields(CucumberTestCase testCase, Map<String, String> mapping,
            JsonGenerator generator) throws IOException
    {
        writeObjectWithValueField(generator, getSafely(CUCUMBER_SCENARIO_TYPE_FIELD_KEY, mapping),
                testCase.getScenarioType());
        generator.writeStringField(getSafely(CUCUMBER_SCENARIO_FIELD_KEY, mapping), testCase.getScenario());
    }
}
