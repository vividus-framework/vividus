/*
 * Copyright 2019-2026 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.metricshub.winrm.AuthScheme;
import org.metricshub.winrm.CommandRequest;
import org.metricshub.winrm.CommandResult;
import org.metricshub.winrm.WinRMClient;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.DynamicConfigurationManager;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class WinRmStepsTests
{
    private static final String SERVER = "win10";
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String HOSTNAME = "10.240.1.1";
    private static final String HTTPS_ENDPOINT_SUFFIX = ":5986/wsman";
    private static final String HTTPS_ADDRESS_PREFIX = "https://" + HOSTNAME;
    private static final String HTTPS_ADDRESS = HTTPS_ADDRESS_PREFIX + HTTPS_ENDPOINT_SUFFIX;
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String VARIABLE_NAME = "result";
    private static final AuthScheme BASIC_AUTH_SCHEME = AuthScheme.BASIC;

    private static final String ECHO_COMMAND = "echo hello cmd";
    private static final String ECHO_COMMAND_OUTPUT = "hello cmd";

    @Mock private DynamicConfigurationManager<WinRmConnectionParameters> winRmConnectionParameters;
    @Mock private VariableContext variableContext;
    @InjectMocks private WinRmSteps steps;

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    @Test
    void shouldConfigureDynamicConnection()
    {
        var tableTransformers = new TableTransformers();
        var parameterConverters = new ParameterConverters(tableTransformers).addConverters(
                new ParameterConverters.FluentEnumConverter());
        var examplesTableFactory = new ExamplesTableFactory(new LocalizedKeywords(), new LoadFromClasspath(),
                parameterConverters, new ParameterControls(), new TableParsers(parameterConverters), tableTransformers);
        var connectionParametersTable = examplesTableFactory.createExamplesTable("""
                |address         |username |password|authentication-scheme|
                |10.10.10.10:5985|admin    |Pa$$w0rd|Basic                |""");
        var key = "new-connection";
        steps.configureWinRmConnection(key, connectionParametersTable);
        var winRmConnectionParametersArgumentCaptor = ArgumentCaptor.forClass(WinRmConnectionParameters.class);
        verify(winRmConnectionParameters).addDynamicConfiguration(eq(key),
                winRmConnectionParametersArgumentCaptor.capture());
        var parameters = winRmConnectionParametersArgumentCaptor.getValue();
        assertAll(
                () -> assertEquals("10.10.10.10:5985", parameters.getAddress()),
                () -> assertEquals("admin", parameters.getUsername()),
                () -> assertEquals("Pa$$w0rd", parameters.getPassword()),
                () -> assertEquals(BASIC_AUTH_SCHEME, parameters.getAuthenticationScheme())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "|any|\n|1|\n|2|" })
    void shouldFailToConfigureDynamicConnection(String tableAsString)
    {
        var connectionParametersTable = new ExamplesTable(tableAsString);
        var key = "invalid-connection";
        var exception = assertThrows(IllegalArgumentException.class,
                () -> steps.configureWinRmConnection(key, connectionParametersTable));
        assertEquals("Exactly one row with WinRM connection parameters is expected in ExamplesTable, but found "
                + connectionParametersTable.getRowCount(), exception.getMessage());
    }

    @Test
    void shouldExecuteBatchCommand()
    {
        var authenticationScheme = AuthScheme.BASIC;
        var disableCertificateChecks = true;

        var serverConfiguration = createServerConfiguration(HTTPS_ADDRESS);
        serverConfiguration.setAuthenticationScheme(BASIC_AUTH_SCHEME);
        serverConfiguration.setDisableCertificateChecks(disableCertificateChecks);

        shouldExecuteCommandUsingWinRm(serverConfiguration, winRmClient -> {
            var command = ECHO_COMMAND;
            var stdout = ECHO_COMMAND_OUTPUT;
            CommandRequest commandRequest = mock();
            CommandResult commandResult = commandResult(stdout);
            when(commandRequest.execute()).thenReturn(commandResult);
            when(winRmClient.command(command)).thenReturn(commandRequest);
            steps.executeBatchCommand(command, SERVER, SCOPES, VARIABLE_NAME);
            return stdout;
        }, (ordered, builder) -> {
            ordered.verify(builder).https();
            ordered.verify(builder).port(5986);
            ordered.verify(builder).trustAllCertificates();
            ordered.verify(builder).authentication(authenticationScheme);
        });
    }

    @Test
    void shouldExecutePowerShellCommand()
    {
        var serverConfiguration = createServerConfiguration(HOSTNAME + ":5986");
        serverConfiguration.setAuthenticationScheme(AuthScheme.NTLM);

        shouldExecuteCommandUsingWinRm(serverConfiguration, winRmClient -> {
            var command = "echo hello ps";
            var stdout = "hello ps";
            CommandRequest commandRequest = mock();
            CommandResult commandResult = commandResult(stdout);
            when(commandRequest.execute()).thenReturn(commandResult);
            when(winRmClient.powerShell(command)).thenReturn(commandRequest);
            steps.executePowerShellCommand(command, SERVER, SCOPES, VARIABLE_NAME);
            return stdout;
        }, (ordered, builder) -> {
            ordered.verify(builder).https();
            ordered.verify(builder).port(5986);
            ordered.verify(builder).authentication(AuthScheme.NTLM);
        });
    }

    @Test
    void shouldExecuteCommandWithKerberosOverHttps()
    {
        var serverConfiguration = createServerConfiguration(HTTPS_ADDRESS);
        serverConfiguration.setAuthenticationScheme(AuthScheme.KERBEROS);

        shouldExecuteCommandUsingWinRm(serverConfiguration, winRmClient -> {
            CommandRequest commandRequest = mock();
            CommandResult commandResult = commandResult(ECHO_COMMAND_OUTPUT);
            when(commandRequest.execute()).thenReturn(commandResult);
            when(winRmClient.command(ECHO_COMMAND)).thenReturn(commandRequest);
            steps.executeBatchCommand(ECHO_COMMAND, SERVER, SCOPES, VARIABLE_NAME);
            return ECHO_COMMAND_OUTPUT;
        }, (ordered, builder) -> {
            ordered.verify(builder).https();
            ordered.verify(builder).port(5986);
            ordered.verify(builder).authentication(AuthScheme.KERBEROS);
        });
    }

    @Test
    void shouldExecuteCommandWithoutAuthenticationOrPort()
    {
        var serverConfiguration = createServerConfiguration(HTTPS_ADDRESS_PREFIX + "/wsman");

        shouldExecuteCommandUsingWinRm(serverConfiguration, winRmClient -> {
            CommandRequest commandRequest = mock();
            CommandResult commandResult = commandResult(ECHO_COMMAND_OUTPUT);
            when(commandRequest.execute()).thenReturn(commandResult);
            when(winRmClient.command(ECHO_COMMAND)).thenReturn(commandRequest);
            steps.executeBatchCommand(ECHO_COMMAND, SERVER, SCOPES, VARIABLE_NAME);
            return ECHO_COMMAND_OUTPUT;
        }, (ordered, builder) -> {
            ordered.verify(builder).https();
            ordered.verify(builder, never()).port(anyInt());
            ordered.verify(builder, never()).authentication(any(AuthScheme.class));
        });
    }

    @Test
    void shouldExecuteCommandUsingHttpEndpoint()
    {
        var serverConfiguration = createServerConfiguration("http://" + HOSTNAME + ":5985/wsman");

        shouldExecuteCommandUsingWinRm(serverConfiguration, winRmClient -> {
            CommandRequest commandRequest = mock();
            CommandResult commandResult = commandResult(ECHO_COMMAND_OUTPUT);
            when(commandRequest.execute()).thenReturn(commandResult);
            when(winRmClient.command(ECHO_COMMAND)).thenReturn(commandRequest);
            steps.executeBatchCommand(ECHO_COMMAND, SERVER, SCOPES, VARIABLE_NAME);
            return ECHO_COMMAND_OUTPUT;
        }, (ordered, builder) -> {
            ordered.verify(builder, never()).https();
            ordered.verify(builder).port(5985);
            ordered.verify(builder, never()).authentication(any(AuthScheme.class));
        });
    }

    @Test
    void shouldFailToExecuteCommandWithAddressWithoutPort()
    {
        var address = "host-without-port";
        var serverConfiguration = createServerConfiguration(address);
        when(winRmConnectionParameters.getConfiguration(SERVER)).thenReturn(serverConfiguration);

        var exception = assertThrows(IllegalArgumentException.class,
                () -> steps.executeBatchCommand(ECHO_COMMAND, SERVER, SCOPES, VARIABLE_NAME));

        assertEquals("WinRM address must include host and port: " + address, exception.getMessage());
    }

    @Test
    void shouldFailToExecuteCommandWithKerberosOverHttp()
    {
        var address = HOSTNAME + ":5985";
        var serverConfiguration = createServerConfiguration(address);
        serverConfiguration.setAuthenticationScheme(AuthScheme.KERBEROS);
        when(winRmConnectionParameters.getConfiguration(SERVER)).thenReturn(serverConfiguration);

        var exception = assertThrows(IllegalArgumentException.class,
                () -> steps.executeBatchCommand(ECHO_COMMAND, SERVER, SCOPES, VARIABLE_NAME));

        assertEquals("Kerberos authentication requires an HTTPS WinRM endpoint: " + address, exception.getMessage());
    }

    @Test
    void shouldFailToExecuteCommandWithInvalidAddress()
    {
        var address = "https://bad host" + HTTPS_ENDPOINT_SUFFIX;
        var serverConfiguration = createServerConfiguration(address);
        when(winRmConnectionParameters.getConfiguration(SERVER)).thenReturn(serverConfiguration);

        var exception = assertThrows(IllegalArgumentException.class,
                () -> steps.executeBatchCommand(ECHO_COMMAND, SERVER, SCOPES, VARIABLE_NAME));

        assertEquals("Invalid WinRM address: " + address, exception.getMessage());
    }

    @Test
    void shouldFailToExecuteCommandWithUnsupportedProtocol()
    {
        var address = "ftp://" + HOSTNAME + HTTPS_ENDPOINT_SUFFIX;
        var serverConfiguration = createServerConfiguration(address);
        when(winRmConnectionParameters.getConfiguration(SERVER)).thenReturn(serverConfiguration);

        var exception = assertThrows(IllegalArgumentException.class,
                () -> steps.executeBatchCommand(ECHO_COMMAND, SERVER, SCOPES, VARIABLE_NAME));

        assertEquals("Unsupported WinRM protocol: ftp", exception.getMessage());
    }

    @SuppressWarnings("PMD.CloseResource")
    private void shouldExecuteCommandUsingWinRm(WinRmConnectionParameters connectionParameters,
            Function<WinRMClient, String> test, BiConsumer<InOrder, WinRMClient.Builder> verifier)
    {
        when(winRmConnectionParameters.getConfiguration(SERVER)).thenReturn(connectionParameters);

        try (var winRmClientStaticMock = mockStatic(WinRMClient.class))
        {
            WinRMClient.Builder builder = mock();
            winRmClientStaticMock.when(() -> WinRMClient.builder(HOSTNAME)).thenReturn(builder);
            when(builder.credentials(eq(connectionParameters.getUsername()), any(char[].class))).thenReturn(builder);
            lenient().when(builder.https()).thenReturn(builder);
            lenient().when(builder.port(5986)).thenReturn(builder);
            lenient().when(builder.trustAllCertificates()).thenReturn(builder);
            lenient().when(builder.authentication(any(AuthScheme.class))).thenReturn(builder);

            WinRMClient winRmClient = mock();
            when(builder.build()).thenReturn(winRmClient);

            String stdout = test.apply(winRmClient);

            var ordered = inOrder(builder, winRmClient, variableContext);
            ordered.verify(builder).credentials(eq(connectionParameters.getUsername()), any(char[].class));
            verifier.accept(ordered, builder);
            ordered.verify(builder).build();
            ordered.verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, Map.of(
                    "stdout", stdout,
                    "stderr", "",
                    "exit-status", 0
            ));
            ordered.verify(winRmClient).close();
        }
    }

    private static CommandResult commandResult(String stdout)
    {
        CommandResult result = mock();
        when(result.stdout()).thenReturn(stdout);
        when(result.stderr()).thenReturn("");
        when(result.exitCode()).thenReturn(0);
        return result;
    }

    private static WinRmConnectionParameters createServerConfiguration(String address)
    {
        var serverConfiguration = new WinRmConnectionParameters();
        serverConfiguration.setAddress(address);
        serverConfiguration.setUsername("user");
        serverConfiguration.setPassword("pa$$w0rd");
        return serverConfiguration;
    }
}
