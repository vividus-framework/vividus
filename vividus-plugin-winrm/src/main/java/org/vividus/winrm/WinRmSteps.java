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

package org.vividus.winrm;

import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.context.DynamicConfigurationManager;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

import io.cloudsoft.winrm4j.client.WinRmClientContext;
import io.cloudsoft.winrm4j.winrm.WinRmTool;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;

public class WinRmSteps
{
    private final DynamicConfigurationManager<WinRmConnectionParameters> winRmConnectionParameters;
    private final VariableContext variableContext;

    public WinRmSteps(DynamicConfigurationManager<WinRmConnectionParameters> winRmConnectionParameters,
            VariableContext variableContext)
    {
        this.winRmConnectionParameters = winRmConnectionParameters;
        this.variableContext = variableContext;
    }

    /**
     * Creates a new dynamic WinRM connection from the provided parameters, the connection is available only within the
     * story creating the connection.
     *
     * @param connectionKey        The key to assign to the creating WinRM connection. In case if the key conflicts
     *                             with a global connection key, the dynamic connection will take precedence within
     *                             the story.
     * @param connectionParameters ExamplesTable with WinRM connection parameters.
     */
    @When("I configure WinRM connection with key `$connectionKey` and parameters:$connectionParameters")
    public void configureWinRmConnection(String connectionKey, ExamplesTable connectionParameters)
    {
        int rowCount = connectionParameters.getRowCount();
        Validate.isTrue(rowCount == 1,
                "Exactly one row with WinRM connection parameters is expected in ExamplesTable, but found %d",
                rowCount);
        WinRmConnectionParameters winRmConnectionParametersToAdd = connectionParameters.getRowsAsParameters(true).get(0)
                .mapTo(WinRmConnectionParameters.class);
        winRmConnectionParameters.addDynamicConfiguration(connectionKey, winRmConnectionParametersToAdd);
    }

    /**
     * Executes a native Windows command. A new session is created on the destination host for each step invocation.
     *
     * @param command       The batch command limited to 8096 bytes. The maximum length of the command can be even less
     *                      depending on the <a href="https://support.microsoft.com/en-us/kb/830473">platform</a>.
     * @param connectionKey The WinRM connection key matching any of configured ones.
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
    @When("I execute batch command `$command` on server `$connectionKey` using WinRM and save result to $scopes "
            + "variable `$variableName`")
    public void executeBatchCommand(String command, String connectionKey, Set<VariableScope> scopes,
            String variableName)
    {
        executeCommand(connectionKey, winRmTool -> winRmTool.executeCommand(command), scopes, variableName);
    }

    /**
     * Executes a PowerShell command. A new session is created on the destination host for each step invocation.
     *
     * @param command       The PowerShell command.
     * @param connectionKey The WinRM connection key matching any of configured ones.
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
    @When("I execute PowerShell command `$command` on server `$connectionKey` using WinRM and save result to $scopes "
            + "variable `$variableName`")
    public void executePowerShellCommand(String command, String connectionKey, Set<VariableScope> scopes,
            String variableName)
    {
        executeCommand(connectionKey, winRmTool -> winRmTool.executePs(command), scopes, variableName);
    }

    private void executeCommand(String connectionKey, Function<WinRmTool, WinRmToolResponse> executor,
            Set<VariableScope> scopes, String variableName)
    {
        WinRmConnectionParameters connectionParameters = winRmConnectionParameters.getConfiguration(connectionKey);

        WinRmTool.Builder winRmToolBuilder = WinRmTool.Builder.builder(connectionParameters.getAddress(),
                connectionParameters.getUsername(), connectionParameters.getPassword());
        winRmToolBuilder.disableCertificateChecks(connectionParameters.isDisableCertificateChecks());
        ofNullable(connectionParameters.getAuthenticationScheme()).ifPresent(winRmToolBuilder::authenticationScheme);

        WinRmClientContext context = WinRmClientContext.newInstance();
        try
        {
            WinRmTool winRmTool = winRmToolBuilder.context(context).build();
            WinRmToolResponse response = executor.apply(winRmTool);
            variableContext.putVariable(scopes, variableName, Map.of(
                    "stdout", response.getStdOut(),
                    "stderr", response.getStdErr(),
                    "exit-status", response.getStatusCode()
            ));
        }
        finally
        {
            context.shutdown();
        }
    }
}
