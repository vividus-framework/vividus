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

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.tika.utils.FileProcessResult;
import org.apache.tika.utils.ProcessUtils;
import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.shell.model.Shell;
import org.vividus.util.property.PropertyMappedCollection;
import org.vividus.variable.VariableScope;

public class ShellSteps
{
    private final VariableContext variableContext;
    private final PropertyMappedCollection<Shell> shells;
    private final String shell;

    private Duration processFinishWaitTimeout;

    public ShellSteps(PropertyMappedCollection<Shell> shells, Optional<String> shell, VariableContext variableContext)
    {
        this.variableContext = variableContext;
        this.shells = shells;
        String osName = System.getProperty("os.name");
        this.shell = shell.orElseGet(() -> {
            if (osName.startsWith("Mac"))
            {
                return "zsh";
            }
            if (osName.startsWith("Windows"))
            {
                return "powershell";
            }
            return "bash";
        });
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
        executeCommand(command, shell, scopes, variableName);
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
        Shell shellToUse = shells.get(shellKey, "Unable to find the shell with key `%s`. Configured shells: %s",
                shellKey, shells.getData().keySet());
        ProcessBuilder processBuilder = new ProcessBuilder().command(shellToUse.getExecutable(),
            shellToUse.getOption(), command);
        FileProcessResult fileProcessResult = ProcessUtils.execute(processBuilder,
                processFinishWaitTimeout.toMillis(), Integer.MAX_VALUE, Integer.MAX_VALUE);
        if (fileProcessResult.isTimeout())
        {
            throw new IllegalStateException(
                    String.format("The command `%s` execution is not finished in `%s`.%nError: %s,%nOutput: %s",
                            command, processFinishWaitTimeout, fileProcessResult.getStderr(),
                            fileProcessResult.getStdout()));
        }

        variableContext.putVariable(scopes, variableName, Map.of(
                "stdout", fileProcessResult.getStdout(),
                "stderr", fileProcessResult.getStderr(),
                "exit-code", fileProcessResult.getExitValue()
        ));
    }

    public void setProcessFinishWaitTimeout(Duration processFinishWaitTimeout)
    {
        this.processFinishWaitTimeout = processFinishWaitTimeout;
    }
}
