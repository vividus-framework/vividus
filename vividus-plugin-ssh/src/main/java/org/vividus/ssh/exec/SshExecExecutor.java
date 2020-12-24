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

import javax.inject.Named;

import com.jcraft.jsch.ChannelExec;

import org.vividus.ssh.Commands;
import org.vividus.ssh.ServerConfiguration;

@Named
public class SshExecExecutor extends SshExecutor<ChannelExec>
{
    @Override
    public String getChannelType()
    {
        return "exec";
    }

    @Override
    protected void configureChannel(ChannelExec channel, ServerConfiguration serverConfiguration)
    {
        channel.setAgentForwarding(serverConfiguration.isAgentForwarding());
        channel.setPty(serverConfiguration.isPseudoTerminalEnabled());
    }

    @Override
    protected void setupCommands(ChannelExec channel, Commands commands)
    {
        channel.setCommand(commands.getJoinedCommands());
    }
}
