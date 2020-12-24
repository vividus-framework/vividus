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

package org.vividus.ssh.sftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.vividus.ssh.CommandExecutionException;
import org.vividus.ssh.Commands;
import org.vividus.ssh.ServerConfiguration;

class SftpExecutionManagerTests
{
    @Test
    void shouldRunExecution() throws CommandExecutionException
    {
        SftpExecutor executor = mock(SftpExecutor.class);
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        Commands commands = new Commands("sftp-command");
        SftpOutput sftpOutput = new SftpOutput();
        when(executor.execute(serverConfiguration, commands)).thenReturn(sftpOutput);
        SftpOutputPublisher outputPublisher = mock(SftpOutputPublisher.class);
        SftpExecutionManager executionManager = new SftpExecutionManager(executor, outputPublisher);
        SftpOutput actual = executionManager.run(serverConfiguration, commands);
        assertEquals(sftpOutput, actual);
        verify(outputPublisher).publishOutput(sftpOutput);
    }
}
