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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Collections;
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
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.comparison.ComparisonUtils.EntryComparisonResult;
import org.vividus.util.property.PropertyMappedCollection;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
@SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION")
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

    private static final String MISSING_DB_CONFIG_ERROR = "Database connection with key '%s' is not configured in "
            + "properties";

    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(DatabaseSteps.class);

    private static final String DURATION_PATTERN = "[0-2][0-9]:[0-5][0-9]:[01][0-9]\\.[0-9]{3}";

    private final IBddVariableContext bddVariableContext = mock(IBddVariableContext.class);

    private final ISoftAssert softAssert = mock(ISoftAssert.class);

    private final IAttachmentPublisher attachmentPublisher = mock(IAttachmentPublisher.class);

    @Mock private PropertyMappedCollection<DriverManagerDataSource> dataSources;
    @Mock private HashFunction hashFunction;
    @InjectMocks private final DatabaseSteps databaseSteps = new DatabaseSteps(bddVariableContext, attachmentPublisher,
            softAssert);

    @BeforeEach
    void beforeEach()
    {
        databaseSteps.setDiffLimit(3);
    }

    @Test
    void testExecuteSql() throws SQLException
    {
        mockDataSource(QUERY, DB_KEY, mockResultSet(COL1, VAL1));
        Set<VariableScope> variableScope = Set.of(VariableScope.SCENARIO);
        List<Map<String, Object>> singletonList = Collections.singletonList(Collections.singletonMap(COL1, VAL1));
        String variableName = "var";
        databaseSteps.executeSql(QUERY, DB_KEY, variableScope, variableName);
        verify(bddVariableContext).putVariable(variableScope, variableName, singletonList);
    }

    static Stream<Arguments> matchingResultSets() throws SQLException
    {
        return Stream.of(
                arguments(
                        DataSetComparisonRule.IS_EQUAL_TO,
                        mockResultSet(COL1, VAL1, COL2, VAL2, COL3, VAL3),
                        mockResultSet(COL1, VAL1, COL2, VAL2, COL3, VAL3)
                ),
                arguments(
                        DataSetComparisonRule.CONTAINS,
                        mockResultSet(COL1, VAL1, COL2, VAL2, COL3, VAL3),
                        mockResultSet(COL2, VAL2)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("matchingResultSets")
    void shouldCompareQueriesResponsesAndDontPostDiffInCaseOfEqualData(DataSetComparisonRule comparisonRule,
            ResultSet leftResultSet, ResultSet rightResultSet)
            throws InterruptedException, ExecutionException, TimeoutException, SQLException
    {
        mockDataSource(QUERY, DB_KEY, leftResultSet);
        mockDataSource(QUERY, DB_KEY2, rightResultSet);
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
        ExecutionException, TimeoutException, SQLException
    {
        mockDataSource(QUERY, DB_KEY, mockResultSet(COL1, VAL1, COL2, null, COL3, VAL3));
        mockDataSource(QUERY, DB_KEY2, mockResultSet(COL1, VAL1, COL2, null, COL3, VAL3));
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
    void shouldExecuteSqlQuery() throws SQLException
    {
        Statement stmt = mock(Statement.class);
        when(stmt.executeUpdate(QUERY)).thenReturn(1);
        Connection con = mock(Connection.class);
        when(con.createStatement()).thenReturn(stmt);
        DriverManagerDataSource dataSource = mockDataSourceRetrieval();
        when(dataSource.getConnection()).thenReturn(con);
        databaseSteps.executeSql(QUERY, DB_KEY);
        assertThat(LOGGER.getLoggingEvents(), equalTo(List.of(info("Executed query: {}\nAffected rows:{}", QUERY, 1))));
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
    void shouldThrowIllegalStateExceptionInCaseOfDataIntegrityViolationException() throws SQLException
    {
        DataIntegrityViolationException cause =
                new DataIntegrityViolationException("A result was returned when none was expected.");
        Statement stmt = mock(Statement.class);
        when(stmt.executeUpdate(QUERY)).thenThrow(cause);
        Connection con = mock(Connection.class);
        when(con.createStatement()).thenReturn(stmt);
        DriverManagerDataSource dataSource = mockDataSourceRetrieval();
        when(dataSource.getConnection()).thenReturn(con);
        IllegalStateException actual = assertThrows(IllegalStateException.class,
            () -> databaseSteps.executeSql(QUERY, DB_KEY));
        assertEquals(cause, actual.getCause());
        assertEquals(actual.getMessage(), "Exception occured during query execution.\n"
                + "If you are trying execute SELECT query consider using step:"
                + "When I execute SQL query '$sqlQuery' and save the result to the $scopes variable '$variableName'");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldCompareQueriesResponsesAndPostDiffTable() throws InterruptedException,
        ExecutionException, TimeoutException, SQLException
    {
        ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
        when(rsmd.getColumnCount()).thenReturn(1);
        when(rsmd.getColumnLabel(1)).thenReturn(COL1);
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(rs.getMetaData()).thenReturn(rsmd);
        when(rs.getObject(1)).thenReturn(VAL1).thenReturn(VAL1).thenReturn(VAL3);
        DriverManagerDataSource dataSource1 = mockDataSource(QUERY, DB_KEY, rs);
        when(dataSource1.getUrl()).thenReturn(DB_URL);
        DriverManagerDataSource dataSource2 = mockDataSource(QUERY2, DB_KEY2, mockResultSet(COL1, VAL2));
        when(dataSource2.getUrl()).thenReturn(DB_URL);
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
    void shouldLimitDiffTable() throws InterruptedException, ExecutionException, TimeoutException, SQLException
    {
        databaseSteps.setDiffLimit(1);
        databaseSteps.setDuplicateKeysStrategy(DuplicateKeysStrategy.NOOP);
        ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
        when(rsmd.getColumnCount()).thenReturn(1);
        when(rsmd.getColumnLabel(1)).thenReturn(COL1);
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(rs.getMetaData()).thenReturn(rsmd);
        when(rs.getObject(1)).thenReturn(VAL1).thenReturn(VAL3);
        mockDataSource(QUERY, DB_KEY, rs);
        mockDataSource(QUERY, DB_KEY2, mockResultSet(COL1, VAL2));
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
    void testWaitUntilQueryReturnedDataEqualToTable() throws SQLException
    {
        databaseSteps.setDuplicateKeysStrategy(DuplicateKeysStrategy.NOOP);
        mockDataSource(QUERY, DB_KEY, mockResultSet(COL1, VAL2));
        when(softAssert.assertTrue(QUERY_RESULTS_ARE_EQUAL, true)).thenReturn(true);
        databaseSteps.waitForDataAppearance(TWO_SECONDS, 10, QUERY, DB_KEY, DataSetComparisonRule.IS_EQUAL_TO, TABLE);
        verify(attachmentPublisher).publishAttachment(eq(DATA_SOURCES_STATISTICS_FTL),
                any(Map.class), eq(DATA_SOURCES_STATISTICS_TITLE));
        verify(softAssert).assertTrue(QUERY_RESULTS_ARE_EQUAL, true);
    }

    @Test
    void testWaitTwiceUntilQueryReturnedDataEqualToTable() throws SQLException
    {
        databaseSteps.setDuplicateKeysStrategy(DuplicateKeysStrategy.NOOP);

        ResultSet rsFirst = mockResultSet(COL1, VAL1);
        ResultSet rsSecond = mockResultSet(COL1, VAL2);
        Statement stmt = mock(Statement.class);
        when(stmt.executeQuery(QUERY)).thenReturn(rsFirst).thenReturn(rsSecond);
        Connection con = mock(Connection.class);
        when(con.createStatement()).thenReturn(stmt);
        DriverManagerDataSource dataSource = mockDataSourceRetrieval();
        when(dataSource.getConnection()).thenReturn(con);

        when(softAssert.assertTrue(QUERY_RESULTS_ARE_EQUAL, true)).thenReturn(true);
        databaseSteps.waitForDataAppearance(TWO_SECONDS, 10, QUERY, DB_KEY, DataSetComparisonRule.IS_EQUAL_TO, TABLE);
        verify(attachmentPublisher).publishAttachment(eq(DATA_SOURCES_STATISTICS_FTL),
                any(Map.class), eq(DATA_SOURCES_STATISTICS_TITLE));
        verify(softAssert).assertTrue(QUERY_RESULTS_ARE_EQUAL, true);
    }

    @Test
    void testWaitUntilQueryReturnedDataEqualToTableFailed() throws SQLException
    {
        databaseSteps.setDuplicateKeysStrategy(DuplicateKeysStrategy.NOOP);

        ResultSet rsFirst = mockResultSet(COL1, VAL1);
        ResultSet rsSecond = mockResultSet(COL1, VAL3);
        Statement stmt = mock(Statement.class);
        when(stmt.executeQuery(QUERY)).thenReturn(rsFirst).thenReturn(rsSecond);
        Connection con = mock(Connection.class);
        when(con.createStatement()).thenReturn(stmt);
        DriverManagerDataSource dataSource = mockDataSourceRetrieval();
        when(dataSource.getConnection()).thenReturn(con);

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
        when(dataSources.get(dbKey, MISSING_DB_CONFIG_ERROR, dbKey)).thenReturn(dataSource);
        return dataSource;
    }

    private DriverManagerDataSource mockDataSource(String query, String dbKey, ResultSet rs) throws SQLException
    {
        Statement stmt = mock(Statement.class);
        when(stmt.executeQuery(query)).thenReturn(rs);
        Connection con = mock(Connection.class);
        when(con.createStatement()).thenReturn(stmt);
        DriverManagerDataSource dataSource = mock(DriverManagerDataSource.class);
        when(dataSource.getConnection()).thenReturn(con);
        lenient().when(dataSources.get(dbKey, MISSING_DB_CONFIG_ERROR, dbKey)).thenReturn(dataSource);
        return dataSource;
    }

    private static ResultSet mockResultSet(String columnName, String value) throws SQLException
    {
        ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
        when(rsmd.getColumnCount()).thenReturn(1);
        when(rsmd.getColumnLabel(1)).thenReturn(columnName);
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true).thenReturn(false);
        when(rs.getMetaData()).thenReturn(rsmd);
        when(rs.getObject(1)).thenReturn(value);
        return rs;
    }

    private static ResultSet mockResultSet(String columnName1, String value1, String columnName2, String value2,
            String columnName3, String value3) throws SQLException
    {
        ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
        when(rsmd.getColumnCount()).thenReturn(3);
        lenient().when(rsmd.getColumnLabel(1)).thenReturn(columnName1);
        lenient().when(rsmd.getColumnLabel(2)).thenReturn(columnName2);
        lenient().when(rsmd.getColumnLabel(3)).thenReturn(columnName3);
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true).thenReturn(false);
        when(rs.getMetaData()).thenReturn(rsmd);
        lenient().when(rs.getObject(1)).thenReturn(value1);
        lenient().when(rs.getObject(2)).thenReturn(value2);
        lenient().when(rs.getObject(3)).thenReturn(value3);
        return rs;
    }

    private void configureTimeout()
    {
        databaseSteps.setDbQueryTimeout(Duration.ofSeconds(20));
    }
}
