/*
 * Copyright 2019 the original author or authors.
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

import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

public class SingleCommand<T>
{
    private final T command;
    private final List<String> parameters;

    public SingleCommand(String rawCommand, Function<String, T> commandFactory)
    {
        String[] values = StringUtils.split(rawCommand, " ", 2);
        this.command = commandFactory.apply(values[0]);
        this.parameters = values.length > 1 ? List.of(values[1]) : List.of();
    }

    public SingleCommand(T command, List<String> parameters)
    {
        this.command = command;
        this.parameters = parameters;
    }

    public T getCommand()
    {
        return command;
    }

    public List<String> getParameters()
    {
        return parameters;
    }
}
