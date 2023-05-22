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

package org.vividus.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.StepPattern;
import org.jbehave.core.steps.NullStepMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.RunTestContext;

public class ReplacementMonitor extends NullStepMonitor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplacementMonitor.class);

    private final RunTestContext runTestContext;

    private final Map<String, Pattern> stepPatternsCache = new ConcurrentHashMap<>();

    public ReplacementMonitor(RunTestContext runTestContext)
    {
        this.runTestContext = runTestContext;
    }

    @Override
    public void stepMatchesPattern(String step, boolean matches, StepPattern pattern, Method method,
            Object stepsInstance)
    {
        if (matches)
        {
            String key = pattern.type() + " " + pattern.annotated();
            stepPatternsCache.putIfAbsent(key, Pattern.compile(pattern.resolved(), Pattern.DOTALL));
        }
    }

    @Override
    public void beforePerforming(String step, boolean dryRun, Method method)
    {
        getAnnotation(method).ifPresent(r -> {
            String rawDeprecatedStep = runTestContext.getRunningStory().getRunningSteps().getFirst();
            Matcher stepMatcher = getStepMatcher(rawDeprecatedStep);
            String[] stepParams = getParametersFromStep(stepMatcher);

            try (Formatter newStep = new Formatter())
            {
                newStep.format(r.replacementFormatPattern(), stepParams);

                LOGGER.atInfo()
                        .addArgument(rawDeprecatedStep)
                        .addArgument(r::versionToRemoveStep)
                        .addArgument(newStep::toString)
                        .log("The step: \"{}\" is deprecated and will be removed in VIVIDUS {}. Use step: \"{}\"");
            }
        });
    }

    private Optional<Replacement> getAnnotation(Method method)
    {
        return Optional.ofNullable(method).map(m -> method.getAnnotation(Replacement.class));
    }

    private Matcher getStepMatcher(String rawDeprecatedStep)
    {
        List<Matcher> matchers = new ArrayList<>();
        Keywords keywords = new Keywords();
        String stepWithoutStartingWord = keywords.stepWithoutStartingWord(rawDeprecatedStep);
        for (Pattern stepPattern : stepPatternsCache.values())
        {
            Matcher matcher = stepPattern.matcher(stepWithoutStartingWord);
            if (matcher.matches())
            {
                matchers.add(matcher);
            }
        }
        return matchers.stream().max(Comparator.comparing(Matcher::groupCount)).orElse(null);
    }

    private String[] getParametersFromStep(Matcher stepMatcher)
    {
        String[] parameters = new String[stepMatcher.groupCount()];
        for (int i = 0; i < parameters.length; i++)
        {
            parameters[i] = stepMatcher.group(i + 1);
        }
        return parameters;
    }
}
