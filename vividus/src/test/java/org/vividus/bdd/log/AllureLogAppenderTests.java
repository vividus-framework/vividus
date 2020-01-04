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

package org.vividus.bdd.log;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AllureLogAppenderTests
{
    private static final String CHARSET = StandardCharsets.UTF_8.toString();

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    private final PrintStream defaultPrintStreamErr = System.err;

    @BeforeEach
    void beforeEach() throws UnsupportedEncodingException
    {
        System.setErr(new PrintStream(outContent, true, CHARSET));
    }

    @Test
    void testCreateAppenderWithNullName() throws UnsupportedEncodingException
    {
        Filter filter = mock(Filter.class);
        Layout<?> layout = mock(Layout.class);
        AllureLogAppender appender = AllureLogAppender.createAppender(null, filter, layout);
        assertTrue(outContent.toString(CHARSET).contains("No name provided for AllureLogAppender"));
        assertNull(appender);
    }

    @AfterEach
    void afterEach()
    {
        System.setErr(defaultPrintStreamErr);
    }
}
