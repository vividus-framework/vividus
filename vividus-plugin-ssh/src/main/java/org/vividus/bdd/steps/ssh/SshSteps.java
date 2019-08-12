/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.steps.ssh;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;

import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.context.SshTestContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.ssh.CommandExecutionException;
import org.vividus.ssh.CommandExecutionManager;
import org.vividus.ssh.Commands;
import org.vividus.ssh.Protocol;
import org.vividus.ssh.ServerConfiguration;
import org.vividus.ssh.SingleCommand;
import org.vividus.ssh.exec.SshOutput;
import org.vividus.ssh.sftp.SftpCommand;
import org.vividus.ssh.sftp.SftpOutput;

public class SshSteps
{
    @Inject private IBddVariableContext bddVariableContext;
    @Inject private Map<String, CommandExecutionManager<?>> commandExecutionManagers;
    @Inject private SshTestContext sshTestContext;
    private Map<String, ServerConfiguration> serverConfigurations;

    /**
     * Step retrieves server configuration by key, opens SSH session and executes commands remotely.
     * <br>Usage example:
     * <code>
     * <br>When I execute commands `cd /directory; pwd` on my-host over SSH
     * </code>
     *
     * @param commands Semicolon-separated commands to execute
     * @param server   Server key matching any of configured ones
     * @param protocol Protocol of execution: SSH or SFTP
     * @return execution output
     * @throws CommandExecutionException if any error happens during commands execution
     */
    @When("I execute commands `$commands` on $server over $protocol")
    public Object executeCommands(Commands commands, String server, Protocol protocol) throws CommandExecutionException
    {
        ServerConfiguration serverConfig = serverConfigurations.get(server);
        CommandExecutionManager<?> commandExecutionManager = commandExecutionManagers.get(protocol.toString());
        Object output = commandExecutionManager.run(serverConfig, commands);
        sshTestContext.putSshOutput(Protocol.SSH == protocol ? (SshOutput) output : null);
        return output;
    }

    /**
     * Step retrieves server configuration by key, opens SFTP session and executes commands remotely
     * and saves the result of the commands to the <b>variable</b>. Step performs validations:
     * <ul>
     * <li>no error is happened during execution
     * </ul>
     *
     * @param commands     Semicolon-separated commands to execute
     * @param server       Server key matching any of configured ones
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName A name of variable to save the SFTP commands execution result
     * @throws CommandExecutionException if any error happens during commands execution
     * @return SFTP command execution result
     */
    @When(value = "I execute commands `$commands` on $server over SFTP and save result to $scopes variable "
            + "`$variableName`", priority = 1)
    public SftpOutput saveSftpResult(Commands commands, String server, Set<VariableScope> scopes, String variableName)
            throws CommandExecutionException
    {
        SftpOutput output = (SftpOutput) executeCommands(commands, server, Protocol.SFTP);
        bddVariableContext.putVariable(scopes, variableName, output.getResult());
        return output;
    }

    /**
     * Step retrieves server configuration by key, opens SFTP session and creates file with given content at the given
     * remote destination
     *
     * @param content     Content of file to create
     * @param destination Remote file destination
     * @param server      Server key matching any of configured ones
     * @throws CommandExecutionException if any error happens during file creation
     */
    @When("I create file with content `$content` at path `$destination` on $server over SFTP")
    public void createFileOverSftp(String content, String destination, String server) throws CommandExecutionException
    {
        executeCommands(new Commands(null)
        {
            @Override
            @SuppressWarnings("unchecked")
            public <T> List<SingleCommand<T>> getSingleCommands(Function<String, T> commandFactory)
            {
                return List.of((SingleCommand<T>) new SingleCommand<>(SftpCommand.PUT, List.of(content, destination)));
            }
        }, server, Protocol.SFTP);
    }

    public void setServerConfigurations(Map<String, ServerConfiguration> serverConfigurations)
    {
        this.serverConfigurations = serverConfigurations;
    }
}
