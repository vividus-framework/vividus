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

package org.vividus.crossbrowsertesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import com.crossbrowsertesting.AutomatedTest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.cloud.AbstractCloudTestStatusManager.UpdateCloudTestStatusException;

@ExtendWith(MockitoExtension.class)
class CbtTestStatusManagerTests
{
    private static final String SESSION_ID = "session-id";
    private static final String STATUS = "status";

    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private CbtTestStatusManager testStatusManager;

    @Test
    void shouldUpdateCloudTestStatus() throws UpdateCloudTestStatusException, UnirestException
    {
        try (var automatedTestConstruction = mockConstruction(AutomatedTest.class))
        {
            testStatusManager.updateCloudTestStatus(SESSION_ID, STATUS);

            var automatedTest = automatedTestConstruction.constructed().get(0);
            verify(automatedTest).setScore(STATUS);
        }
    }

    @SuppressWarnings("try")
    @Test
    void shouldWrapException()
    {
        UnirestException exception = mock();
        try (var ignored = mockConstruction(AutomatedTest.class,
                (mock, context) -> doThrow(exception).when(mock).setScore(STATUS)))
        {
            var thrown = assertThrows(UpdateCloudTestStatusException.class,
                () -> testStatusManager.updateCloudTestStatus(SESSION_ID, STATUS));
            assertEquals(exception, thrown.getCause());
        }
    }
}
