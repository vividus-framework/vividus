/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.ui.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;

@ExtendWith(MockitoExtension.class)
class DescriptiveWaitTests
{
    private static final int TIMEOUT_VALUE = 1;
    private static final Duration TIMEOUT = Duration.ofSeconds(TIMEOUT_VALUE);

    @Mock
    private FluentWait<WebDriver> fluentWaitMock;

    @Mock
    private Function<WebDriver, Boolean> isTrue;

    @Test
    void testUntil()
    {
        String description = "mocked function";
        when(isTrue.toString()).thenReturn(description);
        DescriptiveWait<WebDriver> wait = new DescriptiveWait<>(fluentWaitMock);
        wait.setTimeout(TIMEOUT);
        wait.until(isTrue);
        verify(fluentWaitMock).withTimeout(TIMEOUT);
        verify(fluentWaitMock).until(isTrue);
        assertEquals("Wait with timeout of " + TIMEOUT_VALUE + " second. Condition: " + description, wait.toString());
    }
}
