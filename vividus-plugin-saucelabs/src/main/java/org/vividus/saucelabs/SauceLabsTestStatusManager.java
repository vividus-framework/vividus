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

package org.vividus.saucelabs;

import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.cloud.AbstractCloudTestStatusManager;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.action.JavascriptActions;

public class SauceLabsTestStatusManager extends AbstractCloudTestStatusManager
{
    private final JavascriptActions javascriptActions;

    public SauceLabsTestStatusManager(IWebDriverProvider webDriverProvider, TestContext testContext,
            JavascriptActions javascriptActions)
    {
        super(webDriverProvider, testContext);
        this.javascriptActions = javascriptActions;
    }

    @Override
    protected void updateCloudTestStatus(String status) throws UpdateCloudTestStatusException
    {
        javascriptActions.executeScript("sauce:job-result=" + status);
    }
}
