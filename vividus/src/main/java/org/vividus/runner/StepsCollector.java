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

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCandidate;
import org.vividus.configuration.BeanFactory;

public final class StepsCollector
{
    private static final String EMPTY = "";
    private static final Pattern DEPRECATION_COMMENT_PATTERN = Pattern.compile("^!--\\h+DEPRECATED:\\h+.*");

    private StepsCollector()
    {
    }

    public static Set<Step> getSteps()
    {
        InjectableStepsFactory stepFactory = BeanFactory.getBean(InjectableStepsFactory.class);
        return stepFactory.createCandidateSteps()
                .stream()
                .map(CandidateSteps::listCandidates)
                .flatMap(List::stream)
                .map(StepsCollector::createStepFrom)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static Step createStepFrom(StepCandidate candidate)
    {
        Step step = new Step(candidate.getStartingWord(), candidate.getPatternAsString());
        Method method = candidate.getMethod();
        // Method could be null for composite steps declared in *.steps files
        boolean compositeInStepsFile = method == null;
        step.setCompositeInStepsFile(compositeInStepsFile);
        step.setDeprecated(isDeprecatedCandidate(candidate));
        if (!compositeInStepsFile)
        {
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

    private static boolean isDeprecatedCandidate(StepCandidate candidate)
    {
        Method method = candidate.getMethod();
        return method != null ? method.isAnnotationPresent(Deprecated.class)
                : Stream.ofNullable(candidate.composedSteps()).flatMap(Stream::of)
                        .anyMatch(s -> DEPRECATION_COMMENT_PATTERN.matcher(s).matches());
    }

    public static final class Step implements Comparable<Step>
    {
        private final String startingWord;
        private final String pattern;
        private boolean deprecated;
        private boolean compositeInStepsFile;
        private String location;

        public Step(String startingWord, String pattern)
        {
            this.startingWord = startingWord;
            this.pattern = pattern;
        }

        public String getStartingWord()
        {
            return startingWord;
        }

        public String getPattern()
        {
            return pattern;
        }

        public boolean isDeprecated()
        {
            return deprecated;
        }

        public void setDeprecated(boolean deprecated)
        {
            this.deprecated = deprecated;
        }

        public boolean isCompositeInStepsFile()
        {
            return compositeInStepsFile;
        }

        public void setCompositeInStepsFile(boolean compositeInStepsFile)
        {
            this.compositeInStepsFile = compositeInStepsFile;
        }

        public String getLocation()
        {
            return location;
        }

        public void setLocation(String location)
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
