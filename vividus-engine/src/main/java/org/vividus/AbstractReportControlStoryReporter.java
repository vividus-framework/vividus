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

package org.vividus;

import org.vividus.context.ReportControlContext;
import org.vividus.context.RunContext;

public abstract class AbstractReportControlStoryReporter extends ChainedStoryReporter
{
    private final ReportControlContext reportControlContext;
    private final RunContext runContext;

    protected AbstractReportControlStoryReporter(ReportControlContext reportControlContext,
            RunContext runContext)
    {
        this.reportControlContext = reportControlContext;
        this.runContext = runContext;
    }

    protected void perform(Runnable run)
    {
        if (reportControlContext.isReportingEnabled() && getRunContext().isRunInProgress())
        {
            run.run();
        }
    }

    protected RunContext getRunContext()
    {
        return runContext;
    }
}
