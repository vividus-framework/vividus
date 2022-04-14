/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.runner;

import java.util.Properties;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;
import org.vividus.log.TestInfoLogger;
import org.vividus.results.ResultsProvider;
import org.vividus.results.model.ExitCode;

public abstract class AbstractTestRunner extends JUnitStories
{
    private static Class<?> runnerClass;

    protected AbstractTestRunner()
    {
        Vividus.init();
        TestInfoLogger.logPropertiesSecurely(System.getProperties());
        TestInfoLogger.logPropertiesSecurely(BeanFactory.getBean("properties", Properties.class));
    }

    protected static void setRunnerClass(Class<?> runnerClass)
    {
        AbstractTestRunner.runnerClass = runnerClass;
    }

    @Override
    public Configuration configuration()
    {
        return configuredEmbedder().configuration();
    }

    @Override
    public InjectableStepsFactory stepsFactory()
    {
        return configuredEmbedder().stepsFactory();
    }

    @SuppressWarnings("NoMainMethodInAbstractClass")
    public static void main(String[] args) throws InitializationError
    {
        Runner runner = new JUnit4(runnerClass);
        Result result = new JUnitCore().run(runner);
        ExitCode exitCode;
        if (result.getFailureCount() > 0)
        {
            Logger logger = LoggerFactory.getLogger(runnerClass);
            result.getFailures().forEach(f ->
            {
                logger.error("Failure: {}", f);
                logger.atError().addArgument(f::getTrace).log("{}");
            });
            exitCode = ExitCode.ERROR;
        }
        else
        {
            exitCode = BeanFactory.getBean(ResultsProvider.class).calculateExitCode();
        }
        System.exit(exitCode.getExitCode());
    }
}
