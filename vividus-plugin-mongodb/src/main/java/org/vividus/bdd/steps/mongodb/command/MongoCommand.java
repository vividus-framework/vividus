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

package org.vividus.bdd.steps.mongodb.command;

import static java.util.stream.StreamSupport.stream;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import org.bson.conversions.Bson;

public enum MongoCommand
{
    FIND(CommandType.SOURCE)
    {
        @Override
        public Function<Object, Object> apply(Function<Object, Object> chain, Bson argument)
        {
            return chain.andThen(out -> cast(out, MongoCollection.class).find(argument));
        }
    },
    PROJECTION(CommandType.INTERMEDIATE)
    {
        @Override
        public Function<Object, Object> apply(Function<Object, Object> chain, Bson argument)
        {
            return chain.andThen(out -> cast(out, FindIterable.class).projection(argument));
        }
    },
    COLLECT(CommandType.TERMINAL)
    {
        @SuppressWarnings("unchecked")
        @Override
        public Function<Object, Object> apply(Function<Object, Object> chain, Bson argument)
        {
            return chain.andThen(
                out -> stream(cast(out, FindIterable.class).spliterator(), false).collect(Collectors.toList()));
        }
    },
    COUNT(CommandType.TERMINAL)
    {
        @SuppressWarnings("unchecked")
        @Override
        public Function<Object, Object> apply(Function<Object, Object> chain, Bson argument)
        {
            return chain.andThen(out -> stream(cast(out, FindIterable.class).spliterator(), false).count());
        }
    };

    private static final Map<CommandType, List<MongoCommand>> COMMANDS;

    private final CommandType commandType;

    MongoCommand(CommandType commandType)
    {
        this.commandType = commandType;
    }

    static
    {
        COMMANDS = Stream.of(values())
                .collect(Collectors.groupingBy(MongoCommand::getCommandType, Collectors.toList()));
    }

    public CommandType getCommandType()
    {
        return commandType;
    }

    public static Collection<MongoCommand> findByCommandType(CommandType commandType)
    {
        return COMMANDS.get(commandType);
    }

    public abstract Function<Object, Object> apply(Function<Object, Object> chain, Bson argument);

    <T> T cast(Object object, Class<T> type)
    {
        return type.cast(object);
    }
}
