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

package org.vividus.runner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCandidate;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;
import org.mockito.MockedStatic;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

class StepsPrinterTests
{
    private static final String THEN = "Then";

    @Test
    @StdIo
    void testPrintHelp(StdOut stdOut) throws IOException, ParseException
    {
        StepsPrinter.main(new String[] {"-h"});
        assertThat(stdOut.capturedLines(), arrayContaining(
                "usage: StepPrinter",
                " -f,--file <arg>   Name of file to save steps",
                " -h,--help         Print this message"
        ));
    }

    @Test
    @StdIo
    void testPrintToSystemOut(StdOut stdOut) throws IOException, ParseException, ReflectiveOperationException
    {
        try (var vividus = mockStatic(Vividus.class); var beanFactory = mockStatic(BeanFactory.class))
        {
            var expectedOutput = mockStepCandidates(beanFactory);
            StepsPrinter.main(new String[0]);
            assertThat(stdOut.capturedLines(), arrayContaining(expectedOutput.toArray(new String[0])));
            vividus.verify(Vividus::init);
        }
    }

    @Test
    @StdIo
    void testPrintToFile(StdOut stdOut) throws IOException, ParseException, ReflectiveOperationException
    {
        try (var vividus = mockStatic(Vividus.class);
                var beanFactory = mockStatic(BeanFactory.class);
                var fileUtils = mockStatic(FileUtils.class))
        {
            var expectedOutput = mockStepCandidates(beanFactory);
            var filePath = "mocked" + File.separator + "file";
            StepsPrinter.main(new String[] {"-f", filePath});
            var file = Paths.get(filePath);
            assertThat(stdOut.capturedLines(), arrayContaining("File with steps: " + file.toAbsolutePath()));
            vividus.verify(Vividus::init);
            fileUtils.verify(() -> FileUtils.writeLines(argThat(f -> filePath.equals(f.toString())),
                    argThat(steps -> steps.stream().map(Object::toString).collect(Collectors.toList())
                            .equals(expectedOutput))));
        }
    }

    private List<String> mockStepCandidates(MockedStatic<BeanFactory> beanFactory) throws ReflectiveOperationException
    {
        var stepsFactory = mock(InjectableStepsFactory.class);
        beanFactory.when(() -> BeanFactory.getBean(InjectableStepsFactory.class)).thenReturn(stepsFactory);
        var candidateSteps = mock(CandidateSteps.class);
        when(stepsFactory.createCandidateSteps()).thenReturn(List.of(candidateSteps));
        var stepCandidate1 = mockStepCandidate("Given", "initial state is '$status'", "simpleMethod");
        var stepCandidate2 = mockStepCandidate("When", "I do '$action'", "deprecatedMethod");
        var stepCandidate3 = mockStepCandidate(THEN, "I perform '$verification'", (Method) null);
        var stepCandidate4 = mockStepCandidate(THEN, "I run composite step with comment", (Method) null,
            "!-- DEPRECATED: The step \"Then I run composite step with comment\" "
                       + "is deprecated and will be removed in VIVIDUS 0.6.0");
        var stepCandidate5 = mockStepCandidate(THEN, "I run composite step with replacement comment", (Method) null,
                "!-- DEPRECATED: 0.6.0, Some replacement");
        when(candidateSteps.listCandidates())
                .thenReturn(List.of(stepCandidate1, stepCandidate2, stepCandidate3, stepCandidate4, stepCandidate5));
        return List.of("                          Given initial state is '$status'",
                "  DEPRECATED              When I do '$action'",
                "COMPOSITE IN STEPS FILE                         Then I perform '$verification'",
                "COMPOSITE IN STEPS FILE DEPRECATED              Then I run composite step with comment",
                "COMPOSITE IN STEPS FILE DEPRECATED              Then I run composite step with replacement comment");
    }

    private StepCandidate mockStepCandidate(String startingWord, String patternAsString, String methodName,
            String... composedSteps) throws ReflectiveOperationException
    {
        return mockStepCandidate(startingWord, patternAsString, getClass().getDeclaredMethod(methodName),
                composedSteps);
    }

    private static StepCandidate mockStepCandidate(String startingWord, String patternAsString, Method method,
            String... composedSteps)
    {
        var stepCandidate = mock(StepCandidate.class);
        when(stepCandidate.getStartingWord()).thenReturn(startingWord);
        when(stepCandidate.getPatternAsString()).thenReturn(patternAsString);
        when(stepCandidate.getMethod()).thenReturn(method);
        when(stepCandidate.composedSteps()).thenReturn(composedSteps);
        return stepCandidate;
    }

    @SuppressWarnings("unused")
    private void simpleMethod()
    {
        // used for testing purposes
    }

    @SuppressWarnings({"unused", "checkstyle:MissingDeprecated", "checkstyle:RequiredParameterForAnnotation"})
    @Deprecated
    private void deprecatedMethod()
    {
        // used for testing purposes
    }
}
