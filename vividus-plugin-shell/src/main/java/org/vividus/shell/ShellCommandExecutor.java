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

package org.vividus.shell;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import org.apache.tika.utils.FileProcessResult;
import org.apache.tika.utils.ProcessUtils;
import org.vividus.shell.model.Shell;
import org.vividus.util.property.PropertyMappedCollection;

public class ShellCommandExecutor
{
    private final PropertyMappedCollection<Shell> shells;
    private final String shell;

    private Duration processFinishWaitTimeout;

    public ShellCommandExecutor(PropertyMappedCollection<Shell> shells, Optional<String> shell)
    {
        this.shells = shells;
        String osName = System.getProperty("os.name");
        this.shell = shell.orElseGet(() -> {
            if (osName.startsWith("Mac"))
            {
                return "zsh";
            }
            if (osName.startsWith("Windows"))
            {
                return "powershell";
            }
            return "bash";
        });
    }

    public FileProcessResult executeCommand(String command) throws IOException
    {
        return executeCommand(shell, command);
    }

    public FileProcessResult executeCommand(String shellKey, String command) throws IOException
    {
        Shell shellToUse = shells.get(shellKey, "Unable to find the shell with key `%s`. Configured shells: %s",
                shellKey, shells.getData().keySet());
        ProcessBuilder processBuilder = new ProcessBuilder().command(shellToUse.getExecutable(),
            shellToUse.getOption(), command);
        FileProcessResult fileProcessResult = ProcessUtils.execute(processBuilder,
                processFinishWaitTimeout.toMillis(), Integer.MAX_VALUE, Integer.MAX_VALUE);
        if (fileProcessResult.isTimeout())
        {
            throw new IllegalStateException(
                    String.format("The command `%s` execution is not finished in `%s`.%nError: %s,%nOutput: %s",
                            command, processFinishWaitTimeout, fileProcessResult.getStderr(),
                            fileProcessResult.getStdout()));
        }

        return fileProcessResult;
    }

    public void setProcessFinishWaitTimeout(Duration processFinishWaitTimeout)
    {
        this.processFinishWaitTimeout = processFinishWaitTimeout;
    }
}
