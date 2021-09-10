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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.db.DataSourceManager;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.comparison.ComparisonUtils.EntryComparisonResult;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DatabaseStepsTests
{
    private static final Duration TWO_SECONDS = Duration.ofSeconds(2);

    private static final String ADMIN = "admin";

    private static final String DB_KEY = "dbKey";

    private static final String DB_KEY2 = "dbKey2";

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/test";

    private static final String EMPTY_TIME = "00:00:00.000";

    private static final String STATISTICS = "statistics";

    private static final String DATA_SOURCES_STATISTICS_TITLE = "Data sources statistics";
    private static final String DATA_SOURCES_STATISTICS_FTL = "data-sources-statistics.ftl";

    private static final String RESULTS = "results";

    private static final String DATA_SET_COMPARISON_FTL = "/templates/maps-comparison-table.ftl";
    private static final String DATA_SETS_COMPARISON_TITLE = "Data sets comparison";

    private static final String QUERY_RESULTS_ARE_EQUAL = "Query results are equal";

    private static final String COL1 = "col1";
    private static final String VAL1 = "val1";
    private static final String COL2 = "col2";
    private static final String VAL2 = "val2";
    private static final String COL3 = "col3";
    private static final String VAL3 = "val3";
    private static final List<Map<String, String>> TABLE = List.of(Map.of(COL1, VAL2));

    private static final String QUERY = "select col1 from table";
    private static final String QUERY2 = "select col1 from table2";
    private static final Set<String> KEYS = Set.of(COL1);

    private static final HashCode HASH1 = Hashing.murmur3_128().hashString(VAL1, StandardCharsets.UTF_8);

    private static final HashCode HASH2 = Hashing.murmur3_128().hashString(VAL2, StandardCharsets.UTF_8);

    private static final HashCode HASH3 = Hashing.murmur3_128().hashString(VAL3, StandardCharsets.UTF_8);

    private static final String LOG_EXECUTING_SQL_QUERY = "Executing SQL query: {}";

    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(DatabaseSteps.class);

    private static final String DURATION_PATTERN = "[0-2][0-9]:[0-5][0-9]:[01][0-9]\\.[0-9]{3}";

    private final IBddVariableContext bddVariableContext = mock(IBddVariableContext.class);

    private final ISoftAssert softAssert = mock(ISoftAssert.class);

    private final IAttachmentPublisher attachmentPublisher = mock(IAttachmentPublisher.class);

    private final DataSourceManager dataSourceManager = mock(DataSourceManager.class);

    @Mock private HashFunction hashFunction;
    @InjectMocks private final DatabaseSteps databaseSteps = new DatabaseSteps(dataSourceManager, bddVariableContext,
            attachmentPublisher, softAssert);

    @BeforeEach
    void beforeEach()
    {
        databaseSteps.setDiffLimit(3);
    }

    @Test
    void testExecuteSql()
    {
        mockQueryForList(QUERY, DB_KEY, List.of(Map.of(COL1, VAL1)));
        Set<VariableScope> variableScope = Set.of(VariableScope.SCENARIO);
        List<Map<String, Object>> singletonList = List.of(Map.of(COL1, VAL1));
        String variableName = "var";
        databaseSteps.executeSql(QUERY, DB_KEY, variableScope, variableName);
        verify(bddVariableContext).putVariable(variableScope, variableName, singletonList);
        assertThat(LOGGER.getLoggingEvents(), equalTo(List.of(info(LOG_EXECUTING_SQL_QUERY, QUERY))));
    }

    static Stream<Arguments> matchingResultLists()
    {
        return Stream.of(
                arguments(
                        DataSetComparisonRule.IS_EQUAL_TO,
                        List.of(Map.of(COL1, VAL1, COL2, VAL2, COL3, VAL3)),
                        List.of(Map.of(COL1, VAL1, COL2, VAL2, COL3, VAL3))
                ),
                arguments(
                        DataSetComparisonRule.CONTAINS,
                        List.of(Map.of(COL1, VAL1, COL2, VAL2, COL3, VAL3)),
                        List.of(Map.of(COL2, VAL2))
                )
        );
    }

    @ParameterizedTest
    @MethodSource("matchingResultLists")
    void shouldCompareQueriesResponsesAndDoNotPostDiffInCaseOfEqualData(DataSetComparisonRule comparisonRule,
            List<Map<String, Object>> leftResult, List<Map<String, Object>> rightResult)
            throws InterruptedException, ExecutionException, TimeoutException
    {
        mockQueryForList(QUERY, DB_KEY, leftResult);
        mockQueryForList(QUERY, DB_KEY2, rightResult);
        when(softAssert.assertTrue(comparisonRule.getAssertionDescription(), true)).thenReturn(true);
        configureTimeout();
        databaseSteps.setDuplicateKeysStrategy(DuplicateKeysStrategy.NOOP);
        databaseSteps.compareData(QUERY, DB_KEY, comparisonRule, QUERY, DB_KEY2, KEYS);
        verify(attachmentPublisher).publishAttachment(eq(DATA_SOURCES_STATISTICS_FTL), any(Map.class),
                eq(DATA_SOURCES_STATISTICS_TITLE));
        verify(hashFunction, times(2)).hashString(any(), eq(StandardCharsets.UTF_8));
    }

    @Test
    void shouldCompareQueriesAndUseAllColumnValuesIfUserDoesntSpecifyKeys() throws InterruptedException,
        ExecutionException, TimeoutException
    {
        Map<String, Object> firstResult = new HashMap<>();
        firstResult.put(COL1, VAL1);
        firstResult.put(COL2, null);
        firstResult.put(COL3, VAL3);

        Map<String, Object> secondResult = new HashMap<>();
        secondResult.put(COL1, VAL1);
        secondResult.put(COL2, null);
        secondResult.put(COL3, VAL3);

        mockQueryForList(QUERY, DB_KEY, List.of(firstResult));
        mockQueryForList(QUERY, DB_KEY2, List.of(secondResult));
        when(softAssert.assertTrue(QUERY_RESULTS_ARE_EQUAL, true)).thenReturn(true);
        ArgumentMatcher<CharSequence> matcher = s ->
        {
            String toHash = s.toString();
            return toHash.contains(VAL1) && toHash.contains(VAL3) && !toHash.contains(VAL2);
        };
        when(hashFunction.hashString(argThat(matcher), eq(StandardCharsets.UTF_8))).thenReturn(HASH1);
        configureTimeout();
        databaseSteps.setDuplicateKeysStrategy(DuplicateKeysStrategy.NOOP);
        databaseSteps.compareData(QUERY, DB_KEY, DataSetComparisonRule.IS_EQUAL_TO, QUERY, DB_KEY2, Set.of());
        verify(attachmentPublisher).publishAttachment(eq(DATA_SOURCES_STATISTICS_FTL),
                any(Map.class), eq(DATA_SOURCES_STATISTICS_TITLE));
        verify(hashFunction, times(2)).hashString(argThat(matcher), eq(StandardCharsets.UTF_8));
    }

    @Test
    void shouldExecuteSqlQuery()
    {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(dataSourceManager.getJdbcTemplate(DB_KEY)).thenReturn(jdbcTemplate);
        when(jdbcTemplate.update(QUERY)).thenReturn(1);
        databaseSteps.executeSql(QUERY, DB_KEY);
        assertThat(LOGGER.getLoggingEvents(), equalTo(List.of(
                info(LOG_EXECUTING_SQL_QUERY, QUERY),
                info("The number of affected rows: {}", 1)
        )));
    }

    @SuppressFBWarnings("ODR_OPEN_DATABASE_RESOURCE")
    @Test
    void shouldCompareSqlStatesSQLExceptionIsThrown() throws SQLException
    {
        String sqlState = "28000";
        DriverManagerDataSource dataSource = mockDataSourceRetrieval();
        SQLException exception = mock(SQLException.class);
        when(exception.getSQLState()).thenReturn(sqlState);
        doThrow(exception).when(dataSource).getConnection(ADMIN, ADMIN);
        databaseSteps.verifySqlState(DB_KEY, ADMIN, ADMIN, StringComparisonRule.IS_EQUAL_TO, sqlState);
        verifyShouldCompareSqlStates(sqlState);
    }

    @Test
    void shouldCompareSqlStatesSuccessConnection() throws SQLException
    {
        String sqlState = "00000";
        DriverManagerDataSource dataSource = mockDataSourceRetrieval();
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection(ADMIN, ADMIN)).thenReturn(connection);
        databaseSteps.verifySqlState(DB_KEY, ADMIN, ADMIN, StringComparisonRule.IS_EQUAL_TO, sqlState);
        verifyShouldCompareSqlStates(sqlState);
    }

    private void verifyShouldCompareSqlStates(String sqlState)
    {
        verify(softAssert).assertThat(eq("SQL state for connection"), eq(sqlState),
                argThat(arg -> arg.toString().equals(String.format("\"%s\"", sqlState))));
    }

    @Test
    void shouldThrowIllegalStateExceptionInCaseOfDataIntegrityViolationException()
    {
        DataIntegrityViolationException cause =
                new DataIntegrityViolationException("A result was returned when none was expected.");
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(dataSourceManager.getJdbcTemplate(DB_KEY)).thenReturn(jdbcTemplate);
        when(jdbcTemplate.update(QUERY)).thenThrow(cause);

        IllegalStateException actual = assertThrows(IllegalStateException.class,
            () -> databaseSteps.executeSql(QUERY, DB_KEY));
        assertEquals(cause, actual.getCause());
        assertEquals(actual.getMessage(), "Exception occurred during query execution.\n"
                + "If you are trying execute SELECT query consider using step:"
                + "When I execute SQL query '$sqlQuery' and save the result to the $scopes variable '$variableName'");
        assertThat(LOGGER.getLoggingEvents(), equalTo(List.of(info(LOG_EXECUTING_SQL_QUERY, QUERY))));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldCompareQueriesResponsesAndPostDiffTable() throws InterruptedException,
            ExecutionException, TimeoutException
    {
        List<Map<String, Object>> result = List.of(Map.of(COL1, VAL1), Map.of(COL1, VAL1), Map.of(COL1, VAL3));
        mockQueryForList(QUERY, DB_KEY, result);
        mockQueryForList(QUERY2, DB_KEY2, List.of(Map.of(COL1, VAL2)));
        when(softAssert.assertTrue(QUERY_RESULTS_ARE_EQUAL, false)).thenReturn(false);
        mockHashing();
        configureTimeout();
        databaseSteps.setDuplicateKeysStrategy(DuplicateKeysStrategy.DISTINCT);
        databaseSteps.compareData(QUERY, DB_KEY, DataSetComparisonRule.IS_EQUAL_TO, QUERY2, DB_KEY2, KEYS);
        verify(attachmentPublisher).publishAttachment(eq(DATA_SOURCES_STATISTICS_FTL),
                argThat(r -> {
                    DataSourceStatistics statistics = ((Map<String, DataSourceStatistics>) r).get(STATISTICS);
                    QueryStatistic right = statistics.getRight();
                    QueryStatistic left = statistics.getLeft();
                    return 3 == statistics.getMismatched()
                            && 3 == statistics.getTotalRows()
                            && right.getExecutionTime().matches(DURATION_PATTERN)
                            && left.getExecutionTime().matches(DURATION_PATTERN)
                            && QUERY2.equals(right.getQuery())
                            && QUERY.equals(left.getQuery())
                            && 1 == right.getRowsQuantity()
                            && 3 == left.getRowsQuantity()
                            && 0 == statistics.getMatched()
                            && 2 == left.getNoPair()
                            && 1 == right.getNoPair()
                            && DB_URL.equals(left.getUrl())
                            && DB_URL.equals(right.getUrl());
                }), eq(DATA_SOURCES_STATISTICS_TITLE));
        verify(attachmentPublisher).publishAttachment(eq(DATA_SET_COMPARISON_FTL), argThat(r ->
            ((Map<String, List<List<EntryComparisonResult>>>) r).get(RESULTS).size() == 3),
                eq(DATA_SETS_COMPARISON_TITLE));
    }

    private void mockHashing()
    {
        doAnswer((Answer<HashCode>) invocation -> {
            Object argument = invocation.getArgument(0);
            if (VAL1.equals(argument))
            {
                return HASH1;
            }
            else if (VAL2.equals(argument))
            {
                return HASH2;
            }
            return HASH3;
        }).when(hashFunction).hashString(any(), eq(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldLimitDiffTable() throws InterruptedException, ExecutionException, TimeoutException
    {
        databaseSteps.setDiffLimit(1);
        databaseSteps.setDuplicateKeysStrategy(DuplicateKeysStrategy.NOOP);
        List<Map<String, Object>> result = List.of(Map.of(COL1, VAL1), Map.of(COL1, VAL3));
        mockQueryForList(QUERY, DB_KEY, result);
        mockQueryForList(QUERY, DB_KEY2, List.of(Map.of(COL1, VAL2)));
        when(softAssert.assertTrue(QUERY_RESULTS_ARE_EQUAL, false)).thenReturn(false);
        mockHashing();
        configureTimeout();
        databaseSteps.compareData(QUERY, DB_KEY, DataSetComparisonRule.IS_EQUAL_TO, QUERY, DB_KEY2, KEYS);
        verify(attachmentPublisher).publishAttachment(eq(DATA_SET_COMPARISON_FTL), argThat(r ->
            ((Map<String, List<List<EntryComparisonResult>>>) r).get(RESULTS).size() == 1),
                eq(DATA_SETS_COMPARISON_TITLE));
    }

    @Test
    void shouldThrowTimeoutExceptionIfQueryTakesTooMuchTime()
    {
        databaseSteps.setDbQueryTimeout(Duration.ofNanos(0));
        mockDataSourceRetrieval();
        assertThrows(TimeoutException.class,
            () -> databaseSteps.compareData(QUERY, DB_KEY, DataSetComparisonRule.IS_EQUAL_TO, QUERY, DB_KEY, KEYS));
        verifyNoInteractions(attachmentPublisher, softAssert);
    }

    static Stream<Arguments> matchingDataSets()
    {
        return Stream.of(
                arguments(
                        DataSetComparisonRule.IS_EQUAL_TO,
                        List.of(Map.of(COL1, VAL1)),
                        List.of(Map.of(COL1, VAL1))
                ),
                arguments(
                        DataSetComparisonRule.CONTAINS,
                        List.of(Map.of(COL1, VAL1)),
                        List.of(Map.of(COL1, VAL1))
                ),
                arguments(
                        DataSetComparisonRule.IS_EQUAL_TO,
                        List.of(Map.of(COL1, VAL1, COL2, VAL2)),
                        List.of(Map.of(COL2, VAL2, COL1, VAL1))
                ),
                arguments(
                        DataSetComparisonRule.CONTAINS,
                        List.of(Map.of(COL1, VAL1), Map.of(COL1, VAL2)),
                        List.of(Map.of(COL1, VAL2))
                ),
                arguments(
                        DataSetComparisonRule.IS_EQUAL_TO,
                        List.of(Collections.singletonMap(COL1, null)),
                        List.of(Collections.singletonMap(COL2, null))
                )
        );
    }

    @ParameterizedTest
    @MethodSource("matchingDataSets")
    void shouldCompareDataVsExamplesTableAndNotPostReportIfDataEqual(DataSetComparisonRule comparisonRule,
            List<Map<String, Object>> leftDataSet, List<Map<String, String>> rightDataSet)
    {
        when(softAssert.assertTrue(comparisonRule.getAssertionDescription(), true)).thenReturn(true);
        databaseSteps.setDuplicateKeysStrategy(DuplicateKeysStrategy.NOOP);
        mockHashing();
        mockDataSource();
        databaseSteps.compareData(leftDataSet, Set.of(), DB_KEY, comparisonRule, rightDataSet);
        verify(attachmentPublisher, never()).publishAttachment(eq(DATA_SET_COMPARISON_FTL), any(), any());
        verifyQueryStatisticsPublishing(comparisonRule, leftDataSet, rightDataSet, 0, 1);
    }

    static Stream<Arguments> notMatchingDataSets()
    {
        return Stream.of(
                arguments(
                        DataSetComparisonRule.IS_EQUAL_TO,
                        List.of(Map.of(COL1, VAL1, COL2, VAL1)),
                        List.of(Map.of(COL1, VAL1, COL2, VAL2))
                ),
                arguments(
                        DataSetComparisonRule.CONTAINS,
                        List.of(Map.of(COL1, VAL1, COL2, VAL1)),
                        List.of(Map.of(COL1, VAL1, COL2, VAL2))
                ),
                arguments(
                        DataSetComparisonRule.CONTAINS,
                        List.of(Map.of(COL1, VAL1, COL2, VAL1), Map.of(COL1, VAL1, COL2, VAL3)),
                        List.of(Map.of(COL1, VAL1, COL2, VAL2))
                )
        );
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("notMatchingDataSets")
    void shouldCompareDataVsExamplesTableAndPostReportFailedChecks(DataSetComparisonRule comparisonRule,
            List<Map<String, Object>> leftDataSet, List<Map<String, String>> rightDataSet)
    {
        when(softAssert.assertTrue(comparisonRule.getAssertionDescription(), false)).thenReturn(false);
        databaseSteps.setDuplicateKeysStrategy(DuplicateKeysStrategy.NOOP);
        mockHashing();
        mockDataSource();
        databaseSteps.compareData(leftDataSet, KEYS, DB_KEY, comparisonRule, rightDataSet);
        var resultsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(attachmentPublisher).publishAttachment(eq(DATA_SET_COMPARISON_FTL), resultsCaptor.capture(),
                eq(DATA_SETS_COMPARISON_TITLE));

        List<List<EntryComparisonResult>> results = (List<List<EntryComparisonResult>>) ((Map<?, ?>) resultsCaptor
                .getValue()).get(RESULTS);
        assertEquals(1, results.size());
        List<EntryComparisonResult> firstRowResults = results.get(0);
        assertEquals(2, firstRowResults.size());
        EntryComparisonResult result = firstRowResults.stream().filter(r -> COL2.equals(r.getKey())).findFirst().get();
        assertEquals(VAL1, result.getLeft());
        assertEquals(VAL2, result.getRight());
        assertFalse(result.isPassed());

        verifyQueryStatisticsPublishing(comparisonRule, leftDataSet, rightDataSet, 1, 0);
    }

    @SuppressWarnings("unchecked")
    private void verifyQueryStatisticsPublishing(DataSetComparisonRule comparisonRule,
            List<Map<String, Object>> leftDataSet, List<Map<String, String>> rightDataSet, int expectedMismatched,
            int expectedMatched)
    {
        var statisticsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(attachmentPublisher).publishAttachment(eq(DATA_SOURCES_STATISTICS_FTL), statisticsCaptor.capture(),
                eq(DATA_SOURCES_STATISTICS_TITLE));
        DataSourceStatistics statistics = ((Map<String, DataSourceStatistics>) statisticsCaptor.getValue()).get(
                STATISTICS);
        assertEquals(expectedMismatched, statistics.getMismatched());
        assertEquals(1, statistics.getTotalRows());
        assertEquals(expectedMatched, statistics.getMatched());
        assertQueryStatistic(statistics.getLeft(), leftDataSet.size(),
                comparisonRule == DataSetComparisonRule.CONTAINS ? null : 0L, DB_URL);
        assertQueryStatistic(statistics.getRight(), rightDataSet.size(), 0L, null);
    }

    private void assertQueryStatistic(QueryStatistic actual, int expectedRowsQuantity, Long expectedNoPair,
            String expectedUrl)
    {
        assertEquals(EMPTY_TIME, actual.getExecutionTime());
        assertNull(actual.getQuery());
        assertEquals(expectedRowsQuantity, actual.getRowsQuantity());
        assertEquals(expectedNoPair, actual.getNoPair());
        assertEquals(expectedUrl, actual.getUrl());
    }

    @Test
    void testWaitUntilQueryReturnedDataEqualToTable()
    {
        databaseSteps.setDuplicateKeysStrategy(DuplicateKeysStrategy.NOOP);
        mockQueryForList(QUERY, DB_KEY, List.of(Map.of(COL1, VAL2)));
        when(softAssert.assertTrue(QUERY_RESULTS_ARE_EQUAL, true)).thenReturn(true);
        databaseSteps.waitForDataAppearance(TWO_SECONDS, 10, QUERY, DB_KEY, DataSetComparisonRule.IS_EQUAL_TO, TABLE);
        verify(attachmentPublisher).publishAttachment(eq(DATA_SOURCES_STATISTICS_FTL),
                any(Map.class), eq(DATA_SOURCES_STATISTICS_TITLE));
        verify(softAssert).assertTrue(QUERY_RESULTS_ARE_EQUAL, true);
    }

    @Test
    void testWaitTwiceUntilQueryReturnedDataEqualToTable()
    {
        databaseSteps.setDuplicateKeysStrategy(DuplicateKeysStrategy.NOOP);

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(dataSourceManager.getJdbcTemplate(DB_KEY)).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(QUERY))
                .thenReturn(List.of(Map.of(COL1, VAL1)))
                .thenReturn(List.of(Map.of(COL1, VAL2)));
        DriverManagerDataSource dataSource = mock(DriverManagerDataSource.class);
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getUrl()).thenReturn(DB_URL);

        when(softAssert.assertTrue(QUERY_RESULTS_ARE_EQUAL, true)).thenReturn(true);
        databaseSteps.waitForDataAppearance(TWO_SECONDS, 10, QUERY, DB_KEY, DataSetComparisonRule.IS_EQUAL_TO, TABLE);
        verify(attachmentPublisher).publishAttachment(eq(DATA_SOURCES_STATISTICS_FTL),
                any(Map.class), eq(DATA_SOURCES_STATISTICS_TITLE));
        verify(softAssert).assertTrue(QUERY_RESULTS_ARE_EQUAL, true);
    }

    @Test
    void testWaitUntilQueryReturnedDataEqualToTableFailed()
    {
        databaseSteps.setDuplicateKeysStrategy(DuplicateKeysStrategy.NOOP);

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(dataSourceManager.getJdbcTemplate(DB_KEY)).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(QUERY))
                .thenReturn(List.of(Map.of(COL1, VAL1)))
                .thenReturn(List.of(Map.of(COL1, VAL3)));
        DriverManagerDataSource dataSource = mock(DriverManagerDataSource.class);
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getUrl()).thenReturn(DB_URL);

        databaseSteps.waitForDataAppearance(Duration.ofSeconds(2), 2, QUERY, DB_KEY, DataSetComparisonRule.IS_EQUAL_TO,
                TABLE);
        String logMessage = "SQL result data is not equal to expected data in {} records";
        assertThat(LOGGER.getLoggingEvents(), equalTo(List.of(info(logMessage, 1), info(logMessage, 1))));
        verify(attachmentPublisher).publishAttachment(eq(DATA_SOURCES_STATISTICS_FTL),
                any(Map.class), eq(DATA_SOURCES_STATISTICS_TITLE));
        verify(softAssert).assertTrue(QUERY_RESULTS_ARE_EQUAL, false);
        verify(attachmentPublisher).publishAttachment(eq(DATA_SET_COMPARISON_FTL), argThat(r -> {
            @SuppressWarnings("unchecked")
            List<List<EntryComparisonResult>> results = (List<List<EntryComparisonResult>>) ((Map<?, ?>) r)
                    .get(RESULTS);
            List<EntryComparisonResult> firstRowResults = results.get(0);
            EntryComparisonResult result = firstRowResults.get(0);
            return 1 == results.size()
                && 1 == firstRowResults.size()
                && VAL2.equals(result.getRight())
                && VAL3.equals(result.getLeft())
                && !result.isPassed();
        }),
               eq(DATA_SETS_COMPARISON_TITLE));
    }

    @Test
    void shouldThrowExceptionForDataSetsWithDifferentHeaders()
    {
        Set<VariableScope> variableScope = Set.of(VariableScope.SCENARIO);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> databaseSteps.joinDataSets(List.of(Map.of(COL1, VAL1)),
                List.of(Map.of(COL2, VAL1)), variableScope, VAL1));
        assertEquals("Data sets should have same columns;\nLeft:  [col1]\nRight: [col2]",
                exception.getMessage());
    }

    static Stream<Arguments> dataSetsProvider()
    {
        return Stream.of(
                Arguments.of(List.of(),                   List.of(Map.of(COL1, VAL1))),
                Arguments.of(List.of(Map.of(COL1, VAL1)), List.of()));
    }

    @ParameterizedTest
    @MethodSource("dataSetsProvider")
    void shouldNotVerifyHeadersIfOneOfDataSetsEmpty(List<Map<String, Object>> left, List<Map<String, Object>> right)
    {
        Set<VariableScope> variableScopes = Set.of(VariableScope.SCENARIO);
        databaseSteps.joinDataSets(left, right, variableScopes, VAL1);
        verify(bddVariableContext).putVariable(variableScopes, VAL1, List.of(Map.of(COL1, VAL1)));
    }

    @Test
    void shouldMergeDataSets()
    {
        Set<VariableScope> variableScopes = Set.of(VariableScope.SCENARIO);
        Map<String, Object> row = Map.of(COL1, VAL1);
        List<Map<String, Object>> left = List.of(row);
        List<Map<String, Object>> right = List.of(row);
        databaseSteps.joinDataSets(left, right, variableScopes, VAL1);
        verify(bddVariableContext).putVariable(variableScopes, VAL1, List.of(row, row));
    }

    private void mockDataSource()
    {
        DriverManagerDataSource dataSource = mockDataSourceRetrieval();
        when(dataSource.getUrl()).thenReturn(DB_URL);
    }

    private DriverManagerDataSource mockDataSourceRetrieval()
    {
        DriverManagerDataSource dataSource = mock(DriverManagerDataSource.class);
        String dbKey = DB_KEY;
        lenient().when(dataSourceManager.getDataSource(dbKey)).thenReturn(dataSource);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        lenient().when(dataSourceManager.getJdbcTemplate(dbKey)).thenReturn(jdbcTemplate);
        lenient().when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        return dataSource;
    }

    private void mockQueryForList(String query, String dbKey, List<Map<String, Object>> result)
    {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(dataSourceManager.getJdbcTemplate(dbKey)).thenReturn(jdbcTemplate);
        DriverManagerDataSource dataSource = mock(DriverManagerDataSource.class);
        lenient().when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        lenient().when(dataSource.getUrl()).thenReturn(DB_URL);
        when(jdbcTemplate.queryForList(query)).thenReturn(result);
    }

    private void configureTimeout()
    {
        databaseSteps.setDbQueryTimeout(Duration.ofSeconds(20));
    }
}
