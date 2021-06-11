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

package org.vividus.bdd.steps.db;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.comparison.ComparisonUtils;
import org.vividus.util.comparison.ComparisonUtils.EntryComparisonResult;
import org.vividus.util.property.PropertyMappedCollection;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.util.wait.WaitMode;

public class DatabaseSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSteps.class);
    private final IBddVariableContext bddVariableContext;
    private final IAttachmentPublisher attachmentPublisher;
    private final ISoftAssert softAssert;
    private HashFunction hashFunction;
    private PropertyMappedCollection<DriverManagerDataSource> dataSources;
    private Duration dbQueryTimeout;
    private DuplicateKeysStrategy duplicateKeysStrategy;
    private int diffLimit;

    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();

    public DatabaseSteps(IBddVariableContext bddVariableContext, IAttachmentPublisher attachmentPublisher,
            ISoftAssert softAssert)
    {
        this.bddVariableContext = bddVariableContext;
        this.attachmentPublisher = attachmentPublisher;
        this.softAssert = softAssert;
    }

    /**
     * Step is intended to verify SQL state of JDBC connection
     * @see <a href="https://en.wikipedia.org/wiki/SQLSTATE">SQL States</a>
     * @param dbKey Key identifying the database connection
     * @param username Database username
     * @param password Database password
     * @param comparisonRule The rule to compare values<br>
     * (<i>Possible values:<b> is equal to, contains, does not contain</b></i>)
     * @param sqlState State of SQL connection
    */
    @SuppressWarnings({"try", "EmptyBlock"})
    @Then("SQL state for connection to `$dbKey` with username `$username` and password `$password` $comparisonRule "
            + "`$sqlState`")
    public void verifySqlState(String dbKey, String username, String password, StringComparisonRule comparisonRule,
            String sqlState)
    {
        String actualSqlState = "00000";
        try (Connection connection = getDataSourceByKey(dbKey).getConnection(username, password))
        {
            // empty statement
        }
        catch (SQLException e)
        {
            actualSqlState = e.getSQLState();
        }
        softAssert.assertThat("SQL state for connection", actualSqlState, comparisonRule.createMatcher(sqlState));
    }

    /**
     * Actions performed in the step:
     * <ul>
     *     <li>executes provided SQL query against database by the provided key</li>
     *     <li>saves the query execution result to indexed <i>zero</i>-based variable, e.g. var[0], var[1] and etc.</li>
     * </ul>
     *
     * @param sqlQuery SQL query to execute
     * @param dbKey Key identifying the database connection
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name of variable to assign the values from SQL query execution result
     */
    @When(value = "I execute SQL query `$sqlQuery` against `$dbKey` and save result to $scopes variable"
            + " `$variableName`", priority = 1)
    public void executeSql(String sqlQuery, String dbKey, Set<VariableScope> scopes, String variableName)
    {
        List<Map<String, Object>> result = getJdbcTemplate(dbKey).queryForList(sqlQuery);
        bddVariableContext.putVariable(scopes, variableName, result);
    }

    /**
     * Joins two data sets from previously executed SQL queries;
     * @param leftData data set to join
     * @param rightData data set to join
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName a name of variable to store a result
     */
    @When("I merge `$leftData` and `$rightData` and save result to $scopes variable `$variableName`")
    public void joinDataSets(List<Map<String, Object>> leftData, List<Map<String, Object>> rightData,
            Set<VariableScope> scopes, String variableName)
    {
        List<Map<String, Object>> result = new ArrayList<>(leftData.size() + rightData.size());
        if (!leftData.isEmpty() && !rightData.isEmpty())
        {
            Set<String> leftHeader = leftData.get(0).keySet();
            Set<String> rightHeader = rightData.get(0).keySet();
            Validate.isTrue(leftHeader.equals(rightHeader),
                    "Data sets should have same columns;\nLeft:  %s\nRight: %s", leftHeader, rightHeader);
        }
        result.addAll(leftData);
        result.addAll(rightData);
        bddVariableContext.putVariable(scopes, variableName, result);
    }

    /**
     * Step intended to execute UPDATE, INSERT, DELETE queries.
     * In case of SELECT query exception will be thrown.
     * For SELECT queries please use step:
     * <br><b>When I execute SQL query `$sqlQuery` against `$dbKey`
     * and save result to $scopes variable `$variableName`</b>
     * Actions performed in the step:
     * <ul>
     *     <li>executes provided SQL query against database by the provided key</li>
     *     <li>logs affected lines</li>
     * </ul>
     *
     * @param sqlQuery SQL query to execute
     * @param dbKey Key identifying the database connection
     */
    @When("I execute SQL query `$sqlQuery` against `$dbKey`")
    public void executeSql(String sqlQuery, String dbKey)
    {
        try
        {
            LOGGER.info("Executed query: {}\nAffected rows:{}", sqlQuery, getJdbcTemplate(dbKey).update(sqlQuery));
        }
        catch (DataIntegrityViolationException e)
        {
            throw new IllegalStateException("Exception occured during query execution.\n"
                + "If you are trying execute SELECT query consider using step:"
                + "When I execute SQL query '$sqlQuery' and save the result to the $scopes variable '$variableName'",
                e);
        }
    }

    /**
     * Actions performed in the step:
     * <ul>
     *   <li>executes provided SQL queries against databases by the provided keys</li>
     *   <li>compares queries results </li>
     * </ul>
     * <p>
     *   Use the following property to set the maximum time to wait for DB query completion:
     *   <code>db.query-timeout</code>.<br/>
     *   The default timeout is 30 minutes.
     * </p>
     * <p>
     *   Use the following property to configure the max number of records in the comparison result output:
     *   <code>db.diff-limit</code>.<br/>
     *   The default value is 100
     * </p>
     * <p>
     *   Use the following property to handle rows with duplicate keys:
     *   <code>db.duplicate-keys-strategy</code>.<br/>
     * </p>
     * Acceptable values:
     * <ul>
     *   <li><code>NOOP</code> (by default)</li>
     *   <li><code>DISTINCT</code></li>
     * </ul>
     *
     * @param leftSqlQuery   baseline SQL query
     * @param leftDbKey      key identifying the database connection for the left data set
     * @param comparisonRule The data set comparison rule: "is equal to" or "contains"
     * @param rightSqlQuery  checkpoint SQL query
     * @param rightDbKey     key identifying the database connection for the right data set
     * @param keys           comma-separated list of column's names to map resulting tables rows
     * @throws InterruptedException in case of thread interruption
     * @throws ExecutionException   in case of any exception during DB query
     * @throws TimeoutException     in case when timeout to execute DB query expires
     * @see <a href="https://en.wikipedia.org/wiki/ISO_8601#Durations">Durations format</a>
     */
    @Then("data from `$leftSqlQuery` executed against `$leftDbKey` $comparisonRule data from `$rightSqlQuery` executed"
            + " against `$rightDbKey` matching rows using keys:$keys")
    public void compareData(String leftSqlQuery, String leftDbKey, DataSetComparisonRule comparisonRule,
            String rightSqlQuery, String rightDbKey, Set<String> keys)
            throws InterruptedException, ExecutionException, TimeoutException
    {
        JdbcTemplate leftJdbcTemplate = getJdbcTemplate(leftDbKey);
        JdbcTemplate rightJdbcTemplate = getJdbcTemplate(rightDbKey);
        DataSourceStatistics dataSourceStatistics = new DataSourceStatistics(leftJdbcTemplate, rightJdbcTemplate);
        QueryStatistic left = dataSourceStatistics.getLeft();
        left.setQuery(leftSqlQuery);
        QueryStatistic right = dataSourceStatistics.getRight();
        right.setQuery(rightSqlQuery);
        CompletableFuture<ListMultimap<Object, Map<String, Object>>> leftData =
                createCompletableRequest(leftJdbcTemplate, leftSqlQuery, keys, left);
        CompletableFuture<ListMultimap<Object, Map<String, Object>>> rightData =
                createCompletableRequest(rightJdbcTemplate, rightSqlQuery, keys, right);

        List<List<EntryComparisonResult>> result = leftData.thenCombine(rightData,
                (leftResult, rightResult) -> compareData(comparisonRule, dataSourceStatistics, leftResult, rightResult))
                .get(dbQueryTimeout.toMillis(), TimeUnit.MILLISECONDS);

        verifyComparisonResult(comparisonRule, dataSourceStatistics, result);
    }

    /**
     * The step waits until the <code>leftSqlQuery</code> returns the data which matches to the right examples table
     * rows according the specified comparison rule
     * The order of columns is ignored
     * Actions performed in the step:
     * <ul>
     *     <li>run the <code>leftSqlQuery</code></li>
     *     <li>compares the response with the examples table row</li>
     *     <li>sleep for the <code>duration</code></li>
     *     <li>repeat all of the above until there are no more retry attempts or the result set matches
     *     to the right examples table rows according the specified comparison rule</li>
     * </ul>
     *
     * @param duration       The time gap between two queries
     * @param retryTimes     How many times request will be retried; duration/retryTimes=timeout between requests
     * @param leftSqlQuery   SQL query to execute
     * @param leftDbKey      Key identifying the database connection
     * @param comparisonRule The data set comparison rule: "is equal to" or "contains"
     * @param rightTable     Rows to compare data against
     */
    @When("I wait for '$duration' duration retrying $retryTimes times while data from `$leftSqlQuery`"
            + " executed against `$leftDbKey` $comparisonRule data from:$rightTable")
    public void waitForDataAppearance(Duration duration, int retryTimes, String leftSqlQuery, String leftDbKey,
            DataSetComparisonRule comparisonRule, List<Map<String, String>> rightTable)
    {
        JdbcTemplate leftJdbcTemplate = getJdbcTemplate(leftDbKey);
        DataSourceStatistics statistic = new DataSourceStatistics(leftJdbcTemplate);
        statistic.getLeft().setQuery(leftSqlQuery);
        ListMultimap<Object, Map<String, Object>> rightData = hashMap(Set.of(), rightTable);
        statistic.getRight().setRowsQuantity(rightData.size());

        DurationBasedWaiter waiter = new DurationBasedWaiter(new WaitMode(duration, retryTimes));
        List<List<EntryComparisonResult>> comparisonResult = waiter.wait(
            () -> {
                List<Map<String, Object>> left = leftJdbcTemplate.queryForList(leftSqlQuery);
                statistic.getLeft().setRowsQuantity(left.size());
                ListMultimap<Object, Map<String, Object>> leftData = hashMap(Set.of(),
                        left.stream().map(this::convertValuesToString));
                return compareData(comparisonRule, statistic, leftData, rightData);
            },
            result -> {
                boolean empty = result.isEmpty();
                if (!empty)
                {
                    LOGGER.atInfo().addArgument(result::size).log(
                            "SQL result data is not equal to expected data in {} records");
                }
                return empty;
            }
        );
        verifyComparisonResult(comparisonRule, statistic, filterPassedChecks(comparisonResult));
    }

    private void verifyComparisonResult(DataSetComparisonRule comparisonRule, DataSourceStatistics dataSourceStatistics,
            List<List<EntryComparisonResult>> result)
    {
        attachmentPublisher.publishAttachment("data-sources-statistics.ftl", Map.of("statistics", dataSourceStatistics),
                "Data sources statistics");
        if (!softAssert.assertTrue(comparisonRule.getAssertionDescription(), result.isEmpty()))
        {
            attachmentPublisher.publishAttachment("/templates/maps-comparison-table.ftl", Map.of("results", result),
                    "Data sets comparison");
        }
    }

    /**
     * The step is designed to compare the data retrieved by SQL request against the examples table.
     * The order of columns is ignored.
     * Consider complete example:
     * <br>When I execute SQL query `${sqlQuery}` against `$dbKey` and save result to story variable `data`
     * <br>Then `${data}` matching rows using `` from `$dbKey` is equal to data from:
     * <br>tables/data.table
     *
     * @param leftData       saved by step:
     *                       When I execute SQL query `$sqlQuery` against `$dbKey` and save result to $scopes
     *                       variable `$data`"
     * @param keys           comma-separated list of column's names to map resulting tables rows
     *                       (could be empty - all columns will be used)
     * @param leftDbKey      key identifying the database connection used to get data
     * @param comparisonRule The data set comparison rule: "is equal to" or "contains"
     * @param rightTable     rows to compare data against
     */
    @Then("`$leftData` matching rows using `$keys` from `$leftDbKey` $comparisonRule data from:$rightTable")
    public void compareData(List<Map<String, Object>> leftData, Set<String> keys, String leftDbKey,
            DataSetComparisonRule comparisonRule, List<Map<String, String>> rightTable)
    {
        JdbcTemplate leftJdbcTemplate = getJdbcTemplate(leftDbKey);
        DataSourceStatistics statistics = new DataSourceStatistics(leftJdbcTemplate);
        statistics.getLeft().setRowsQuantity(leftData.size());
        ListMultimap<Object, Map<String, Object>> left = hashMap(keys,
                leftData.stream().map(this::convertValuesToString));
        ListMultimap<Object, Map<String, Object>> right = hashMap(keys, rightTable);
        statistics.getRight().setRowsQuantity(right.size());
        List<List<EntryComparisonResult>> result = compareData(comparisonRule, statistics, left, right);
        verifyComparisonResult(comparisonRule, statistics, filterPassedChecks(result));
    }

    private List<List<EntryComparisonResult>> compareData(DataSetComparisonRule comparisonRule,
            DataSourceStatistics dataSourceStatistics, ListMultimap<Object, Map<String, Object>> leftData,
            ListMultimap<Object, Map<String, Object>> rightData)
    {
        List<Pair<Map<String, Object>, Map<String, Object>>> comparison = new ArrayList<>();
        comparisonRule.collectComparisonKeys(leftData, rightData).forEach(key ->
        {
            List<Map<String, Object>> left = leftData.get(key);
            List<Map<String, Object>> right = rightData.get(key);
            int leftSize = left.size();
            int rightSize = right.size();
            int size = duplicateKeysStrategy.getTargetSize(comparisonRule, leftSize, rightSize);
            for (int i = 0; i < size; i++)
            {
                Map<String, Object> leftValue = i < leftSize ? left.get(i) : Map.of();
                Map<String, Object> rightValue = i < rightSize ? right.get(i) : Map.of();
                comparison.add(Pair.of(leftValue, rightValue));
            }
        });
        comparisonRule.fillStatistics(dataSourceStatistics, comparison);
        List<List<EntryComparisonResult>> comparisonResult = comparison.stream()
                .parallel()
                .map(p -> ComparisonUtils.compareMaps(p.getLeft(), p.getRight()))
                .collect(Collectors.toList());
        List<List<EntryComparisonResult>> mismatchedRows = filterPassedChecks(comparisonResult);
        dataSourceStatistics.setMismatched(mismatchedRows.size());
        dataSourceStatistics.setTotalRows(comparisonResult.size());
        return mismatchedRows.size() > diffLimit ? mismatchedRows.subList(0, diffLimit) : mismatchedRows;
    }

    private List<List<EntryComparisonResult>> filterPassedChecks(List<List<EntryComparisonResult>> comparisonResult)
    {
        return comparisonResult.stream()
                        .filter(r -> !r.stream().allMatch(EntryComparisonResult::isPassed))
                        .collect(Collectors.toList());
    }

    private CompletableFuture<ListMultimap<Object, Map<String, Object>>> createCompletableRequest(
            JdbcTemplate jdbcTemplate, String sqlRequest, Set<String> keys, QueryStatistic statistics)
    {
        return CompletableFuture.supplyAsync(() -> {
            statistics.start();
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sqlRequest);
            statistics.end();
            statistics.setRowsQuantity(result.size());
            return result;
        })
                .thenApplyAsync(r -> hashMap(keys, r));
    }

    private Map<String, Object> convertValuesToString(Map<String, Object> map)
    {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
    }

    @SuppressWarnings("unchecked")
    private ListMultimap<Object, Map<String, Object>> hashMap(Set<String> keys, List<?> r)
    {
        return hashMap(keys, ((List<Map<String, Object>>) r).stream());
    }

    private ListMultimap<Object, Map<String, Object>> hashMap(Set<String> keys, Stream<Map<String, Object>> data)
    {
        return data.collect(Multimaps.toMultimap(row -> hash(keys, row), row -> row, ArrayListMultimap::create));
    }

    private HashCode hash(Set<String> keys, Map<String, Object> map)
    {
        return hashFunction.hashString((keys.isEmpty() ? map.keySet() : keys)
                           .stream()
                           .map(map::get)
                           .filter(Objects::nonNull)
                           .map(Object::toString)
                           .sorted()
                           .collect(Collectors.joining()), StandardCharsets.UTF_8);
    }

    private JdbcTemplate getJdbcTemplate(String dbKey)
    {
        return jdbcTemplates.computeIfAbsent(dbKey, key -> new JdbcTemplate(getDataSourceByKey(key)));
    }

    private DriverManagerDataSource getDataSourceByKey(String key)
    {
        return dataSources.get(key, "Database connection with key '%s' is not configured in properties", key);
    }

    public void setDataSources(PropertyMappedCollection<DriverManagerDataSource> dataSources)
    {
        this.dataSources = dataSources;
    }

    public void setDbQueryTimeout(Duration dbQueryTimeout)
    {
        this.dbQueryTimeout = dbQueryTimeout;
    }

    public void setHashFunction(HashFunction hashFunction)
    {
        this.hashFunction = hashFunction;
    }

    public void setDuplicateKeysStrategy(DuplicateKeysStrategy duplicateKeysStrategy)
    {
        this.duplicateKeysStrategy = duplicateKeysStrategy;
    }

    public void setDiffLimit(int diffLimit)
    {
        this.diffLimit = diffLimit;
    }
}
