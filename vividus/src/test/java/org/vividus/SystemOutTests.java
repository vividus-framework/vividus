/*
 * Copyright 2019 the original author or authors.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class SystemOutTests
{
    private static final String CHARSET = StandardCharsets.UTF_8.toString();
    private static final PrintStream DEFAULT_PRINT_STREAM = System.out;

    private static ByteArrayOutputStream output;

    @BeforeClass
    public static void beforeClass() throws UnsupportedEncodingException
    {
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, CHARSET));
    }

    protected String getOutput() throws UnsupportedEncodingException
    {
        return output.toString(CHARSET);
    }

    @After
    public void after()
    {
        output.reset();
    }

    @AfterClass
    public static void afterBase() throws IOException
    {
        System.setOut(DEFAULT_PRINT_STREAM);
        output.close();
    }
}
