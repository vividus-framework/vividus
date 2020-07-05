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

package org.vividus.bdd.steps.mongodb;

import static java.util.function.Function.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import org.apache.commons.lang3.Validate;
import org.bson.conversions.Bson;
import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.mongodb.command.CommandType;
import org.vividus.bdd.steps.mongodb.command.MongoCommand;
import org.vividus.bdd.steps.mongodb.command.MongoCommandEntry;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.util.json.JsonUtils;

public class MongoDbSteps
{
    private final Map<String, String> connections;
    private final JsonUtils jsonUtils;
    private final IBddVariableContext bddVariableContext;

    public MongoDbSteps(Map<String, String> connections, JsonUtils jsonUtils, IBddVariableContext bddVariableContext)
    {
        this.connections = connections;
        this.jsonUtils = jsonUtils;
        this.bddVariableContext = bddVariableContext;
    }

    /**
     * Actions performed in the step:
     * <ul>
     *     <li>executes provided <b>command</b> against MongoDB instance by the provided <b>instanceKey</b></li>
     *     <li>saves the command execution result into <b>variableName</b> variable in JSON format</li>
     * </ul>
     *
     * @param command command to perform e.g. <i>{ listCollections: 1, nameOnly: true }</i>
     * @param dbName database name e.g. <i>users</i>
     * @param instanceKey key of particular connection under <b>mongodb.connection.</b> prefix
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name of variable to assign the values from command execution result
     * @see <a href="https://docs.mongodb.com/manual/reference/command/">Database commands</a>
     */
    @When("I execute command `$command` against `$dbName` database on `$instanceKey` MongoDB instance and save result "
            + "to $scopes variable `$variableName`")
    public void executeCommand(Bson command, String dbName, String instanceKey, Set<VariableScope> scopes,
            String variableName)
    {
        executeInDatabase(instanceKey, dbName,
            db -> db.runCommand(command).entrySet().stream()
                .peek(e -> e.setValue(jsonUtils.toJson(e.getValue())))
                .collect(Collectors.collectingAndThen(Collectors.toMap(Entry::getKey, Entry::getValue),
                    putVariable(scopes, variableName))));
    }

    /**
     * Actions performed in the step:
     * <ul>
     *     <li>verifies the sequence of provided <b>commands</b> (see rules below)</li>
     *     <li>executes provided <b>commands</b> against MongoDB instance by the provided <b>instanceKey</b></li>
     *     <li>saves the commands execution result into <b>variableName</b> variable in JSON format</li>
     * </ul>
     * Commands
     * <table border="1">
     * <caption>A table of commands</caption>
     * <tr>
     * <th>Name</th>
     * <th>Type</th>
     * <th>Description</th>
     * <th>Example</th>
     * </tr>
     * <tr>
     * <td>find</td>
     * <td>source</td>
     * <td>selects documents in a collection, takes JSON as an argument</td>
     * <td>{ age: { $gte: 20 }, city: "minsk" }</td>
     * </tr>
     * <tr>
     * <td>projection</td>
     * <td>intermediate</td>
     * <td>determine which fields to include in the returned documents, takes JSON as an argument</td>
     * <td>{ age: 1, city: 1, name: 0 }</td>
     * </tr>
     * <tr>
     * <td>count</td>
     * <td>terminal</td>
     * <td>counts the number of documents in a collection, takes no arguments</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td>collect</td>
     * <td>terminal</td>
     * <td>collects previously found documents into JSON format, takes no arguments</td>
     * <td></td>
     * </tr>
     * </table>
     * Command sequence rules
     * <ul>
     * <li>commands sequence must start with one of the <b>source</b> operations</li>
     * <li>commands sequence is allowed to have only <b>intermediate</b> operations between the first and last
     * commands</li>
     * <li>commands sequence must end with one of the <b>terminal</b> operations</li>
     * </ul>
     * @param commands sequence of commands to execute
     * @param collectionName collection name to retrieve documents from e.g. <i>phone_book</i>
     * @param dbName database name e.g. <i>users</i>
     * @param instanceKey key of particular connection under <b>mongodb.connection.</b> prefix
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name of variable to assign the values from command execution result
     */
    @When("I execute commands $commands in `$collectionName` collection against `$dbName` database on `$instanceKey` "
            + "MongoDB instance and save result to $scopes variable `$variableName`")
    public void executeCommands(List<MongoCommandEntry> commands, String collectionName, String dbName,
            String instanceKey, Set<VariableScope> scopes, String variableName)
    {
        verify(commands);
        executeInDatabase(instanceKey, dbName,
            db -> commands.stream()
                .reduce(identity(), (f, c) -> c.getCommand().apply(f, c.getArgument()), (l, r) -> l)
                .andThen(jsonUtils::toJson)
                .andThen(putVariable(scopes, variableName))
                .apply(db.getCollection(collectionName)));
    }

    private <T> UnaryOperator<T> putVariable(Set<VariableScope> scopes, String variableName)
    {
        return r ->
        {
            bddVariableContext.putVariable(scopes, variableName, r);
            return r;
        };
    }

    private static void verify(List<MongoCommandEntry> commands)
    {
        Validate.notEmpty(commands, "Command sequence must not be empty");

        List<String> errors = new ArrayList<>();
        boolean checkStart = !commands.get(0).getCommand().getCommandType().equals(CommandType.SOURCE);
        appendIf(checkStart, () -> "Command sequence must start with one of the source commands: "
                + MongoCommand.findByCommandType(CommandType.SOURCE), errors);

        boolean checkEnd = !commands.get(commands.size() - 1).getCommand().getCommandType()
                .equals(CommandType.TERMINAL);
        appendIf(checkEnd, () -> "Command sequence must end with one of the terminal commands: "
                + MongoCommand.findByCommandType(CommandType.TERMINAL), errors);

        boolean checkIntermediate = commands.size() > 2 && !commands.subList(1, commands.size() - 1).stream()
                .map(MongoCommandEntry::getCommand)
                .map(MongoCommand::getCommandType)
                .allMatch(CommandType.INTERMEDIATE::equals);
        appendIf(checkIntermediate,
            () -> "Only the following commands are allowed between the first and the last ones: "
                + MongoCommand.findByCommandType(CommandType.INTERMEDIATE), errors);

        Validate.isTrue(errors.isEmpty(), "%n%s",
                errors.stream().map(e -> " - " + e).collect(Collectors.joining(System.lineSeparator())));
    }

    private static void appendIf(boolean condition, Supplier<String> error, List<String> errors)
    {
        if (condition)
        {
            errors.add(error.get());
        }
    }

    private void executeInDatabase(String connectionKey, String dbKey, Consumer<MongoDatabase> databaseConsumer)
    {
        String connection = connections.get(connectionKey);
        Validate.validState(connection != null, "Connection with key '%s' does not exist", connectionKey);
        try (MongoClient client = MongoClients.create(connection))
        {
            MongoDatabase database = client.getDatabase(dbKey);
            databaseConsumer.accept(database);
        }
    }
}
