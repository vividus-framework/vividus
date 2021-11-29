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

import java.util.List;

import org.jbehave.core.reporters.TxtOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingStoryReporter extends TxtOutput
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingStoryReporter.class);

    @SuppressWarnings("resource")
    public LoggingStoryReporter()
    {
        super(new LoggingPrintStream(LOGGER));
    }

    @Override
    public void pendingMethods(List<String> methods)
    {
        // Skip logging as it's not informative
    }

    @Override
    public void successful(String step)
    {
        // Skip logging as it's not informative
    }

    @Override
    public void failed(String step, Throwable storyFailure)
    {
        super.failed(step, storyFailure);
        LOGGER.error("Step is failed with exception", storyFailure);
    }
}
