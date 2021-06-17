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

package org.vividus.runner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.BatchedEmbedder;
import org.vividus.bdd.IBatchedPathFinder;
import org.vividus.bdd.IRunStatusProvider;
import org.vividus.bdd.Status;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;
import org.vividus.report.MetadataLogger;

public class GenericRunner extends JUnitStories
{
    private static final int RUN_PASSED_EXIT_CODE = 0;
    private static final int RUN_KNOWN_ISSUES_EXIT_CODE = 1;
    private static final int RUN_FAILED_EXIT_CODE = 2;
    private static final int ERROR_EXIT_CODE = 3;

    private static String embedderBeanName;
    private final Embedder embedder;
    private final IBatchedPathFinder batchedPathFinder;

    public GenericRunner()
    {
        Vividus.init();
        batchedPathFinder = BeanFactory.getBean(IBatchedPathFinder.class);
        embedder = BeanFactory.getBean(embedderBeanName, Embedder.class);
        MetadataLogger.logPropertiesSecurely(System.getProperties());
        MetadataLogger.logPropertiesSecurely(BeanFactory.getBean("properties", Properties.class));
    }

    protected static void setEmbedderBeanName(String embedderBeanName)
    {
        GenericRunner.embedderBeanName = embedderBeanName;
    }

    @Override
    public Embedder configuredEmbedder()
    {
        return embedder;
    }

    @Override
    public Configuration configuration()
    {
        return embedder.configuration();
    }

    @Override
    public InjectableStepsFactory stepsFactory()
    {
        return embedder.stepsFactory();
    }

    @Override
    public List<String> storyPaths()
    {
        try
        {
            return batchedPathFinder.findPaths().values().stream().flatMap(List::stream).collect(Collectors.toList());
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void run()
    {
        Embedder embedder = configuredEmbedder();
        if (embedder instanceof BatchedEmbedder)
        {
            try
            {
                ((BatchedEmbedder) embedder).runStoriesAsPaths(batchedPathFinder.findPaths());
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
        }
        else
        {
            super.run();
        }
    }

    public static void main(String[] args)
    {
        Class<?> clazz = MethodHandles.lookup().lookupClass();
        Result result = new JUnitCore().run(clazz);
        int exitCode;
        if (result.getFailureCount() > 0)
        {
            Logger logger = LoggerFactory.getLogger(clazz);
            result.getFailures().forEach(f ->
            {
                logger.error("Failure: {}", f);
                logger.atError().addArgument(f::getTrace).log("{}");
            });
            exitCode = ERROR_EXIT_CODE;
        }
        else
        {
            exitCode = calculateExitCode(BeanFactory.getBean(IRunStatusProvider.class).getRunStatus());
        }
        System.exit(exitCode);
    }

    private static int calculateExitCode(Optional<Status> status)
    {
        if (status.isEmpty())
        {
            return RUN_FAILED_EXIT_CODE;
        }
        switch (status.get())
        {
            case PASSED:            return RUN_PASSED_EXIT_CODE;
            case KNOWN_ISSUES_ONLY: return RUN_KNOWN_ISSUES_EXIT_CODE;
            default:                return RUN_FAILED_EXIT_CODE;
        }
    }
}
