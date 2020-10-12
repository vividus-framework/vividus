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

package org.vividus.ssh.exec;

import java.util.List;

import javax.inject.Named;

import com.jcraft.jsch.Channel;

import org.vividus.ssh.CommandExecutionManager;
import org.vividus.ssh.CommandExecutor;
import org.vividus.ssh.ServerConfiguration;

@Named("SSH")
public class SshExecutionManager extends CommandExecutionManager<SshOutput>
{
    private final List<SshExecutor<? extends Channel>> sshExecutors;

    public SshExecutionManager(List<SshExecutor<? extends Channel>> sshExecutors, SshOutputPublisher outputPublisher)
    {
        super(outputPublisher);
        this.sshExecutors = sshExecutors;
    }

    @Override
    protected CommandExecutor<SshOutput> getCommandExecutor(ServerConfiguration serverConfiguration)
    {
        String channelType = serverConfiguration.getChannelType().orElse("exec");
        return sshExecutors.stream()
                           .filter(e -> channelType.equals(e.getChannelType()))
                           .findFirst()
                           .orElseThrow(() -> new IllegalArgumentException("Unsupported channel type: " + channelType));
    }
}
