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

package org.vividus.xray.converter;

import static java.lang.System.lineSeparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.vividus.bdd.model.jbehave.Step;
import org.vividus.xray.exception.SyntaxException;
import org.vividus.xray.model.ManualTestStep;

public final class ManualStepConverter
{
    private static final int DOTALL_MULTILINE_MODE = Pattern.DOTALL | Pattern.MULTILINE;

    private static final String STEP_SIGN = "Step:";
    private static final String DATA_SIGN = "Data:";
    private static final String RESULT_SIGN = "Result:";

    private static final String FORMAT_MESSAGE =  "Manual scenario rules:" + lineSeparator()
        + "1. Manual scenario entries must be prepended with '!-- ' sequence" + lineSeparator()
        + "2. '" + STEP_SIGN + "' designator is required to be the first one in the manual step block" + lineSeparator()
        + "3. '" + DATA_SIGN + "' and '" + RESULT_SIGN + "' designators are optional"  + lineSeparator()
        + "4. '" + STEP_SIGN + "' designator is mandatory" + lineSeparator();

    private static final Pattern STEP_BLOCK_PATTERN = Pattern.compile(String.format("%1$s.*?(?=^%1$s|\\Z)", STEP_SIGN),
            DOTALL_MULTILINE_MODE);

    private static final String REQUIRED_PART_FORMAT = "\\A%s(.*?)(?=^%s|^%s|\\Z)";
    private static final String OPTIONAL_PARTS_FORMAT = "^%s(.*?)(?=^%s|^%s|\\Z)";

    private static final Pattern STEP_PATTERN = Pattern
            .compile(String.format(REQUIRED_PART_FORMAT, STEP_SIGN, DATA_SIGN, RESULT_SIGN), DOTALL_MULTILINE_MODE);
    private static final Pattern DATA_PATTERN = Pattern
            .compile(String.format(OPTIONAL_PARTS_FORMAT, DATA_SIGN, DATA_SIGN, RESULT_SIGN), DOTALL_MULTILINE_MODE);
    private static final Pattern RESULT_PATTERN = Pattern
            .compile(String.format(OPTIONAL_PARTS_FORMAT, RESULT_SIGN, RESULT_SIGN, DATA_SIGN), DOTALL_MULTILINE_MODE);

    private ManualStepConverter()
    {
    }

    public static List<ManualTestStep> convert(String storyTitle, String scenarioTitle, List<Step> steps)
            throws SyntaxException
    {
        boolean checkPrefix = steps.stream()
                                   .map(Step::getOutcome)
                                   .allMatch("comment"::equals);

        if (!checkPrefix)
        {
            throw new SyntaxException(getErrorMessage(storyTitle, scenarioTitle));
        }

        String manualScenario = steps.stream().map(Step::getValue)
                .map(v -> RegExUtils.replaceAll(v, "!--\\s*-?\\s*", StringUtils.EMPTY))
                .collect(Collectors.joining(lineSeparator()));

        if (!manualScenario.startsWith(STEP_SIGN))
        {
            throw new SyntaxException(getErrorMessage(storyTitle, scenarioTitle));
        }

        List<ManualTestStep> manualSteps = new ArrayList<>();
        Matcher stepMatcher = STEP_BLOCK_PATTERN.matcher(manualScenario);
        while (stepMatcher.find())
        {
            String stepBlock = stepMatcher.group();
            String step = extractFirstByPattern(STEP_PATTERN, stepBlock, STEP_SIGN);
            ManualTestStep manualTestStep = new ManualTestStep(step);
            extractFirstByPattern(DATA_PATTERN, stepBlock, DATA_SIGN, manualTestStep::setData);
            extractFirstByPattern(RESULT_PATTERN, stepBlock, RESULT_SIGN, manualTestStep::setExpectedResult);
            manualSteps.add(manualTestStep);
        }
        return manualSteps;
    }

    private static String extractFirstByPattern(Pattern pattern, String data, String target)
            throws SyntaxException
    {
        Matcher matcher = pattern.matcher(data);
        if (matcher.find())
        {
            String matched = matcher.group(1);
            if (matcher.find())
            {
                throw new SyntaxException(
                        String.format("Only one %s is expected to be present in the data", target));
            }
            return matched.strip();
        }
        return null;
    }

    private static void extractFirstByPattern(Pattern pattern, String data, String target,
            Consumer<String> valueConsumer) throws SyntaxException
    {
        Optional.ofNullable(extractFirstByPattern(pattern, data, target)).ifPresent(valueConsumer);
    }

    private static String getErrorMessage(String storyTitle, String scenarioTitle)
    {
        return "Error:" + lineSeparator() + "Story: " + storyTitle + lineSeparator() + "Scenario: " + scenarioTitle
                + lineSeparator() + FORMAT_MESSAGE;
    }
}
