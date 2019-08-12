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

package org.vividus.bdd.log;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.PrintStreamEmbedderMonitor;
import org.jbehave.core.failures.BatchFailures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEmbedderMonitor extends PrintStreamEmbedderMonitor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEmbedderMonitor.class);

    @SuppressWarnings("resource")
    public LoggingEmbedderMonitor() throws UnsupportedEncodingException
    {
        super(new LoggingPrintStream(LOGGER));
    }

    @Override
    public void runningStory(String path)
    {
        // Nothing to do
    }

    @Override
    public void batchFailed(BatchFailures failures)
    {
        // Nothing to do
    }

    @Override
    public void processingSystemProperties(Properties properties)
    {
        // Nothing to do
    }

    @Override
    public void usingControls(EmbedderControls embedderControls)
    {
        // Nothing to doF
    }
}
