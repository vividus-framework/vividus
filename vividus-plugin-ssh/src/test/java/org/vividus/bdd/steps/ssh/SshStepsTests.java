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

package org.vividus.bdd.steps.ssh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.function.FailableFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class SshStepsTests
{
    private static final String DESTINATION_PATH = "/path";
    private static final String SERVER = "my-server";
    private static final ServerConfiguration SERVER_CONFIGURATION = new ServerConfiguration();

    @Mock
    private IBddVariableContext bddVariableContext;

    @Mock
    private Map<String, CommandExecutionManager<?>> commandExecutionManagers;

    @Mock
    private SshTestContext sshTestContext;

    @InjectMocks
    private SshSteps sshSteps;

    @BeforeEach
    void beforeEach()
    {
        sshSteps.setServerConfigurations(new PropertyMappedCollection<>(Map.of(SERVER, SERVER_CONFIGURATION)));
    }

    @Test
    void shouldExecuteSshCommands() throws CommandExecutionException
    {
        CommandExecutionManager<SshOutput> executionManager = mockGettingOfCommandExecutionManager(Protocol.SSH);
        Commands commands = new Commands("ssh-command");
        SshOutput output = new SshOutput();
        when(executionManager.run(SERVER_CONFIGURATION, commands)).thenReturn(output);
        Object actual = sshSteps.executeCommands(commands, SERVER, Protocol.SSH);
        assertEquals(output, actual);
        verify(sshTestContext).putSshOutput(output);
    }

    @Test
    void shouldExecuteSftpCommands() throws CommandExecutionException
    {
        testSftpExecution(commands -> sshSteps.executeCommands(commands, SERVER, Protocol.SFTP));
    }

    @Test
    void shouldSaveSftpResult() throws CommandExecutionException
    {
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        String variableName = "sftp-result";
        SftpOutput sftpOutput = testSftpExecution(
            commands -> sshSteps.saveSftpResult(commands, SERVER, scopes, variableName));
        verify(bddVariableContext).putVariable(scopes, variableName, sftpOutput.getResult());
    }

    @Test
    void shouldCreateFileOverSftp() throws CommandExecutionException
    {
        String content = "content";
        testPutFile(SftpCommand.PUT, content, () -> sshSteps.createFileOverSftp(content, DESTINATION_PATH, SERVER));
    }

    @Test
    void testPutFileSftp() throws CommandExecutionException
    {
        String filePath = "/test.txt";
        testPutFile(SftpCommand.PUT_FROM_FILE, filePath,
            () -> sshSteps.copyFileOverSftp(filePath, DESTINATION_PATH, SERVER));
    }

    private void testPutFile(SftpCommand command, String parameter, StepRunner stepExecutor)
            throws CommandExecutionException
    {
        CommandExecutionManager<SftpOutput> executionManager = mockGettingOfCommandExecutionManager(Protocol.SFTP);
        when(executionManager.run(eq(SERVER_CONFIGURATION), argThat(commands -> {
            List<SingleCommand<Object>> singleCommands = commands.getSingleCommands(null);
            if (singleCommands.size() == 1)
            {
                SingleCommand<Object> singleCommand = singleCommands.get(0);
                return singleCommand.getCommand() == command
                        && List.of(parameter, DESTINATION_PATH).equals(singleCommand.getParameters());
            }
            return false;
        }))).thenReturn(new SftpOutput());
        stepExecutor.run();
        verify(sshTestContext).putSshOutput(null);
    }

    private SftpOutput testSftpExecution(FailableFunction<Commands, Object, CommandExecutionException> commandExecutor)
            throws CommandExecutionException
    {
        CommandExecutionManager<SftpOutput> executionManager = mockGettingOfCommandExecutionManager(Protocol.SFTP);
        Commands commands = new Commands("sftp-command");
        SftpOutput output = new SftpOutput();
        output.setResult("sftp-output");
        when(executionManager.run(SERVER_CONFIGURATION, commands)).thenReturn(output);
        Object actual = commandExecutor.apply(commands);
        assertEquals(output, actual);
        verify(sshTestContext).putSshOutput(null);
        return output;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> CommandExecutionManager<T> mockGettingOfCommandExecutionManager(Protocol protocol)
    {
        CommandExecutionManager commandExecutionManager = mock(CommandExecutionManager.class);
        when(commandExecutionManagers.get(protocol.toString())).thenReturn(commandExecutionManager);
        return commandExecutionManager;
    }

    @FunctionalInterface
    private interface StepRunner
    {
        void run() throws CommandExecutionException;
    }
}
