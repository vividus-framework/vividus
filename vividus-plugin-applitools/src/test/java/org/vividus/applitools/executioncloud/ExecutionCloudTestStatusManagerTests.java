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

package org.vividus.applitools.executioncloud;

import static org.mockito.Mockito.verify;

import java.util.Map;

import com.applitools.eyes.TestResultsStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.cloud.AbstractCloudTestStatusManager.UpdateCloudTestStatusException;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.action.JavascriptActions;

@ExtendWith(MockitoExtension.class)
class ExecutionCloudTestStatusManagerTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private TestContext testContext;
    @Mock private JavascriptActions javascriptActions;
    @InjectMocks private ExecutionCloudTestStatusManager manager;

    @Test
    void shouldUpdateCloudTestStatus() throws UpdateCloudTestStatusException
    {
        String status = TestResultsStatus.Passed.name();

        manager.updateCloudTestStatus(status, status);

        verify(javascriptActions).executeScript("applitools:endTest", Map.of("status", status));
    }
}
