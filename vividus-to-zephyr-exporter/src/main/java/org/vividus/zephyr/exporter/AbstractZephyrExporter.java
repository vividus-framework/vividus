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

package org.vividus.zephyr.exporter;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.vividus.jira.JiraConfigurationException;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.databind.TestCaseDeserializer;
import org.vividus.zephyr.facade.ZephyrFacade;
import org.vividus.zephyr.model.TestCase;
import org.vividus.zephyr.parser.TestCaseParser;

public abstract class AbstractZephyrExporter implements ZephyrExporter
{
    private final ZephyrFacade zephyrFacade;
    private final TestCaseParser testCaseParser;
    private final ObjectMapper objectMapper;

    protected AbstractZephyrExporter(ZephyrFacade zephyrFacade, TestCaseParser testCaseParser)
    {
        this.zephyrFacade = zephyrFacade;
        this.testCaseParser = testCaseParser;
        this.objectMapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .build()
                .registerModule(new SimpleModule().addDeserializer(TestCase.class, new TestCaseDeserializer()));
    }

    @Override
    public void exportResults() throws IOException, JiraConfigurationException
    {
        List<TestCase> testCasesForImporting = testCaseParser.createTestCases(objectMapper);
        ZephyrConfiguration configuration = getZephyrFacade().prepareConfiguration();
        for (TestCase testCase : testCasesForImporting)
        {
            exportTestExecution(testCase, configuration);
        }
    }

    protected abstract void exportTestExecution(TestCase testCase, ZephyrConfiguration configuration)
            throws IOException, JiraConfigurationException;

    protected ZephyrFacade getZephyrFacade()
    {
        return zephyrFacade;
    }

    protected ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }
}
