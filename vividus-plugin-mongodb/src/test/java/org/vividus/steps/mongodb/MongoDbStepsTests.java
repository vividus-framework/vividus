/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps.mongodb;

import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.steps.mongodb.command.MongoCommand;
import org.vividus.steps.mongodb.command.MongoCommandEntry;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class MongoDbStepsTests
{
    private static final String LOCAL_KEY = "localKey";
    private static final Document COMMAND = Document.parse("{ listCollections: 1, nameOnly: true }");
    private static final String VARIABLE_KEY = "variableKey";
    private static final String CONNECTION_KEY = "mongodb://0.0.0.0:27017";
    private static final String COLLECTION_KEY = "collectionKey";
    private static final String DOCUMENT_JSON = "{\"id\":1}";

    @Mock
    private VariableContext context;

    private final JsonUtils jsonUtils = new JsonUtils();

    @Test
    void testExecuteCommandNoConnection()
    {
        MongoDbSteps steps = new MongoDbSteps(Map.of(), jsonUtils, context);
        Exception exception = assertThrows(IllegalStateException.class,
            () -> steps.executeCommand(COMMAND, LOCAL_KEY, LOCAL_KEY, Set.of(VariableScope.STORY), VARIABLE_KEY));
        assertEquals("Connection with key 'localKey' does not exist", exception.getMessage());
    }

    @Test
    void testExecuteCommand()
    {
        try (MockedStatic<MongoClients> mongoClients = Mockito.mockStatic(MongoClients.class))
        {
            MongoDatabase database = mockDatabase(mongoClients);
            when(database.runCommand(COMMAND)).thenReturn(Document.parse(DOCUMENT_JSON));

            MongoDbSteps steps = new MongoDbSteps(Map.of(LOCAL_KEY, CONNECTION_KEY), jsonUtils, context);
            steps.executeCommand(COMMAND, LOCAL_KEY, LOCAL_KEY, Set.of(VariableScope.STORY), VARIABLE_KEY);

            verify(context).putVariable(Set.of(VariableScope.STORY), VARIABLE_KEY, Map.of("id", "1"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testExecuteCommands()
    {
        try (MockedStatic<MongoClients> mongoClients = Mockito.mockStatic(MongoClients.class))
        {
            MongoDatabase database = mockDatabase(mongoClients);
            MongoCollection<Document> collection = mock(MongoCollection.class);
            when(database.getCollection(COLLECTION_KEY)).thenReturn(collection);
            Bson argument = mock(Bson.class);
            FindIterable<Document> findIterable = mock(FindIterable.class);
            when(collection.find(argument)).thenReturn(findIterable);
            when(findIterable.spliterator()).thenReturn(List.of(Document.parse(DOCUMENT_JSON)).spliterator());

            MongoDbSteps steps = new MongoDbSteps(Map.of(LOCAL_KEY, CONNECTION_KEY), jsonUtils, context);

            steps.executeCommands(
                    List.of(commandEntry(MongoCommand.FIND, argument), commandEntry(MongoCommand.COLLECT, argument)),
                    COLLECTION_KEY, LOCAL_KEY, LOCAL_KEY, Set.of(VariableScope.STORY), VARIABLE_KEY);
            verify(context).putVariable(Set.of(VariableScope.STORY), VARIABLE_KEY,
                    String.format("[%s]", DOCUMENT_JSON));
        }
    }

    static Stream<Arguments> invalidCommandSequence()
    {
        return Stream.of(
            Arguments.of(List.of(
                commandEntry(MongoCommand.PROJECTION, mock(Bson.class)),
                commandEntry(MongoCommand.COLLECT, mock(Bson.class)),
                commandEntry(MongoCommand.FIND, mock(Bson.class))),
                    lineSeparator()
                    + " - Command sequence must start with one of the source commands: [FIND]"
                    + lineSeparator()
                    + " - Command sequence must end with one of the terminal commands: [COLLECT, COUNT]"
                    + lineSeparator()
                    + " - Only the following commands are allowed between the first and the last ones: "
                    + "[PROJECTION]"),
            Arguments.of(List.of(), "Command sequence must not be empty"));
    }

    @MethodSource("invalidCommandSequence")
    @ParameterizedTest
    void testExecuteCommandsInvalidSequence(List<MongoCommandEntry> entries, String message)
    {
        MongoDbSteps steps = new MongoDbSteps(Map.of(LOCAL_KEY, CONNECTION_KEY), jsonUtils, context);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> steps.executeCommands(entries,
            COLLECTION_KEY, LOCAL_KEY, LOCAL_KEY, Set.of(VariableScope.STORY), VARIABLE_KEY));
        assertEquals(message, exception.getMessage());
    }

    private static MongoCommandEntry commandEntry(MongoCommand command, Bson argument)
    {
        MongoCommandEntry entry = new MongoCommandEntry();
        entry.setCommand(command);
        entry.setArgument(argument);
        return entry;
    }

    @SuppressWarnings("PMD.CloseResource")
    private MongoDatabase mockDatabase(MockedStatic<MongoClients> mongoClients)
    {
        MongoClient client = mock(MongoClient.class);
        mongoClients.when(() -> MongoClients.create(CONNECTION_KEY)).thenReturn(client);

        MongoDatabase database = mock(MongoDatabase.class);
        when(client.getDatabase(LOCAL_KEY)).thenReturn(database);

        return database;
    }
}
