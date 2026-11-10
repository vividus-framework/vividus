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

package org.vividus.ssh;

import java.util.Optional;

import org.jbehave.core.annotations.AsParameters;
import org.jbehave.core.annotations.Parameter;

@AsParameters
public class SshConnectionParameters extends SshSessionParameters
{
    @Parameter(name = "channel-type")
    private Optional<String> channelType = Optional.empty();

    @Parameter(name = "pseudo-terminal-enabled")
    private boolean pseudoTerminalEnabled;

    public boolean isPseudoTerminalEnabled()
    {
        return pseudoTerminalEnabled;
    }

    public void setPseudoTerminalEnabled(boolean pseudoTerminalEnabled)
    {
        this.pseudoTerminalEnabled = pseudoTerminalEnabled;
    }

    public Optional<String> getChannelType()
    {
        return channelType;
    }

    public void setChannelType(Optional<String> channelType)
    {
        this.channelType = channelType;
    }
}
