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

package org.vividus.saucelabs;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.cloud.AbstractCloudTestStatusManager.UpdateCloudTestStatusException;
import org.vividus.ui.action.JavascriptActions;

@ExtendWith(MockitoExtension.class)
class SauceLabsTestStatusManagerTests
{
    @Mock private JavascriptActions javascriptActions;
    @InjectMocks private SauceLabsTestStatusManager manager;

    @Test
    void shouldUpdateSessionStatus() throws UpdateCloudTestStatusException
    {
        manager.updateCloudTestStatus(null, "status");

        verify(javascriptActions).executeScript("sauce:job-result=status");
        verifyNoMoreInteractions(javascriptActions);
    }
}
