/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.bdd.context;

import org.vividus.testcontext.TestContext;

public class ReportControlContext
{
    private final TestContext testContext;

    public ReportControlContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    public boolean isReportingEnabled()
    {
        return getContext().isEnabled();
    }

    public void enableReporting()
    {
        getContext().setEnabled(true);
    }

    public void disableReporting()
    {
        getContext().setEnabled(false);
    }

    private ReportingContext getContext()
    {
        return testContext.get(ReportingContext.class, ReportingContext::new);
    }

    private static final class ReportingContext
    {
        private boolean enabled;

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
        }
    }
}
