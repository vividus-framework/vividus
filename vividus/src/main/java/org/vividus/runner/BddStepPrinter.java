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

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCandidate;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

public final class BddStepPrinter
{
    private static final String DEPRECATED = "DEPRECATED";
    private static final String COMPOSITE = "COMPOSITE IN STEPS FILE";
    private static final String EMPTY = "";

    private BddStepPrinter()
    {
    }

    public static void main(String[] args) throws ParseException, IOException
    {
        CommandLineParser parser = new DefaultParser();
        Option helpOption = new Option("h", "help", false, "Print this message");
        Option fileOption = new Option("f", "file", true, "Name of file to save BDD steps");
        Options options = new Options();
        options.addOption(helpOption);
        options.addOption(fileOption);
        CommandLine commandLine = parser.parse(options, args);

        if (commandLine.hasOption(helpOption.getOpt()))
        {
            new HelpFormatter().printHelp("BddStepPrinter", options);
            return;
        }

        Vividus.init();
        Set<Step> steps = getSteps();

        int maxLocationLength = steps.stream().map(Step::getLocation).mapToInt(String::length).max().orElse(0);

        List<String> stepsLines = steps.stream().map(s -> String
                .format("%-" + (maxLocationLength + 1) + "s%-24s%s %s", s.getLocation(),
                        s.deprecated ? DEPRECATED : (s.compositeInStepsFile ? COMPOSITE : EMPTY), s.startingWord,
                        s.pattern))
                .collect(Collectors.toList());

        String file = commandLine.getOptionValue(fileOption.getOpt());
        if (file == null)
        {
            stepsLines.forEach(System.out::println);
        }
        else
        {
            Path path = Paths.get(file);
            FileUtils.writeLines(path.toFile(), stepsLines);
            System.out.println("File with BDD steps: " + path.toAbsolutePath());
        }
    }

    private static Set<Step> getSteps()
    {
        InjectableStepsFactory stepFactory = BeanFactory.getBean(InjectableStepsFactory.class);
        return stepFactory.createCandidateSteps()
                .stream()
                .map(CandidateSteps::listCandidates)
                .flatMap(List::stream)
                .map(BddStepPrinter::createStepFrom)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static Step createStepFrom(StepCandidate candidate)
    {
        Step step = new Step(candidate.getStartingWord(), candidate.getPatternAsString());
        Method method = candidate.getMethod();
        // Method could be null for composite steps declared in *.steps files
        boolean compositeInStepsFile = method == null;
        step.setCompositeInStepsFile(compositeInStepsFile);
        if (!compositeInStepsFile)
        {
            step.setDeprecated(method.isAnnotationPresent(Deprecated.class));
            String baseFileName = FilenameUtils.getBaseName(
                    method.getDeclaringClass().getProtectionDomain().getCodeSource().getLocation().toString());
            step.setLocation(baseFileName.replaceAll("-\\d+\\.\\d+\\.\\d+.*", EMPTY));
        }
        else
        {
            step.setLocation(EMPTY);
        }
        return step;
    }

    private static final class Step implements Comparable<Step>
    {
        private final String startingWord;
        private final String pattern;
        private boolean deprecated;
        private boolean compositeInStepsFile;
        private String location;

        private Step(String startingWord, String pattern)
        {
            this.startingWord = startingWord;
            this.pattern = pattern;
        }

        private String getStartingWord()
        {
            return startingWord;
        }

        private String getPattern()
        {
            return pattern;
        }

        private void setDeprecated(boolean deprecated)
        {
            this.deprecated = deprecated;
        }

        private boolean isCompositeInStepsFile()
        {
            return compositeInStepsFile;
        }

        private void setCompositeInStepsFile(boolean compositeInStepsFile)
        {
            this.compositeInStepsFile = compositeInStepsFile;
        }

        private String getLocation()
        {
            return location;
        }

        private void setLocation(String location)
        {
            this.location = location;
        }

        @Override
        public int compareTo(Step anotherStep)
        {
            return Comparator.comparing(Step::isCompositeInStepsFile)
                    .thenComparing(Step::getLocation)
                    .thenComparing(Step::getStartingWord)
                    .thenComparing(Step::getPattern)
                    .compare(this, anotherStep);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            Step step = (Step) o;
            return deprecated == step.deprecated && compositeInStepsFile == step.compositeInStepsFile && Objects.equals(
                    startingWord, step.startingWord) && Objects.equals(pattern, step.pattern) && Objects.equals(
                    location, step.location);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(startingWord, pattern, deprecated, compositeInStepsFile, location);
        }
    }
}
