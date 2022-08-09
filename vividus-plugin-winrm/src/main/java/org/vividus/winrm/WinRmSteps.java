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

import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.util.property.PropertyMappedCollection;
import org.vividus.variable.VariableScope;

import io.cloudsoft.winrm4j.client.WinRmClientContext;
import io.cloudsoft.winrm4j.winrm.WinRmTool;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;

public class WinRmSteps
{
    private final PropertyMappedCollection<ServerConfiguration> serverConfigurations;
    private final VariableContext variableContext;

    public WinRmSteps(PropertyMappedCollection<ServerConfiguration> serverConfigurations,
            VariableContext variableContext)
    {
        this.serverConfigurations = serverConfigurations;
        this.variableContext = variableContext;
    }

    /**
     * Executes a native Windows command. A new session is created on the destination host for each step invocation.
     *
     * @param command      The batch command limited to 8096 bytes. The maximum length of the command can be even less
     *                     depending on the <a href="https://support.microsoft.com/en-us/kb/830473">platform</a>.
     * @param server       The WinRM server key matching one configured in the properties.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                     scopes.<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The variable name to store the command execution results.
     */
    @When("I execute batch command `$command` on server `$server` using WinRM and save result to $scopes variable "
            + "`$variableName`")
    public void executeBatchCommand(String command, String server, Set<VariableScope> scopes, String variableName)
    {
        executeCommand(server, winRmTool -> winRmTool.executeCommand(command), scopes, variableName);
    }

    /**
     * Executes a PowerShell command. A new session is created on the destination host for each step invocation.
     *
     * @param command      The PowerShell command.
     * @param server       The WinRM server key matching one configured in the properties.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                     scopes.<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The variable name to store the command execution results.
     */
    @When("I execute PowerShell command `$command` on server `$server` using WinRM and save result to $scopes variable "
            + "`$variableName`")
    public void executePowerShellCommand(String command, String server, Set<VariableScope> scopes, String variableName)
    {
        executeCommand(server, winRmTool -> winRmTool.executePs(command), scopes, variableName);
    }

    private void executeCommand(String server, Function<WinRmTool, WinRmToolResponse> executor,
            Set<VariableScope> scopes, String variableName)
    {
        ServerConfiguration serverConfiguration = serverConfigurations.get(server,
                "WinRM server connection with key '%s' is not configured in properties", server);

        WinRmTool.Builder winRmToolBuilder = WinRmTool.Builder.builder(serverConfiguration.getAddress(),
                serverConfiguration.getUsername(), serverConfiguration.getPassword());
        winRmToolBuilder.disableCertificateChecks(serverConfiguration.isDisableCertificateChecks());
        ofNullable(serverConfiguration.getAuthenticationScheme()).ifPresent(winRmToolBuilder::authenticationScheme);

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
