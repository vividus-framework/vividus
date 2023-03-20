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

package org.vividus.expressions;

import org.jbehave.core.expressions.PrintingExpressionResolverMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingExpressionResolverMonitor extends PrintingExpressionResolverMonitor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingExpressionResolverMonitor.class);

    @Override
    protected void print(String format, Object... args)
    {
        LOGGER.atError().addArgument(() -> String.format(format, args)).log("{}");
    }

    @Override
    protected void printStackTrace(Throwable e)
    {
        // Do not print full stack trace
    }
}
