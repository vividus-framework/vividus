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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.DynamicConfigurationManager;
import org.vividus.context.VariableContext;
import org.vividus.ssh.context.SshTestContext;
import org.vividus.ssh.exec.SshOutput;
import org.vividus.ssh.sftp.SftpCommand;
import org.vividus.ssh.sftp.SftpOutput;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class SshStepsTests
{
    private static final String DESTINATION_PATH = "/path";
    private static final String SERVER = "my-server";
    private static final SshConnectionParameters SSH_CONNECTION_PARAMETERS = new SshConnectionParameters();

    @Mock private DynamicConfigurationManager<SshConnectionParameters> sshConnectionParameters;
    @Mock private VariableContext variableContext;
    @Mock private Map<String, CommandExecutionManager<?>> commandExecutionManagers;
    @Mock private SshTestContext sshTestContext;
    @InjectMocks private SshSteps sshSteps;

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    @Test
    void shouldConfigureDynamicConnection()
    {
        var connectionParametersTable = new ExamplesTable("|username |host       |port|password|\n"
                        + "|admin    |10.10.10.10|22  |Pa$$w0rd|");
        var key = "new-connection";
        sshSteps.configureSshConnection(key, connectionParametersTable);
        var sshConnectionParametersArgumentCaptor = ArgumentCaptor.forClass(SshConnectionParameters.class);
        verify(sshConnectionParameters).addDynamicConfiguration(eq(key),
                sshConnectionParametersArgumentCaptor.capture());
        var parameters = sshConnectionParametersArgumentCaptor.getValue();
        assertAll(
                () -> assertEquals("admin", parameters.getUsername()),
                () -> assertEquals("10.10.10.10", parameters.getHost()),
                () -> assertEquals(22, parameters.getPort()),
                () -> assertEquals("Pa$$w0rd", parameters.getPassword())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "|any|\n|1|\n|2|" })
    void shouldFailToConfigureDynamicConnection(String tableAsString)
    {
        var connectionParametersTable = new ExamplesTable(tableAsString);
        var steps = new SshSteps(null, variableContext, commandExecutionManagers, sshTestContext);
        var key = "invalid-connection";
        var exception = assertThrows(IllegalArgumentException.class,
                () -> steps.configureSshConnection(key, connectionParametersTable));
        assertEquals("Exactly one row with SSH connection parameters is expected in ExamplesTable, but found "
                + connectionParametersTable.getRowCount(), exception.getMessage());
    }

    @Test
    void shouldExecuteSshCommands() throws CommandExecutionException
    {
        CommandExecutionManager<SshOutput> executionManager = mockGettingOfCommandExecutionManager(Protocol.SSH);
        var commands = new Commands("ssh-command");
        var output = new SshOutput();
        when(executionManager.run(SSH_CONNECTION_PARAMETERS, commands)).thenReturn(output);
        when(sshConnectionParameters.getConfiguration(SERVER)).thenReturn(SSH_CONNECTION_PARAMETERS);

        var steps = new SshSteps(sshConnectionParameters, variableContext, commandExecutionManagers, sshTestContext);
        var actual = steps.executeCommands(commands, SERVER, Protocol.SSH);

        assertEquals(output, actual);
        verify(sshTestContext).putSshOutput(output);
    }

    @Test
    void shouldExecuteSftpCommands() throws CommandExecutionException
    {
        testSftpExecution(commands -> Optional.of(sshSteps.executeCommands(commands, SERVER, Protocol.SFTP)),
                Optional.empty());
    }

    @Test
    void shouldSaveSftpResult() throws CommandExecutionException
    {
        var scopes = Set.of(VariableScope.SCENARIO);
        var variableName = "sftp-result";
        var result = "sftp-output";
        testSftpExecution(commands -> Optional.of(sshSteps.saveSftpResult(commands, SERVER, scopes, variableName)),
                Optional.of(result));
        verify(variableContext).putVariable(scopes, variableName, result);
    }

    @Test
    void shouldFailToSaveResultIfSftpCommandDoesNotProvideIt() throws CommandExecutionException
    {
        var scopes = Set.of(VariableScope.SCENARIO);
        String variableName = any();
        testSftpExecution(commands -> {
            var exception = assertThrows(IllegalArgumentException.class,
                    () -> sshSteps.saveSftpResult(commands, SERVER, scopes, variableName));
            assertEquals(exception.getMessage(), "The command '" + commands.getJoinedCommands()
                    + "' has not provided any result. Only following commands provide result: get, ls, pwd");
            return Optional.empty();
        }, Optional.empty());
        verifyNoInteractions(variableContext);
    }

    @Test
    void shouldCreateFileOverSftp() throws CommandExecutionException
    {
        var content = "content";
        testPutFile(SftpCommand.PUT, content, steps -> steps.createFileOverSftp(content, DESTINATION_PATH, SERVER));
    }

    @Test
    void testPutFileSftp() throws CommandExecutionException
    {
        var filePath = "/test.txt";
        testPutFile(SftpCommand.PUT_FROM_FILE, filePath,
            steps -> steps.copyFileOverSftp(filePath, DESTINATION_PATH, SERVER));
    }

    private void testPutFile(SftpCommand command, String parameter,
            FailableConsumer<SshSteps, CommandExecutionException> stepExecutor) throws CommandExecutionException
    {
        when(sshConnectionParameters.getConfiguration(SERVER)).thenReturn(SSH_CONNECTION_PARAMETERS);
        CommandExecutionManager<SftpOutput> executionManager = mockGettingOfCommandExecutionManager(Protocol.SFTP);
        when(executionManager.run(eq(SSH_CONNECTION_PARAMETERS), argThat(commands -> {
            var singleCommands = commands.getSingleCommands(null);
            if (singleCommands.size() == 1)
            {
                var singleCommand = singleCommands.get(0);
                return singleCommand.getCommand() == command
                        && List.of(parameter, DESTINATION_PATH).equals(singleCommand.getParameters());
            }
            return false;
        }))).thenReturn(new SftpOutput());

        stepExecutor.accept(sshSteps);

        verify(sshTestContext).putSshOutput(null);
    }

    private Commands testSftpExecution(
            FailableFunction<Commands, Optional<Object>, CommandExecutionException> commandExecutor,
            Optional<String> commandResult) throws CommandExecutionException
    {
        when(sshConnectionParameters.getConfiguration(SERVER)).thenReturn(SSH_CONNECTION_PARAMETERS);
        CommandExecutionManager<SftpOutput> executionManager = mockGettingOfCommandExecutionManager(Protocol.SFTP);
        var commands = new Commands("sftp-command");
        var output = new SftpOutput();
        output.setResult(commandResult);
        when(executionManager.run(SSH_CONNECTION_PARAMETERS, commands)).thenReturn(output);

        var actual = commandExecutor.apply(commands);

        actual.ifPresent(result -> assertEquals(output, result));
        verify(sshTestContext).putSshOutput(null);
        return commands;
    }

    @SuppressWarnings("unchecked")
    private <T> CommandExecutionManager<T> mockGettingOfCommandExecutionManager(Protocol protocol)
    {
        var commandExecutionManager = mock(CommandExecutionManager.class);
        when(commandExecutionManagers.get(protocol.toString())).thenReturn(commandExecutionManager);
        return commandExecutionManager;
    }
}
