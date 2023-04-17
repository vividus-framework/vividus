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

import java.util.Optional;

import org.vividus.batch.BatchConfiguration;
import org.vividus.batch.BatchStorage;
import org.vividus.context.RunContext;
import org.vividus.testcontext.TestContext;

public class JBehaveFailTestFastManager implements FailTestFastManager
{
    private static final Object FAIL_TEST_CASE_FAST_KEY = JBehaveFailTestFastManager.class;

    private final RunContext runContext;
    private final BatchStorage batchStorage;
    private final TestContext testContext;

    private boolean failTestCaseFast;

    public JBehaveFailTestFastManager(RunContext runContext, BatchStorage batchStorage, TestContext testContext)
    {
        this.runContext = runContext;
        this.batchStorage = batchStorage;
        this.testContext = testContext;
    }

    @Override
    public boolean isFailTestCaseFast()
    {
        String batchKey = runContext.getRunningBatchKey();
        BatchConfiguration batchConfiguration = batchStorage.getBatchConfiguration(batchKey);
        return testContext.get(FAIL_TEST_CASE_FAST_KEY,
                () -> Optional.ofNullable(batchConfiguration.isFailScenarioFast()).orElse(failTestCaseFast));
    }

    @Override
    public void enableTestCaseFailFast()
    {
        testContext.put(FAIL_TEST_CASE_FAST_KEY, true);
    }

    @Override
    public void disableTestCaseFailFast()
    {
        testContext.put(FAIL_TEST_CASE_FAST_KEY, false);
    }

    public void setFailTestCaseFast(boolean failTestCaseFast)
    {
        this.failTestCaseFast = failTestCaseFast;
    }
}
