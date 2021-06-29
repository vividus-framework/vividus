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

package org.vividus.crossbrowsertesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import com.crossbrowsertesting.AutomatedTest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.cloud.AbstractCloudTestLinkPublisher.GetCloudTestUrlException;

@ExtendWith(MockitoExtension.class)
class CbtTestLinkPublisherTests
{
    private static final String WEB_URL = "webUrl";

    @InjectMocks private CbtTestLinkPublisher testLinkPublisher;

    @Test
    void shouldGetCloudTestUrl() throws UnirestException, GetCloudTestUrlException
    {
        try (MockedConstruction<AutomatedTest> automatedTestConstruction = mockConstruction(AutomatedTest.class,
                (mock, context) -> when(mock.getWebUrl()).thenReturn(WEB_URL)))
        {
            assertEquals(WEB_URL, testLinkPublisher.getCloudTestUrl(null));
        }
    }

    @Test
    void shouldWrapException()
    {
        UnirestException exception = mock(UnirestException.class);
        try (MockedConstruction<AutomatedTest> automatedTestConstruction = mockConstruction(AutomatedTest.class,
                (mock, context) -> doThrow(exception).when(mock).getWebUrl()))
        {
            GetCloudTestUrlException thrown = assertThrows(GetCloudTestUrlException.class,
                () -> testLinkPublisher.getCloudTestUrl(null));
            assertEquals(exception, thrown.getCause());
        }
    }
}
