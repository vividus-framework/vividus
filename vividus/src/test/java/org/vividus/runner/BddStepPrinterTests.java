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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCandidate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.SystemOutTests;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.*", "org.xml.*", "org.w3c.*", "com.sun.*"})
public class BddStepPrinterTests extends SystemOutTests
{
    @Test
    public void testPrintHelp() throws Exception
    {
        BddStepPrinter.main(new String[] {"-h"});
        assertOutput(List.of("usage: BddStepPrinter",
                                   " -f,--file <arg>   Name of file to save BDD steps",
                                   " -h,--help         Print this message"));
    }

    @Test
    @PrepareForTest({ Vividus.class, BeanFactory.class })
    public void testPrintToSystemOut() throws Exception
    {
        List<String> expectedOutput = mockStepCandidates();
        BddStepPrinter.main(new String[0]);
        assertOutput(expectedOutput);
    }

    @Test
    @PrepareForTest({ Vividus.class, BeanFactory.class, FileUtils.class })
    public void testPrintToFile() throws Exception
    {
        List<String> expectedOutput = mockStepCandidates();
        String filePath = "mocked" + File.separator + "file";
        mockStatic(FileUtils.class);
        BddStepPrinter.main(new String[] {"-f", filePath});
        Path file = Paths.get(filePath);
        assertOutput(List.of("File with BDD steps: " + file.toAbsolutePath()));
        PowerMockito.verifyStatic(FileUtils.class);
        FileUtils.writeLines(argThat(f -> filePath.equals(f.toString())), argThat(steps -> steps.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList())
                .equals(expectedOutput)));
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private List<String> mockStepCandidates() throws Exception
    {
        mockStatic(Vividus.class);
        PowerMockito.doNothing().when(Vividus.class, "init");
        mockStatic(BeanFactory.class);
        InjectableStepsFactory stepsFactory = mock(InjectableStepsFactory.class);
        when(BeanFactory.getBean(InjectableStepsFactory.class)).thenReturn(stepsFactory);
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

    private void assertOutput(List<String> lines) throws UnsupportedEncodingException
    {
        String lineSeparator = System.lineSeparator();
        String expectedOutput = lines.stream().collect(Collectors.joining(lineSeparator, "", lineSeparator));
        assertEquals(expectedOutput, getOutput());
    }

    @SuppressWarnings("unused")
    private void simpleMethod()
    {
        // used for testing purposes
    }

    @SuppressWarnings({"unused", "checkstyle:MissingDeprecated"})
    @Deprecated
    private void deprecatedMethod()
    {
        // used for testing purposes
    }
}
