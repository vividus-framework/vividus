/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.shell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.function.FailableConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.shell.model.Shell;
import org.vividus.util.Sleeper;
import org.vividus.util.property.PropertyMappedCollection;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class ShellStepsTests
{
    private static final String COMMAND = "java -version";
    private static final String VARIABLE = "variableName";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.STEP);
    private static final String OS_NAME = "os.name";
    private static final String BASH = "bash";
    private static final String C = "-c";
    private static final String SHELL_SEARCH_ERROR_MESSAGE =
        "Unable to find the shell with key `%s`. Configured shells: %s";
    private static final String OUTPUT = "output";
    private static final String ERROR = "error";

    @Mock private VariableContext variableContext;
    @Mock private PropertyMappedCollection<Shell> shells;
    @Mock private Process process;

    @Test
    @SetSystemProperty(key = OS_NAME, value = "Windows")
    void shouldRunCommandUsingPowershellForWindows() throws Exception
    {
        testCommandRunUsing("powershell", "-Command");
    }

    @Test
    @SetSystemProperty(key = OS_NAME, value = "Mac OS")
    void shouldRunCommandUsingZshForIos() throws Exception
    {
        testCommandRunUsing("zsh", C);
    }

    @Test
    @SetSystemProperty(key = OS_NAME, value = "Linux")
    void shouldRunCommandUsingBashForLinux() throws Exception
    {
        testCommandRunUsing(BASH, C);
    }

    @Test
    @SetSystemProperty(key = OS_NAME, value = "LINUX")
    void shouldThrowAnExceptionWhenProcessExceedsTimeout() throws Exception
    {
        ShellSteps shellSteps = new ShellSteps(shells, Optional.empty(), variableContext);
        shellSteps.setProcessFinishWaitTimeout(Duration.ofMillis(1));
        when(shells.getData()).thenReturn(Map.of());
        try (MockedConstruction<ProcessBuilder> constructedBuilder = Mockito.mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    when(mock.command(BASH, C, COMMAND)).thenReturn(mock);
                    when(mock.inheritIO()).thenReturn(mock);
                    when(mock.start()).thenReturn(process);
                }))
        {
            Shell shellToUse = new Shell();
            shellToUse.setExecutable(BASH);
            shellToUse.setOption(C);
            when(shells.get(BASH, SHELL_SEARCH_ERROR_MESSAGE, BASH, Set.of())).thenReturn(shellToUse);
            when(process.waitFor(eq(1L), argThat(tu -> {
                Sleeper.sleep(Duration.ofSeconds(1));
                return TimeUnit.MILLISECONDS == tu;
            }))).thenReturn(false);
            when(process.getInputStream()).thenReturn(IOUtils.toInputStream(OUTPUT, StandardCharsets.UTF_8));
            var errorStream = mock(InputStream.class);
            when(errorStream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException());
            when(process.getErrorStream()).thenReturn(errorStream);
            var exception = assertThrows(IllegalStateException.class,
                () -> shellSteps.executeCommand(COMMAND, SCOPES, VARIABLE));
            assertEquals(String.format(
                "The command `java -version` execution is not finished in `PT0.001S`.%n" + "Error: ,%nOutput: output"),
                    exception.getMessage());
            assertEquals(1, constructedBuilder.constructed().size());
            verifyNoInteractions(variableContext);
        }
    }

    @Test
    void shouldRunCommandUsingProvidedShell() throws Exception
    {
        var bash = "BASH";
        testCommandRunUsing(bash, C, steps -> steps.executeCommand(COMMAND, bash, SCOPES, VARIABLE));
    }

    private void testCommandRunUsing(String shellName, String option)
            throws Exception
    {
        testCommandRunUsing(shellName, option, steps -> steps.executeCommand(COMMAND, SCOPES, VARIABLE));
    }

    private void testCommandRunUsing(String shellName, String option, FailableConsumer<ShellSteps, Exception> test)
        throws Exception
    {
        ShellSteps shellSteps = new ShellSteps(shells, Optional.empty(), variableContext);
        shellSteps.setProcessFinishWaitTimeout(Duration.ofMillis(1));
        when(shells.getData()).thenReturn(Map.of());
        try (MockedConstruction<ProcessBuilder> constructedBuilder = Mockito.mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    when(mock.command(shellName, option, COMMAND)).thenReturn(mock);
                    when(mock.inheritIO()).thenReturn(mock);
                    when(mock.start()).thenReturn(process);
                }))
        {
            Shell shellToUse = new Shell();
            shellToUse.setExecutable(shellName);
            shellToUse.setOption(option);
            when(shells.get(shellName, SHELL_SEARCH_ERROR_MESSAGE, shellName,
                    Set.of())).thenReturn(shellToUse);
            when(process.waitFor(eq(1L), argThat(tu -> {
                Sleeper.sleep(Duration.ofSeconds(1));
                return TimeUnit.MILLISECONDS == tu;
            }))).thenReturn(true);
            when(process.getInputStream()).thenReturn(IOUtils.toInputStream(OUTPUT, StandardCharsets.UTF_8));
            when(process.getErrorStream()).thenReturn(IOUtils.toInputStream(ERROR, StandardCharsets.UTF_8));
            when(process.exitValue()).thenReturn(1);
            test.accept(shellSteps);
            assertEquals(1, constructedBuilder.constructed().size());
            verify(variableContext).putVariable(SCOPES, VARIABLE, Map.of("stdout", OUTPUT,
                    "stderr", ERROR,
                    "exit-code", 1));
        }
    }
}
