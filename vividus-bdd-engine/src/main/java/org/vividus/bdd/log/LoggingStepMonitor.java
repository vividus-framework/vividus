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

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.jbehave.core.model.StepPattern;
import org.jbehave.core.steps.PrintStreamStepMonitor;
import org.jbehave.core.steps.StepType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingStepMonitor extends PrintStreamStepMonitor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingStepMonitor.class);

    @SuppressWarnings("resource")
    public LoggingStepMonitor()
    {
        super(new LoggingPrintStream(LOGGER));
    }

    @Override
    public void stepMatchesType(String step, String previous, boolean matches, StepType stepType, Method method,
            Object stepsInstance)
    {
        // Skip logging as it's not informative
    }

    @Override
    public void stepMatchesPattern(String step, boolean matches, StepPattern stepPattern, Method method,
            Object stepsInstance)
    {
        // Skip logging as it's not informative
    }

    @Override
    public void usingAnnotatedNameForParameter(String name, int position)
    {
        // Skip logging as it's not informative
    }

    @Override
    public void usingParameterNameForParameter(String name, int position)
    {
        // Skip logging as it's not informative
    }

    @Override
    public void usingTableAnnotatedNameForParameter(String name, int position)
    {
        // Skip logging as it's not informative
    }

    @Override
    public void usingTableParameterNameForParameter(String name, int position)
    {
        // Skip logging as it's not informative
    }

    @Override
    public void usingNaturalOrderForParameter(int position)
    {
        // Skip logging as it's not informative
    }

    @Override
    public void foundParameter(String parameter, int position)
    {
        // Skip logging as it's not informative
    }

    @Override
    public void convertedValueOfType(String value, Type type, Object converted, Class<?> converterClass)
    {
        // Skip logging as it's not informative
    }
}
