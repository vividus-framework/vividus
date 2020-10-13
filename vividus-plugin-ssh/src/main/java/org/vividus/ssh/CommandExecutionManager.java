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

package org.vividus.ssh;

public abstract class CommandExecutionManager<R>
{
    private final OutputPublisher<R> outputPublisher;

    protected CommandExecutionManager(OutputPublisher<R> outputPublisher)
    {
        this.outputPublisher = outputPublisher;
    }

    public R run(ServerConfiguration serverConfiguration, Commands commands) throws CommandExecutionException
    {
        R executionOutput = getCommandExecutor(serverConfiguration).execute(serverConfiguration, commands);
        outputPublisher.publishOutput(executionOutput);
        return executionOutput;
    }

    protected abstract CommandExecutor<R> getCommandExecutor(ServerConfiguration serverConfiguration);
}
