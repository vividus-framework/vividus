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

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.util.RowsCollector;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.comparison.ComparisonUtils;
import org.vividus.util.comparison.ComparisonUtils.EntryComparisonResult;
import org.vividus.util.wait.WaitMode;
import org.vividus.util.wait.Waiter;

public class DatabaseSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSteps.class);
    private final IBddVariableContext bddVariableContext;
    private final IAttachmentPublisher attachmentPublisher;
    private final ISoftAssert softAssert;
    private HashFunction hashFunction;
    private Map<String, DriverManagerDataSource> dataSources;
    private Duration dbQueryTimeout;
    private RowsCollector rowsCollector;
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
        try (Connection connection = dataSources.get(dbKey).getConnection(username, password))
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
     *     <li>executes provided SQL queries against databases by the provided keys</li>
     *     <li>compares queries results </li>
     * </ul>
     * To limit DB query execution time you could use property:
     * <b>db.query-timeout</b>
     * Default timeout is 30 minutes
     * To limit queries comparison result output you could use:
     * <b>db.diff-limit</b>
     * Default value is 100
     * To workaround repeating rows you could use:
     * <b>db.rows-collector</b>
     * Possible values (NOOP - default, DISTINCT)
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
        CompletableFuture<Map<Object, Map<String, Object>>> sourceData =
                createCompletableRequest(sourceJdbcTemplate, sourceSqlQuery, keys, source);
        CompletableFuture<Map<Object, Map<String, Object>>> targetData =
                createCompletableRequest(targetJdbcTemplate, targetSqlQuery, keys, target);

        List<List<EntryComparisonResult>> result = sourceData.thenCombine(targetData,
                compareQueryResults(queriesStatistic)).get(dbQueryTimeout.toMillis(),
                        TimeUnit.MILLISECONDS);

        verifyComparisonResult(queriesStatistic, result);
    }

    /**
     * Step waits until data received from SQL request is equal to data from examples table for some duration.
     * Actions performed in the step:
     * <ul>
     *     <li>executes provided SQL query against database</li>
     *     <li>compares data received from SQL request against examples table</li>
     *     <li>sleeps during calculated part of specified duration</li>
     *     <li>repeats previous actions if data was not equal and seconds timeout not expired</li>
     * </ul>
     * @param duration Time duration to wait
     * @param retryTimes How many times request will be retried; duration/retryTimes=timeout between requests
     * @param sqlQuery SQL query to execute
     * @param dbKey Key identifying the database connection
     * @param table Rows to compare data against
     * @throws Exception In case of any exception
     */
    @When("I wait for '$duration' duration retrying $retryTimes times while data from `$sqlQuery`"
            + " executed against `$dbKey` is equal to data from:$table")
    public void waitForDataAppearance(Duration duration, int retryTimes, String sqlQuery, String dbKey,
            ExamplesTable table) throws Exception
    {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        QueriesStatistic statistics = new QueriesStatistic(jdbcTemplate, jdbcTemplate);
        Map<Object, Map<String, Object>> sourceData = hashMap(Set.of(), table.getRows());
        statistics.getTarget().setRowsQuantity(sourceData.size());

        Waiter waiter = new Waiter(new WaitMode(duration, retryTimes));
        List<List<EntryComparisonResult>> comparisonResult = waiter.wait(
            () -> {
                List<Map<String, Object>> data = jdbcTemplate.queryForList(sqlQuery);
                statistics.getSource().setRowsQuantity(data.size());
                Map<Object, Map<String, Object>> targetData = hashMap(Set.of(),
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
     * Step designed to compare data received from SQL request against examples table;
     * Examples table could be build for example via GENERATE_FROM_CSV table transformer.
     * Consider complete example:
     * <br>When I execute SQL query `${source}` against `$dbKey` and save result to STORY variable `data`
     * <br>Then `${data}` matching rows using `` from `$dbKey` is equal to data from:
     * <br>{transformer=GENERATE_FROM_CSV, csvPath=${path}/source.csv}
     * @param data saved by step:
     * When I execute SQL query `$sqlQuery` against `$dbKey` and save result to $scopes variable `$data`"
     * @param keys comma-separated list of column's names to map resulting tables rows
     * (could be empty - all columns will be used)
     * @param dbKey key identifying the database connection used to get data
     * @param table rows to compare data against
     */
    @Then("`$data` matching rows using `$keys` from `$dbKey` is equal to data from:$table")
    public void compareData(List<Map<String, Object>> data, Set<String> keys, String dbKey, ExamplesTable table)
    {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        QueriesStatistic statistics = new QueriesStatistic(jdbcTemplate, jdbcTemplate);
        statistics.getTarget().setRowsQuantity(data.size());
        Map<Object, Map<String, Object>> targetData = hashMap(keys, data.stream()
                .map(m -> m.entrySet()
                           .stream()
                           .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())))));
        Map<Object, Map<String, Object>> sourceData = hashMap(keys, table.getRows());
        statistics.getSource().setRowsQuantity(sourceData.size());
        List<List<EntryComparisonResult>> result = compareData(statistics, sourceData, targetData);
        verifyComparisonResult(statistics, filterPassedChecks(result));
    }

    private BiFunction<Map<Object, Map<String, Object>>, Map<Object, Map<String, Object>>,
        List<List<EntryComparisonResult>>> compareQueryResults(QueriesStatistic queriesStatistic)
    {
        return (sourceQueryResult, targetQueryResult) -> compareData(queriesStatistic, sourceQueryResult,
                targetQueryResult);
    }

    private List<List<EntryComparisonResult>> compareData(QueriesStatistic queriesStatistic,
            Map<Object, Map<String, Object>> sourceData, Map<Object, Map<String, Object>> targetData)
    {
        List<Pair<Map<String, Object>, Map<String, Object>>> comparison = new ArrayList<>();
        sourceData.entrySet().stream()
            .filter(e -> {
                Map<String, Object> targetEntry = targetData.remove(e.getKey());
                if (null != targetEntry)
                {
                    comparison.add(Pair.of(e.getValue(), targetEntry));
                    return false;
                }
                return true;
            })
            .forEach(e -> comparison.add(Pair.of(e.getValue(), Map.of())));
        queriesStatistic.getSource().setNoPair(comparison.stream()
                .map(Pair::getValue)
                .filter(Map::isEmpty)
                .count());
        queriesStatistic.getTarget().setNoPair(targetData.size());
        targetData.values().forEach(value -> comparison.add(Pair.of(Map.of(), value)));
        List<List<EntryComparisonResult>> comparisonResult = comparison.stream()
                .parallel()
                .map(p -> ComparisonUtils.compareMaps(p.getLeft(), p.getRight()))
                .collect(Collectors.toList());
        long totalRows = comparisonResult.size();
        List<List<EntryComparisonResult>> mismatchedRows = filterPassedChecks(comparisonResult);
        queriesStatistic.setMismatched(mismatchedRows.size());
        queriesStatistic.setTotalRows(totalRows);
        return mismatchedRows.size() > diffLimit ? mismatchedRows.subList(0, diffLimit) : mismatchedRows;
    }

    private List<List<EntryComparisonResult>> filterPassedChecks(List<List<EntryComparisonResult>> comparisonResult)
    {
        return comparisonResult.stream()
                        .filter(r -> !r.stream().allMatch(EntryComparisonResult::isPassed))
                        .collect(Collectors.toList());
    }

    private CompletableFuture<Map<Object, Map<String, Object>>> createCompletableRequest(JdbcTemplate jdbcTemplate,
            String sqlRequest, Set<String> keys, QueryStatistic statistics)
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
    private Map<Object, Map<String, Object>> hashMap(Set<String> keys, List<?> r)
    {
        return hashMap(keys, ((List<Map<String, Object>>) r).stream());
    }

    private Map<Object, Map<String, Object>> hashMap(Set<String> keys, Stream<Map<String, Object>> data)
    {
        return data.map(row -> Pair.of(hash(keys, row), row))
                   .collect(rowsCollector.get());
    }

    private HashCode hash(Set<String> keys, Map<String, Object> map)
    {
        return hashFunction.hashString((keys.isEmpty() ? map.keySet() : keys)
                           .stream()
                           .map(map::get)
                           .filter(Objects::nonNull)
                           .map(Object::toString)
                           .collect(Collectors.joining()), StandardCharsets.UTF_8);
    }

    private JdbcTemplate getJdbcTemplate(String dbKey)
    {
        return jdbcTemplates.computeIfAbsent(dbKey, key -> new JdbcTemplate(dataSources.get(key)));
    }

    public void setDataSources(Map<String, DriverManagerDataSource> dataSources)
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

    public void setRowsCollector(RowsCollector rowsCollector)
    {
        this.rowsCollector = rowsCollector;
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

        private QueriesStatistic(JdbcTemplate sourceJdbcTemplate, JdbcTemplate targetJdbcTemplate)
        {
            String sourceUrl = ((DriverManagerDataSource) sourceJdbcTemplate.getDataSource()).getUrl();
            String targetUrl = ((DriverManagerDataSource) targetJdbcTemplate.getDataSource()).getUrl();
            source = new QueryStatistic(sourceUrl);
            target = new QueryStatistic(targetUrl);
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
        private final String url;
        private long rowsQuantity;
        private String query;
        private long noPair;

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
