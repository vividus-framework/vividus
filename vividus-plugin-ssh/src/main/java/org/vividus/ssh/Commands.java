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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public class Commands
{
    private final String joinedCommands;

    public Commands(String joinedCommands)
    {
        this.joinedCommands = joinedCommands;
    }

    public String getJoinedCommands()
    {
        return joinedCommands;
    }

    public <T> List<SingleCommand<T>> getSingleCommands(Function<String, T> commandFactory)
    {
        return Stream.of(StringUtils.split(joinedCommands, ';'))
                .map(rawCommand -> new SingleCommand<>(rawCommand, commandFactory))
                .collect(toList());
    }
}
