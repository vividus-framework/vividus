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

package org.vividus.replacement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.vividus.context.RunTestContext;

public class DeprecatedStepNotificationFactory
{
    private final RunTestContext runTestContext;
    private final Configuration configuration;
    private final StepPatternsRegistry stepPatternsRegistry;

    public DeprecatedStepNotificationFactory(RunTestContext runTestContext, Configuration configuration,
            StepPatternsRegistry stepPatternsRegistry)
    {
        this.runTestContext = runTestContext;
        this.configuration = configuration;
        this.stepPatternsRegistry = stepPatternsRegistry;
    }

    protected String createDeprecatedStepNotification(String versionToRemove, String replacementFormatPattern)
    {
        String runningDeprecatedStep = runTestContext.getRunningStory().getRunningSteps().getFirst();
        Matcher stepMatcher = getStepMatcher(runningDeprecatedStep);
        String[] stepParams = getParametersFromStep(stepMatcher);

        String notification;
        try (Formatter newStep = new Formatter())
        {
            newStep.format(replacementFormatPattern, (Object[]) stepParams);
            notification = String.format(
                    "The step: \"%s\" is deprecated and will be removed in VIVIDUS %s. Use step: \"%s\"",
                    runningDeprecatedStep, versionToRemove, newStep);
        }
        return notification;
    }

    private Matcher getStepMatcher(String rawDeprecatedStep)
    {
        List<Matcher> matchers = new ArrayList<>();
        Keywords keywords = configuration.keywords();
        String stepWithoutStartingWord = keywords.stepWithoutStartingWord(rawDeprecatedStep);
        Collection<Pattern> stepPatterns = stepPatternsRegistry.getRegisteredStepPatterns().values();
        for (Pattern stepPattern : stepPatterns)
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
