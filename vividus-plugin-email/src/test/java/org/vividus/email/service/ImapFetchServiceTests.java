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

package org.vividus.email.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.vividus.email.factory.EmailMessageFactory;

class ImapFetchServiceTests
{
    @Test
    void testInvalidDuration()
    {
        Duration duration = Duration.ofSeconds(5);
        EmailMessageFactory factory = mock(EmailMessageFactory.class);
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> new ImapFetchService(duration, 5, "INBOX", factory));
        assertEquals("Polling timeout must be not less than 5 seconds, but got 1000 milliseconds",
            exception.getMessage());
    }
}
