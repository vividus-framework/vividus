/*
 * Copyright 2019-2025 the original author or authors.
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.io.FileUtils;
import org.vividus.configuration.Vividus;
import org.vividus.runner.StepsCollector.Step;

public final class StepsPrinter
{
    private static final String DEPRECATED = "DEPRECATED";
    private static final String COMPOSITE = "COMPOSITE IN STEPS FILE";

    private StepsPrinter()
    {
    }

    public static void main(String[] args) throws ParseException, IOException
    {
        CommandLineParser parser = new DefaultParser();
        Option helpOption = new Option("h", "help", false, "Print this message");
        Option fileOption = new Option("f", "file", true, "Name of file to save steps");
        Options options = new Options();
        options.addOption(helpOption);
        options.addOption(fileOption);
        CommandLine commandLine = parser.parse(options, args);

        if (commandLine.hasOption(helpOption.getOpt()))
        {
            HelpFormatter.builder().get().printHelp("StepPrinter", null, options, null, false);
            return;
        }

        Vividus.init();
        Set<Step> steps = StepsCollector.getSteps();

        int maxLocationLength = steps.stream().map(Step::getLocation).mapToInt(String::length).max().orElse(0);

        List<String> stepsLines = steps.stream()
                .map(s -> String.format("%-" + (maxLocationLength + 1) + "s %-24s%s %s",
                        s.isCompositeInStepsFile() ? COMPOSITE : s.getLocation(),
                        s.isDeprecated() ? DEPRECATED : "",
                        s.getStartingWord(), s.getPattern()))
                .toList();

        String file = commandLine.getOptionValue(fileOption.getOpt());
        if (file == null)
        {
            stepsLines.forEach(System.out::println);
        }
        else
        {
            Path path = Paths.get(file);
            FileUtils.writeLines(path.toFile(), stepsLines);
            System.out.println("File with steps: " + path.toAbsolutePath());
        }
    }
}
