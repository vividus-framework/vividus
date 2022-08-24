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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.http.client.config.AuthSchemes;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.DynamicConfigurationManager;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

import io.cloudsoft.winrm4j.client.WinRmClientContext;
import io.cloudsoft.winrm4j.winrm.WinRmTool;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;

@ExtendWith(MockitoExtension.class)
class WinRmStepsTests
{
    private static final String SERVER = "win10";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String VARIABLE_NAME = "result";

    @Mock private DynamicConfigurationManager<WinRmConnectionParameters> winRmConnectionParameters;
    @Mock private VariableContext variableContext;
    @InjectMocks private WinRmSteps steps;

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    @Test
    void shouldConfigureDynamicConnection()
    {
        var connectionParametersTable = new ExamplesTable(
                "|address         |username |password|authentication-scheme|\n"
                + "|10.10.10.10:5985|admin    |Pa$$w0rd|Basic                |");
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
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    void shouldExecuteBatchCommand()
    {
        var authenticationScheme = AuthSchemes.BASIC;
        var disableCertificateChecks = true;

        var serverConfiguration = createServerConfiguration("10.240.1.1:5986");
        serverConfiguration.setAuthenticationScheme(authenticationScheme);
        serverConfiguration.setDisableCertificateChecks(disableCertificateChecks);

        shouldExecuteCommandUsingWinRm(serverConfiguration, winRmTool -> {
            var command = "echo hello cmd";
            var stdout = "hello cmd";
            when(winRmTool.executeCommand(command)).thenReturn(new WinRmToolResponse(stdout, "", 0));
            steps.executeBatchCommand(command, SERVER, SCOPES, VARIABLE_NAME);
            return stdout;
        }, (ordered, builder) -> {
            ordered.verify(builder).disableCertificateChecks(disableCertificateChecks);
            ordered.verify(builder).authenticationScheme(authenticationScheme);
        });
    }

    @Test
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    void shouldExecutePowerShellCommand()
    {
        var serverConfiguration = createServerConfiguration("https://10.240.1.1:5986/wsman");

        shouldExecuteCommandUsingWinRm(serverConfiguration, winRmTool -> {
            var command = "echo hello ps";
            var stdout = "hello ps";
            when(winRmTool.executePs(command)).thenReturn(new WinRmToolResponse(stdout, "", 0));
            steps.executePowerShellCommand(command, SERVER, SCOPES, VARIABLE_NAME);
            return stdout;
        }, (ordered, builder) -> { });
    }

    void shouldExecuteCommandUsingWinRm(WinRmConnectionParameters connectionParameters,
            Function<WinRmTool, String> test, BiConsumer<InOrder, WinRmTool.Builder> verifier)
    {
        when(winRmConnectionParameters.getConfiguration(SERVER)).thenReturn(connectionParameters);

        try (var builderStaticMock = mockStatic(WinRmTool.Builder.class);
                var contextStaticMock = mockStatic(WinRmClientContext.class))
        {
            var builder = mock(WinRmTool.Builder.class);
            builderStaticMock.when(() -> WinRmTool.Builder.builder(connectionParameters.getAddress(),
                    connectionParameters.getUsername(), connectionParameters.getPassword())).thenReturn(builder);

            var context = mock(WinRmClientContext.class);
            contextStaticMock.when(WinRmClientContext::newInstance).thenReturn(context);

            when(builder.context(context)).thenReturn(builder);

            var winRmTool = mock(WinRmTool.class);
            when(builder.build()).thenReturn(winRmTool);

            var stdout = test.apply(winRmTool);

            var ordered = inOrder(builder, context, variableContext);
            verifier.accept(ordered, builder);
            ordered.verify(builder).context(context);
            ordered.verify(builder).build();
            ordered.verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, Map.of(
                    "stdout", stdout,
                    "stderr", "",
                    "exit-status", 0
            ));

            ordered.verify(context).shutdown();
        }
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
