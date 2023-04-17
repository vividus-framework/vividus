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

package org.vividus.softassert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.batch.BatchConfiguration;
import org.vividus.batch.BatchStorage;
import org.vividus.context.RunContext;
import org.vividus.testcontext.TestContext;

@ExtendWith(MockitoExtension.class)
class JBehaveFailTestFastManagerTests
{
    private static final Object FAIL_TEST_CASE_FAST_KEY = JBehaveFailTestFastManager.class;

    @Mock private RunContext runContext;
    @Mock private BatchStorage batchStorage;
    @Mock private TestContext testContext;

    @InjectMocks private JBehaveFailTestFastManager failTestFastManager;

    @ParameterizedTest
    @CsvSource({
        "true, false, true",
        "false, true, false",
        "true, true,",
        "false, false,"
    })
    void shouldProvideFailFastValue(boolean expectedValue, boolean propertyValue, Boolean configurationValue)
    {
        var key = "key";
        failTestFastManager.setFailTestCaseFast(propertyValue);
        when(runContext.getRunningBatchKey()).thenReturn(key);
        var configuration = mock(BatchConfiguration.class);
        when(batchStorage.getBatchConfiguration(key)).thenReturn(configuration);
        when(configuration.isFailScenarioFast()).thenReturn(configurationValue);
        doAnswer(a ->
        {
            Supplier supplier = a.getArgument(1);
            return supplier.get();
        }).when(testContext).get(eq(FAIL_TEST_CASE_FAST_KEY), any(Supplier.class));
        assertEquals(expectedValue, failTestFastManager.isFailTestCaseFast());
    }

    @Test
    void shouldEnableTestCaseFailFast()
    {
        failTestFastManager.enableTestCaseFailFast();
        verify(testContext).put(FAIL_TEST_CASE_FAST_KEY, true);
    }

    @Test
    void shouldDisableTestCaseFailFast()
    {
        failTestFastManager.disableTestCaseFailFast();
        verify(testContext).put(FAIL_TEST_CASE_FAST_KEY, false);
    }
}
