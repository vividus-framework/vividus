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

package org.vividus.ssh;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.context.DynamicConfigurationManager;
import org.vividus.context.VariableContext;
import org.vividus.ssh.context.SshTestContext;
import org.vividus.ssh.exec.SshOutput;
import org.vividus.ssh.sftp.SftpCommand;
import org.vividus.ssh.sftp.SftpOutput;
import org.vividus.variable.VariableScope;

public class SshSteps
{
    private final DynamicConfigurationManager<SshConnectionParameters> sshConnectionParameters;
    private final VariableContext variableContext;
    private final Map<String, CommandExecutionManager<?>> commandExecutionManagers;
    private final SshTestContext sshTestContext;

    public SshSteps(DynamicConfigurationManager<SshConnectionParameters> sshConnectionParameters,
            VariableContext variableContext, Map<String, CommandExecutionManager<?>> commandExecutionManagers,
            SshTestContext sshTestContext)
    {
        this.sshConnectionParameters = sshConnectionParameters;
        this.variableContext = variableContext;
        this.commandExecutionManagers = commandExecutionManagers;
        this.sshTestContext = sshTestContext;
    }

    /**
     * Creates a new dynamic SSH connection from the provided parameters, the connection is available only within the
     * story creating the connection.
     *
     * @param connectionKey        The key to assign to the creating SSH connection. In case if the key conflicts with a
     *                             global connection key, the dynamic connection will take precedence within the story.
     * @param connectionParameters ExamplesTable with SSH connection parameters.
     */
    @When("I configure SSH connection with key `$connectionKey` and parameters:$connectionParameters")
    public void configureSshConnection(String connectionKey, ExamplesTable connectionParameters)
    {
        int rowCount = connectionParameters.getRowCount();
        Validate.isTrue(rowCount == 1,
                "Exactly one row with SSH connection parameters is expected in ExamplesTable, but found %d", rowCount);
        SshConnectionParameters sshConnectionParametersToAdd = connectionParameters.getRowsAsParameters(true).get(0)
                .mapTo(SshConnectionParameters.class);
        sshConnectionParameters.addDynamicConfiguration(connectionKey, sshConnectionParametersToAdd);
    }

    /**
     * Retrieves SSH connection parameters by key, opens SSH session and executes commands remotely.
     * <br>Usage examples:
     * <pre>
     * When I execute commands `cd /directory; pwd` on my-host over SSH
     * When I execute commands `cd /Users` on my-host over SFTP
     * </pre>.
     *
     * @param commands      Semicolon-separated commands to execute.
     * @param connectionKey The SSH connection key matching any of configured ones.
     * @param protocol      The protocol of execution: SSH or SFTP.
     * @return execution output
     * @throws CommandExecutionException if any error happens during commands execution
     */
    @When("I execute commands `$commands` on $connectionKey over $protocol")
    public Object executeCommands(Commands commands, String connectionKey, Protocol protocol)
            throws CommandExecutionException
    {
        SshConnectionParameters connectionParameters = sshConnectionParameters.getConfiguration(connectionKey);
        CommandExecutionManager<?> commandExecutionManager = commandExecutionManagers.get(protocol.toString());
        Object output = commandExecutionManager.run(connectionParameters, commands);
        sshTestContext.putSshOutput(Protocol.SSH == protocol ? (SshOutput) output : null);
        return output;
    }

    /**
     * Retrieves SSH connection parameters by key, opens SSH session, executes SFTP commands remotely and saves the
     * result of the commands to the variable.
     *
     * @param commands      Semicolon-separated commands to execute. It's allowed to combine any SFTP commands, but
     *                      at least one of them should return result. SFTP commands returning results are:
     *                      <ul>
     *                      <li><code>get remote-path</code></li>
     *                      <li><code>ls [path]</code></li>
     *                      <li><code>pwd</code></li>
     *                      </ul>
     * @param connectionKey The SSH connection key matching any of configured ones.
     * @param scopes        The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                      <i>Available scopes:</i>
     *                      <ul>
     *                      <li><b>STEP</b> - the variable will be available only within the step,
     *                      <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                      <li><b>STORY</b> - the variable will be available within the whole story,
     *                      <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                      </ul>.
     * @param variableName  The name of the variable to save the SFTP commands execution result.
     * @return SFTP command execution result
     * @throws CommandExecutionException if any error happens during commands execution
     */
    @When(value = "I execute commands `$commands` on $connectionKey over SFTP and save result to $scopes variable "
            + "`$variableName`", priority = 1)
    public SftpOutput saveSftpResult(Commands commands, String connectionKey, Set<VariableScope> scopes,
            String variableName) throws CommandExecutionException
    {
        SftpOutput output = (SftpOutput) executeCommands(commands, connectionKey, Protocol.SFTP);
        output.getResult().ifPresentOrElse(result -> variableContext.putVariable(scopes, variableName, result), () -> {
            throw new IllegalArgumentException("The command '" + commands.getJoinedCommands()
                    + "' has not provided any result. Only following commands provide result: get, ls, pwd");
        });
        return output;
    }

    /**
     * Retrieves SSH connection parameters by key, opens SSH session and creates file with the given content at the
     * provided remote destination.
     *
     * @param content       The content of the file to create.
     * @param destination   The remote file destination.
     * @param connectionKey The SSH connection key matching any of configured ones.
     * @throws CommandExecutionException if any error happens during file creation
     */
    @When("I create file with content `$content` at path `$destination` on $connectionKey over SFTP")
    public void createFileOverSftp(String content, String destination, String connectionKey)
            throws CommandExecutionException
    {
        executePutCommand(SftpCommand.PUT, List.of(content, destination), connectionKey);
    }

    /**
     * Retrieves SSH connection parameters by key, opens SSH session and puts the local file to the remote destination.
     *
     * @param filePath      The path of the file to copy.
     * @param destination   The remote file destination.
     * @param connectionKey The SSH connection key matching any of configured ones.
     * @throws CommandExecutionException if any error happens during file creation
     */
    @When("I copy local file located at `$filePath` to path `$destination` on $connectionKey over SFTP")
    public void copyFileOverSftp(String filePath, String destination, String connectionKey)
            throws CommandExecutionException
    {
        executePutCommand(SftpCommand.PUT_FROM_FILE, List.of(filePath, destination), connectionKey);
    }

    private void executePutCommand(SftpCommand command, List<String> parameters, String server)
            throws CommandExecutionException
    {
        executeCommands(new Commands(null)
        {
            @Override
            @SuppressWarnings("unchecked")
            public <T> List<SingleCommand<T>> getSingleCommands(Function<String, T> commandFactory)
            {
                return List.of((SingleCommand<T>) new SingleCommand<>(command, parameters));
            }
        }, server, Protocol.SFTP);
    }
}
