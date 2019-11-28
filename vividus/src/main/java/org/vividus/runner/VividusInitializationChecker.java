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

package org.vividus.runner;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

public final class VividusInitializationChecker
{
    private VividusInitializationChecker()
    {
    }

    @SuppressWarnings("checkstyle:IllegalCatchExtended")
    public static void main(String[] args) throws ParseException
    {
        Vividus.init();
        boolean isFailed = false;
        Logger logger = LoggerFactory.getLogger(VividusInitializationChecker.class);
        CommandLineParser parser = new DefaultParser();
        Option helpOption = new Option("h", "help", false, "print this message.");
        Option ignoreOption = new Option("i", "ignoreBeans", true,
                "comma separated list of beans that are not instantiated during check (e.g. bean1,bean2)");
        Options options = new Options();
        options.addOption(helpOption);
        options.addOption(ignoreOption);
        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.hasOption(helpOption.getOpt()))
        {
            new HelpFormatter().printHelp("VividusInitializationChecker", options);
            return;
        }
        String[] beanNames = BeanFactory.getBeanDefinitionNames();
        if (commandLine.hasOption(ignoreOption.getOpt()))
        {
            List<String> ignoreBeans = List.of(commandLine.getOptionValue(ignoreOption.getOpt()).split(","));
            beanNames = Arrays.stream(beanNames).filter(beanName -> !ignoreBeans.contains(beanName))
                    .toArray(String[]::new);
        }
        for (String beanName : beanNames)
        {
            try
            {
                BeanFactory.getBean(beanName);
            }
            catch (BeanIsAbstractException e)
            {
                // Nothing to do
            }
            catch (Exception e)
            {
                isFailed = true;
                logger.error(e.toString());
            }
        }
        if (isFailed)
        {
            throw new RuntimeException("Initialization of beans has been failed");
        }
    }
}
