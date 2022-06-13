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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.facade.ZephyrScaleFacade;
import org.vividus.zephyr.model.TestCase;
import org.vividus.zephyr.model.TestCaseStatus;
import org.vividus.zephyr.parser.TestCaseParser;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class ZephyrScaleExporterTests
{
    private static final String TEST_CASE_KEY1 = "TEST-1";
    private static final String TEST_CASE_KEY2 = "TEST-2";
    private static final String STATUS_UPDATE_JSON = "{\"status\":\"-1\"}";
    private static final String PASSED_STATUS_ID = "101";

    @Mock private ZephyrScaleFacade zephyrScaleFacade;
    @Mock private TestCaseParser testCaseParser;
    @InjectMocks private ZephyrScaleExporter zephyrExporter;

    @Test
    void testExportResults() throws IOException, URISyntaxException, JiraConfigurationException
    {
        when(testCaseParser.createTestCases(any())).thenReturn(List.of(
                new TestCase(TEST_CASE_KEY1, TestCaseStatus.SKIPPED),
                new TestCase(TEST_CASE_KEY2, TestCaseStatus.PASSED)));
        when(zephyrScaleFacade.prepareConfiguration()).thenReturn(prepareTestConfiguration());
        when(zephyrScaleFacade.createExecution(TEST_CASE_KEY1)).thenReturn(111);
        when(zephyrScaleFacade.createExecution(TEST_CASE_KEY2)).thenReturn(222);
        zephyrExporter.exportResults();
        verify(zephyrScaleFacade).updateExecutionStatus(TEST_CASE_KEY1, STATUS_UPDATE_JSON);
        verify(zephyrScaleFacade).updateExecutionStatus(TEST_CASE_KEY2, "{\"status\":\"101\"}");
    }

    private ZephyrConfiguration prepareTestConfiguration()
    {
        ZephyrConfiguration configuration = new ZephyrConfiguration();
        configuration.setProjectId("11111");
        configuration.setVersionId("11112");
        configuration.setCycleId("11113");
        configuration.setFolderId("11114");
        Map<TestCaseStatus, String> map = new EnumMap<>(TestCaseStatus.class);
        map.put(TestCaseStatus.SKIPPED, "-1");
        map.put(TestCaseStatus.PASSED, PASSED_STATUS_ID);
        configuration.setTestStatusPerZephyrStatusMapping(map);
        return configuration;
    }
}
