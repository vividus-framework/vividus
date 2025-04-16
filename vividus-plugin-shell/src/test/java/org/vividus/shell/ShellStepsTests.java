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

package org.vividus.shell;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.tika.utils.FileProcessResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class ShellStepsTests
{
    private static final String COMMAND = "java -version";
    private static final String VARIABLE = "variableName";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.STEP);
    private static final String BASH = "bash";
    private static final String OUTPUT = "output";
    private static final String ERROR = "error";

    @Mock private FileProcessResult result;
    @Mock private VariableContext variableContext;
    @Mock private ShellCommandExecutor commandExecutor;
    @InjectMocks private ShellSteps shellSteps;

    @BeforeEach
    void beforeEach()
    {
        when(result.getExitValue()).thenReturn(1);
        when(result.getStderr()).thenReturn(ERROR);
        when(result.getStdout()).thenReturn(OUTPUT);
    }

    @Test
    void shouldExecuteCommand() throws IOException
    {
        when(commandExecutor.executeCommand(COMMAND)).thenReturn(result);
        shellSteps.executeCommand(COMMAND, SCOPES, VARIABLE);
        verifyVariable();
    }

    @Test
    void shouldExecuteCommandWithShell() throws IOException
    {
        when(commandExecutor.executeCommand(BASH, COMMAND)).thenReturn(result);
        shellSteps.executeCommand(COMMAND, BASH, SCOPES, VARIABLE);
        verifyVariable();
    }

    private void verifyVariable()
    {
        verify(variableContext).putVariable(SCOPES, VARIABLE,
                Map.of("stdout", OUTPUT, "stderr", ERROR, "exit-code", 1));
    }
}
