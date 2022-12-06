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

package org.vividus.softassert;

import java.util.Optional;

import org.vividus.batch.BatchConfiguration;
import org.vividus.batch.BatchStorage;
import org.vividus.context.RunContext;

public class JBehaveFailTestFastManager implements FailTestFastManager
{
    private final RunContext runContext;
    private final BatchStorage batchStorage;

    private boolean failTestCaseFast;

    public JBehaveFailTestFastManager(RunContext runContext, BatchStorage batchStorage)
    {
        this.runContext = runContext;
        this.batchStorage = batchStorage;
    }

    @Override
    public boolean isFailTestCaseFast()
    {
        String batchKey = runContext.getRunningBatchKey();
        BatchConfiguration batchConfiguration = batchStorage.getBatchConfiguration(batchKey);
        return Optional.ofNullable(batchConfiguration.isFailScenarioFast()).orElse(failTestCaseFast);
    }

    public void setFailTestCaseFast(boolean failTestCaseFast)
    {
        this.failTestCaseFast = failTestCaseFast;
    }
}
