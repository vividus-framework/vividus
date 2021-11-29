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

package org.vividus.log;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;

public class LoggingPrintStream extends PrintStream
{
    private final Logger logger;

    public LoggingPrintStream(Logger logger)
    {
        super(System.out, false, StandardCharsets.UTF_8);
        this.logger = logger;
    }

    @Override
    public void print(String str)
    {
        String msg = str.trim();
        if (!msg.isEmpty())
        {
            logger.info(msg);
        }
    }

    @Override
    public void println(String str)
    {
        print(str);
    }

    @Override
    public void println(Object obj)
    {
        println(String.valueOf(obj));
    }

    @Override
    public PrintStream printf(String format, Object... args)
    {
        print(String.format(format, args));
        return this;
    }

    @Override
    public void close()
    {
        // Nothing to do
    }
}
