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
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
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
     * @param left data set to join
     * @param right data set to join
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
    @When("I merge `$left` and `$right` and save result to $scopes variable `$variableName`")
    public void joinDataSets(List<Map<String, Object>> left, List<Map<String, Object>> right,
            Set<VariableScope> scopes, String variableName)
    {
        List<Map<String, Object>> result = new ArrayList<>(left.size() + right.size());
        if (!left.isEmpty() && !right.isEmpty())
        {
            Set<String> leftHeader = left.get(0).keySet();
            Set<String> rightHeader = right.get(0).keySet();
            Validate.isTrue(leftHeader.equals(rightHeader),
                    "Data sets should have same columns;\nLeft:  %s\nRight: %s", leftHeader, rightHeader);
        }
        result.addAll(left);
        result.addAll(right);
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
     * @see <a href="https://en.wikipedia.org/wiki/ISO_8601#Durations">Durations format</a>
     * @param sourceSqlQuery baseline SQL query
     * @param sourceDbKey key identifying source database connection
     * @param targetSqlQuery checkpoint SQL query
     * @param targetDbKey key identifying target database connection
     * @param keys comma-separated list of column's names to map resulting tables rows
     * @throws InterruptedException in case of thread interruption
     * @throws ExecutionException in case of any exception during DB query
     * @throws TimeoutException in case when timeout to execute DB query expires
     */
    @Then("data from `$sourceSqlQuery` executed against `$sourceDbKey` is equal to data from `$targetSqlQuery` executed"
            + " against `$targetDbKey` matching rows using keys:$keys")
    public void compareData(String sourceSqlQuery, String sourceDbKey, String targetSqlQuery, String targetDbKey,
            Set<String> keys) throws InterruptedException, ExecutionException, TimeoutException
    {
        JdbcTemplate sourceJdbcTemplate = getJdbcTemplate(sourceDbKey);
        JdbcTemplate targetJdbcTemplate = getJdbcTemplate(targetDbKey);
        QueriesStatistic queriesStatistic = new QueriesStatistic(sourceJdbcTemplate, targetJdbcTemplate);
        QueryStatistic source = queriesStatistic.getSource();
        source.setQuery(sourceSqlQuery);
        QueryStatistic target = queriesStatistic.getTarget();
        target.setQuery(targetSqlQuery);
        CompletableFuture<ListMultimap<Object, Map<String, Object>>> sourceData =
                createCompletableRequest(sourceJdbcTemplate, sourceSqlQuery, keys, source);
        CompletableFuture<ListMultimap<Object, Map<String, Object>>> targetData =
                createCompletableRequest(targetJdbcTemplate, targetSqlQuery, keys, target);

        List<List<EntryComparisonResult>> result = sourceData.thenCombine(targetData,
                compareQueryResults(queriesStatistic)).get(dbQueryTimeout.toMillis(),
                        TimeUnit.MILLISECONDS);

        verifyComparisonResult(queriesStatistic, result);
    }

    /**
     * The step waits until the <code>sqlQuery</code> returns the data equal to the examples table row
     * The order of columns is ignored
     * Actions performed in the step:
     * <ul>
     *     <li>run the <code>sqlQuery</code></li>
     *     <li>compares the response with the examples table row</li>
     *     <li>sleep for the <code>duration</code></li>
     *     <li>repeat all of the above until there are no more retry attempts or the response is equal
     *     to the examples table row</li>
     * </ul>
     * @param duration The time gap between two queries
     * @param retryTimes How many times request will be retried; duration/retryTimes=timeout between requests
     * @param sqlQuery SQL query to execute
     * @param dbKey Key identifying the database connection
     * @param table Rows to compare data against
     */
    @When("I wait for '$duration' duration retrying $retryTimes times while data from `$sqlQuery`"
            + " executed against `$dbKey` is equal to data from:$table")
    public void waitForDataAppearance(Duration duration, int retryTimes, String sqlQuery, String dbKey,
            List<Map<String, String>> table)
    {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        QueriesStatistic statistics = new QueriesStatistic(jdbcTemplate);
        ListMultimap<Object, Map<String, Object>> sourceData = hashMap(Set.of(), table);
        statistics.getTarget().setRowsQuantity(sourceData.size());

        DurationBasedWaiter waiter = new DurationBasedWaiter(new WaitMode(duration, retryTimes));
        List<List<EntryComparisonResult>> comparisonResult = waiter.wait(
            () -> {
                List<Map<String, Object>> data = jdbcTemplate.queryForList(sqlQuery);
                statistics.getSource().setRowsQuantity(data.size());
                ListMultimap<Object, Map<String, Object>> targetData = hashMap(Set.of(),
                        data.stream().map(
                            m -> m.entrySet()
                               .stream()
                               .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())))
                        )
                );
                return compareData(statistics, sourceData, targetData);
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
        verifyComparisonResult(statistics, filterPassedChecks(comparisonResult));
    }

    private void verifyComparisonResult(QueriesStatistic statistics, List<List<EntryComparisonResult>> result)
    {
        attachmentPublisher.publishAttachment("queries-statistics.ftl", Map.of("statistics", statistics),
                "Queries statistics");
        if (!softAssert.assertTrue("Query results are equal", result.isEmpty()))
        {
            attachmentPublisher.publishAttachment("/templates/maps-comparison-table.ftl", Map.of("results", result),
                    "Queries comparison result");
        }
    }

    /**
     * The step is designed to compare the data retrieved by SQL request against the examples table.
     * The order of columns is ignored.
     * Consider complete example:
     * <br>When I execute SQL query `${source}` against `$dbKey` and save result to STORY variable `data`
     * <br>Then `${data}` matching rows using `` from `$dbKey` is equal to data from:
     * <br>tables/data.table
     * @param data saved by step:
     * When I execute SQL query `$sqlQuery` against `$dbKey` and save result to $scopes variable `$data`"
     * @param keys comma-separated list of column's names to map resulting tables rows
     * (could be empty - all columns will be used)
     * @param dbKey key identifying the database connection used to get data
     * @param table rows to compare data against
     */
    @Then("`$data` matching rows using `$keys` from `$dbKey` is equal to data from:$table")
    public void compareData(List<Map<String, Object>> data, Set<String> keys, String dbKey,
            List<Map<String, String>> table)
    {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        QueriesStatistic statistics = new QueriesStatistic(jdbcTemplate);
        statistics.getTarget().setRowsQuantity(data.size());
        ListMultimap<Object, Map<String, Object>> targetData = hashMap(keys, data.stream()
                .map(m -> m.entrySet()
                           .stream()
                           .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())))));
        ListMultimap<Object, Map<String, Object>> sourceData = hashMap(keys, table);
        statistics.getSource().setRowsQuantity(sourceData.size());
        List<List<EntryComparisonResult>> result = compareData(statistics, sourceData, targetData);
        verifyComparisonResult(statistics, filterPassedChecks(result));
    }

    private BiFunction<ListMultimap<Object, Map<String, Object>>, ListMultimap<Object, Map<String, Object>>,
        List<List<EntryComparisonResult>>> compareQueryResults(QueriesStatistic queriesStatistic)
    {
        return (sourceQueryResult, targetQueryResult) -> compareData(queriesStatistic, sourceQueryResult,
                targetQueryResult);
    }

    private List<List<EntryComparisonResult>> compareData(QueriesStatistic queriesStatistic,
            ListMultimap<Object, Map<String, Object>> leftData, ListMultimap<Object, Map<String, Object>> rightData)
    {
        List<Pair<Map<String, Object>, Map<String, Object>>> comparison = new ArrayList<>();
        Stream.concat(leftData.keySet().stream(), rightData.keySet().stream()).distinct().forEach(key ->
        {
            List<Map<String, Object>> left = leftData.get(key);
            List<Map<String, Object>> right = rightData.get(key);
            int leftSize = left.size();
            int rightSize = right.size();
            int size = duplicateKeysStrategy.getTargetSize(leftSize, rightSize);
            for (int i = 0; i < size; i++)
            {
                Map<String, Object> leftValue = i < leftSize ? left.get(i) : Map.of();
                Map<String, Object> rightValue = i < rightSize ? right.get(i) : Map.of();
                comparison.add(Pair.of(leftValue, rightValue));
            }
        });
        queriesStatistic.getSource().setNoPair(comparison.stream()
                .map(Pair::getRight)
                .filter(Map::isEmpty)
                .count());
        queriesStatistic.getTarget().setNoPair(comparison.stream()
                .map(Pair::getLeft)
                .filter(Map::isEmpty)
                .count());
        List<List<EntryComparisonResult>> comparisonResult = comparison.stream()
                .parallel()
                .map(p -> ComparisonUtils.compareMaps(p.getLeft(), p.getRight()))
                .collect(Collectors.toList());
        List<List<EntryComparisonResult>> mismatchedRows = filterPassedChecks(comparisonResult);
        queriesStatistic.setMismatched(mismatchedRows.size());
        queriesStatistic.setTotalRows(comparisonResult.size());
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

    public static final class QueriesStatistic
    {
        private long totalRows;
        private long mismatched;
        private final QueryStatistic source;
        private final QueryStatistic target;

        private QueriesStatistic(JdbcTemplate targetJdbcTemplate)
        {
            source = new QueryStatistic();
            target = createQueryStatistic(targetJdbcTemplate);
        }

        private QueriesStatistic(JdbcTemplate sourceJdbcTemplate, JdbcTemplate targetJdbcTemplate)
        {
            source = createQueryStatistic(sourceJdbcTemplate);
            target = createQueryStatistic(targetJdbcTemplate);
        }

        private QueryStatistic createQueryStatistic(JdbcTemplate jdbcTemplate)
        {
            String url = ((DriverManagerDataSource) jdbcTemplate.getDataSource()).getUrl();
            return new QueryStatistic(url);
        }

        public long getMismatched()
        {
            return mismatched;
        }

        public long getMatched()
        {
            return totalRows - mismatched;
        }

        public void setMismatched(long mismatched)
        {
            this.mismatched = mismatched;
        }

        public long getTotalRows()
        {
            return totalRows;
        }

        public void setTotalRows(long totalRows)
        {
            this.totalRows = totalRows;
        }

        public QueryStatistic getSource()
        {
            return source;
        }

        public QueryStatistic getTarget()
        {
            return target;
        }
    }

    public static final class QueryStatistic
    {
        private final StopWatch stopwatch = new StopWatch();
        private String url;
        private long rowsQuantity;
        private String query;
        private long noPair;

        private QueryStatistic()
        {
        }

        private QueryStatistic(String url)
        {
            this.url = url;
        }

        public void start()
        {
            stopwatch.start();
        }

        public void end()
        {
            stopwatch.stop();
        }

        public String getExecutionTime()
        {
            return DurationFormatUtils.formatDurationHMS(stopwatch.getTime());
        }

        public String getQuery()
        {
            return query;
        }

        public void setQuery(String query)
        {
            this.query = query;
        }

        public long getRowsQuantity()
        {
            return rowsQuantity;
        }

        public void setRowsQuantity(long rowsQuantity)
        {
            this.rowsQuantity = rowsQuantity;
        }

        public long getNoPair()
        {
            return noPair;
        }

        public void setNoPair(long noPair)
        {
            this.noPair = noPair;
        }

        public String getUrl()
        {
            return url;
        }
    }
}
