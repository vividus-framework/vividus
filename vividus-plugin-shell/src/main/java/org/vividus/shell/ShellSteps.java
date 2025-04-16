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

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.tika.utils.FileProcessResult;
import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

public class ShellSteps
{
    private final VariableContext variableContext;
    private final ShellCommandExecutor shellCommandExecutor;

    public ShellSteps(VariableContext variableContext, ShellCommandExecutor shellCommandExecutor)
    {
        this.variableContext = variableContext;
        this.shellCommandExecutor = shellCommandExecutor;
    }

    /**
     * Executes a command using the shell.
     *
     * @param command       The command to execute.
     * @param scopes        The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                      scopes.<br>
     *                      <i>Available scopes:</i>
     *                      <ul>
     *                      <li><b>STEP</b> - the variable will be available only within the step,
     *                      <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                      <li><b>STORY</b> - the variable will be available within the whole story,
     *                      <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                      </ul>
     * @param variableName  The variable name to store the command execution results.
     */
    @When("I execute command `$command` and save result to $scopes variable `$variableName`")
    public void executeCommand(String command, Set<VariableScope> scopes, String variableName)
            throws IOException
    {
        saveResults(scopes, variableName, shellCommandExecutor.executeCommand(command));
    }

    /**
     * Executes a command using the shell defined under provided shell key.
     *
     * @param command       The command to execute.
     * @param shellKey      The key of the shell to use for command execution.
     * @param scopes        The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                      scopes.<br>
     *                      <i>Available scopes:</i>
     *                      <ul>
     *                      <li><b>STEP</b> - the variable will be available only within the step,
     *                      <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                      <li><b>STORY</b> - the variable will be available within the whole story,
     *                      <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                      </ul>
     * @param variableName  The variable name to store the command execution results.
     */
    @When("I execute command `$command` using $shellName and save result to $scopes variable `$variableName`")
    public void executeCommand(String command, String shellKey, Set<VariableScope> scopes, String variableName)
            throws IOException
    {
        saveResults(scopes, variableName, shellCommandExecutor.executeCommand(shellKey, command));
    }

    private void saveResults(Set<VariableScope> scopes, String variableName, FileProcessResult fileProcessResult)
    {
        variableContext.putVariable(scopes, variableName, Map.of(
                "stdout", fileProcessResult.getStdout(),
                "stderr", fileProcessResult.getStderr(),
                "exit-code", fileProcessResult.getExitValue()
        ));
    }
}
