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
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCandidate;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

public final class BddStepPrinter
{
    private static final String SPACE = " ";

    private BddStepPrinter()
    {
        // Nothing to do
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

        String file = commandLine.getOptionValue(fileOption.getOpt());
        if (file == null)
        {
            steps.forEach(System.out::println);
        }
        else
        {
            Path path = Paths.get(file);
            FileUtils.writeLines(path.toFile(), steps);
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
        Step step = new Step();
        step.setDefinition(candidate.getStartingWord() + SPACE + candidate.getPatternAsString());
        Method method = candidate.getMethod();
        // Method could be null for composite steps declared in *.steps files
        boolean compositeInStepsFile = method == null;
        step.setCompositeInStepsFile(compositeInStepsFile);
        step.setDeprecated(!compositeInStepsFile && method.isAnnotationPresent(Deprecated.class));
        return step;
    }

    private static class Step implements Comparable<Step>
    {
        private static final String DEPRECATED        = "DEPRECATED              ";
        private static final String COMPOSITE         = "COMPOSITE IN STEPS FILE ";
        private static final String EMPTY_PLACEHOLDER = "                        ";

        private String definition;
        private boolean deprecated;
        private boolean compositeInStepsFile;

        private void setDefinition(String definition)
        {
            this.definition = definition;
        }

        private void setDeprecated(boolean deprecated)
        {
            this.deprecated = deprecated;
        }

        public void setCompositeInStepsFile(boolean compositeInStepsFile)
        {
            this.compositeInStepsFile = compositeInStepsFile;
        }

        @Override
        public String toString()
        {
            return (deprecated ? DEPRECATED : compositeInStepsFile ? COMPOSITE : EMPTY_PLACEHOLDER) + definition;
        }

        @Override
        public int compareTo(Step anotherStep)
        {
            return definition.compareTo(anotherStep.definition);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (obj.getClass() != this.getClass())
            {
                return false;
            }
            Step other = (Step) obj;
            return deprecated == other.deprecated && compositeInStepsFile == other.compositeInStepsFile
                    && Objects.equals(definition, other.definition);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(definition, deprecated, compositeInStepsFile);
        }
    }
}
