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

package org.vividus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

public class SystemStreamTests
{
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final PrintStream DEFAULT_OUT_STREAM = System.out;
    private static final PrintStream DEFAULT_ERR_STREAM = System.err;

    private static ByteArrayOutputStream out;
    private static ByteArrayOutputStream err;

    protected String getOutStreamContent()
    {
        return out.toString(CHARSET);
    }

    protected String getErrStreamContent()
    {
        return err.toString(CHARSET);
    }

    @BeforeClass
    @BeforeAll
    public static void beforeClass()
    {
        out = new ByteArrayOutputStream();
        System.setOut(createPrintStream(out));
        err = new ByteArrayOutputStream();
        System.setErr(createPrintStream(err));
    }

    @After
    @AfterEach
    public void after()
    {
        out.reset();
        err.reset();
    }

    @AfterClass
    @AfterAll
    public static void afterBase() throws IOException
    {
        System.setOut(DEFAULT_OUT_STREAM);
        out.close();
        System.setErr(DEFAULT_ERR_STREAM);
        err.close();
    }

    protected void assertOutput(List<String> lines)
    {
        String lineSeparator = System.lineSeparator();
        String expectedOutput = lines.stream().collect(Collectors.joining(lineSeparator, "", lineSeparator));
        assertEquals(expectedOutput, getOutStreamContent());
    }

    private static PrintStream createPrintStream(ByteArrayOutputStream out)
    {
        return new PrintStream(out, true, CHARSET);
    }
}
