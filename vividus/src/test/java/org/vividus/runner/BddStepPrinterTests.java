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

package org.vividus.runner;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCandidate;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.vividus.SystemStreamTests;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

class BddStepPrinterTests extends SystemStreamTests
{
    @Test
    void testPrintHelp() throws IOException, ParseException
    {
        BddStepPrinter.main(new String[] {"-h"});
        assertOutput(List.of("usage: BddStepPrinter",
                                   " -f,--file <arg>   Name of file to save BDD steps",
                                   " -h,--help         Print this message"));
    }

    @Test
    void testPrintToSystemOut() throws IOException, ParseException, ReflectiveOperationException
    {
        try (MockedStatic<Vividus> vividus = mockStatic(Vividus.class);
                MockedStatic<BeanFactory> beanFactory = mockStatic(BeanFactory.class))
        {
            List<String> expectedOutput = mockStepCandidates(beanFactory);
            BddStepPrinter.main(new String[0]);
            assertOutput(expectedOutput);
            vividus.verify(Vividus::init);
        }
    }

    @Test
    void testPrintToFile() throws IOException, ParseException, ReflectiveOperationException
    {
        try (MockedStatic<Vividus> vividus = mockStatic(Vividus.class);
                MockedStatic<BeanFactory> beanFactory = mockStatic(BeanFactory.class);
                MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class))
        {
            List<String> expectedOutput = mockStepCandidates(beanFactory);
            String filePath = "mocked" + File.separator + "file";
            BddStepPrinter.main(new String[] {"-f", filePath});
            Path file = Paths.get(filePath);
            assertOutput(List.of("File with BDD steps: " + file.toAbsolutePath()));
            vividus.verify(Vividus::init);
            fileUtils.verify(() -> FileUtils.writeLines(argThat(f -> filePath.equals(f.toString())),
                    argThat(steps -> steps.stream().map(Object::toString).collect(Collectors.toList())
                            .equals(expectedOutput))));
        }
    }

    private List<String> mockStepCandidates(MockedStatic<BeanFactory> beanFactory) throws ReflectiveOperationException
    {
        InjectableStepsFactory stepsFactory = mock(InjectableStepsFactory.class);
        beanFactory.when(() -> BeanFactory.getBean(InjectableStepsFactory.class)).thenReturn(stepsFactory);
        CandidateSteps candidateSteps = mock(CandidateSteps.class);
        when(stepsFactory.createCandidateSteps()).thenReturn(List.of(candidateSteps));
        StepCandidate stepCandidate1 = mockStepCandidate("Given", "initial state is '$status'", "simpleMethod");
        StepCandidate stepCandidate2 = mockStepCandidate("When", "I do '$action'", "deprecatedMethod");
        StepCandidate stepCandidate3 = mockStepCandidate("Then", "I perform '$verification'", (Method) null);
        when(candidateSteps.listCandidates()).thenReturn(List.of(stepCandidate1, stepCandidate2, stepCandidate3));
        return List.of("                         Given initial state is '$status'",
                " DEPRECATED              When I do '$action'",
                " COMPOSITE IN STEPS FILE Then I perform '$verification'");
    }

    private StepCandidate mockStepCandidate(String startingWord, String patternAsString, String methodName)
            throws ReflectiveOperationException
    {
        return mockStepCandidate(startingWord, patternAsString, getClass().getDeclaredMethod(methodName));
    }

    private static StepCandidate mockStepCandidate(String startingWord, String patternAsString, Method method)
    {
        StepCandidate stepCandidate = mock(StepCandidate.class);
        when(stepCandidate.getStartingWord()).thenReturn(startingWord);
        when(stepCandidate.getPatternAsString()).thenReturn(patternAsString);
        when(stepCandidate.getMethod()).thenReturn(method);
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
