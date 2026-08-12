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

package org.vividus.winrm;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jbehave.core.model.ExamplesTable;
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
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String VARIABLE_NAME = "result";

    @Mock private DynamicConfigurationManager<WinRmConnectionParameters> winRmConnectionParameters;
    @Mock private VariableContext variableContext;
    @InjectMocks private WinRmSteps steps;

    @Test
    void shouldConfigureDynamicConnection()
    {
        var connectionParametersTable = new ExamplesTable(
                "|address         |username |password|authentication-scheme|\n"
                + "|10.10.10.10:5985|admin    |Pa$$w0rd|NTLM                 |");
        var key = "new-connection";
        steps.configureWinRmConnection(key, connectionParametersTable);
        var winRmConnectionParametersArgumentCaptor = ArgumentCaptor.forClass(WinRmConnectionParameters.class);
        verify(winRmConnectionParameters).addDynamicConfiguration(eq(key),
                winRmConnectionParametersArgumentCaptor.capture());
        var parameters = winRmConnectionParametersArgumentCaptor.getValue();
        assertAll(
                () -> assertEquals("10.10.10.10:5985", parameters.getAddress()),
                () -> assertEquals("admin", parameters.getUsername()),
                () -> assertEquals("Pa$$w0rd", parameters.getPassword())
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
        var authenticationScheme = AuthScheme.NTLM;
        var disableCertificateChecks = true;

        var serverConfiguration = createServerConfiguration(HOSTNAME + ":5986");
        serverConfiguration.setAuthenticationScheme(authenticationScheme);
        serverConfiguration.setDisableCertificateChecks(disableCertificateChecks);

        shouldExecuteCommandUsingWinRm(serverConfiguration, winRmClient -> {
            var command = "echo hello cmd";
            var stdout = "hello cmd";
            var result = commandResult(stdout, "", 0);
            var commandRequest = mock(CommandRequest.class);
            when(commandRequest.execute()).thenReturn(result);
            when(winRmClient.command(command)).thenReturn(commandRequest);
            steps.executeBatchCommand(command, SERVER, SCOPES, VARIABLE_NAME);
            return stdout;
        }, (ordered, builder) -> {
            ordered.verify(builder).port(5986);
            ordered.verify(builder).trustAllCertificates();
            ordered.verify(builder).authentication(authenticationScheme);
        });
    }

    @Test
    void shouldExecutePowerShellCommand()
    {
        var serverConfiguration = createServerConfiguration("https://" + HOSTNAME + ":5986/wsman");

        shouldExecuteCommandUsingWinRm(serverConfiguration, winRmClient -> {
            var command = "echo hello ps";
            var stdout = "hello ps";
            var result = commandResult(stdout, "", 0);
            var commandRequest = mock(CommandRequest.class);
            when(commandRequest.execute()).thenReturn(result);
            when(winRmClient.powerShell(command)).thenReturn(commandRequest);
            steps.executePowerShellCommand(command, SERVER, SCOPES, VARIABLE_NAME);
            return stdout;
        }, (ordered, builder) -> {
            ordered.verify(builder).https();
            ordered.verify(builder).port(5986);
        });
    }

    @SuppressWarnings("PMD.CloseResource")
    void shouldExecuteCommandUsingWinRm(WinRmConnectionParameters connectionParameters,
            Function<WinRMClient, String> test, BiConsumer<InOrder, WinRMClient.Builder> verifier)
    {
        when(winRmConnectionParameters.getConfiguration(SERVER)).thenReturn(connectionParameters);

        try (var winRmClientStaticMock = mockStatic(WinRMClient.class))
        {
            var builder = mock(WinRMClient.Builder.class);
            winRmClientStaticMock.when(() -> WinRMClient.builder(HOSTNAME)).thenReturn(builder);
            when(builder.credentials(eq(connectionParameters.getUsername()), any(char[].class)))
                    .thenReturn(builder);
            lenient().when(builder.https()).thenReturn(builder);
            lenient().when(builder.port(5986)).thenReturn(builder);
            lenient().when(builder.trustAllCertificates()).thenReturn(builder);
            lenient().when(builder.authentication(any(AuthScheme.class))).thenReturn(builder);

            var winRmClient = mock(WinRMClient.class);
            when(builder.build()).thenReturn(winRmClient);

            var stdout = test.apply(winRmClient);

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

    private static CommandResult commandResult(String stdout, String stderr, int exitCode)
    {
        var result = mock(CommandResult.class);
        when(result.stdout()).thenReturn(stdout);
        when(result.stderr()).thenReturn(stderr);
        when(result.exitCode()).thenReturn(exitCode);
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
