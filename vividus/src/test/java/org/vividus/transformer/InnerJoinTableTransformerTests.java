/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InnerJoinTableTransformerTests
{
    private static final String JOINT_COLUMN = "joinID";
    private static final String OTHER_JOINT_COLUMN = "joinID1";
    private static final String RIGHT_COLUMN_PARAMETER_NAME = "rightTableJoinColumn";
    private static final String LEFT_COLUMN_PARAMETER_NAME = "leftTableJoinColumn";
    private static final String PATH_1 = "/test1.table";
    private static final String PATH_2 = "/test2.table";
    private static final String PATH_3 = "/test3.table";
    private static final String TABLES_DELIMITER = "; ";
    private static final String THREE_PATHS = String.join(TABLES_DELIMITER, PATH_1, PATH_2, PATH_3);
    private static final String FIRST_TABLE = "|column1|joinID|column2|column3|\n"
            + "|row11  |1     |row21  |row31  |\n"
            + "|row12  |2     |row22  |row32  |\n"
            + "|row13  |3     |row23  |row33  |\n"
            + "|row133 |3     |row233 |row333 |\n"
            + "|row14  |4     |row24  |row34  |\n";
    private static final String SECOND_TABLE = "|joinID|column4|column5|\n"
            + "|5     |row45  |row51  |\n"
            + "|3     |row43  |row53  |\n"
            + "|1     |row41  |row51  |\n"
            + "|3     |row433 |row533 |\n";
    private static final String EMPTY_TABLE = "|joinID|\n";

    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters = new ParameterConverters()
            .addConverters(new FluentEnumConverter());

    @Mock private ExamplesTableFactory factory;
    @Mock private Configuration configuration;
    @InjectMocks private InnerJoinTableTransformer transformer;

    @Test
    void shouldTransform()
    {
        Properties properties = createProperties(JOINT_COLUMN, JOINT_COLUMN, PATH_1);
        mockCreateExamplesTable(PATH_1, FIRST_TABLE);
        mockCreateExamplesTable(SECOND_TABLE);
        String expectedTable = "|column1|joinID|column5|column4|column3|column2|\n"
                + "|row11|1|row51|row41|row31|row21|\n"
                + "|row13|3|row53|row43|row33|row23|\n"
                + "|row13|3|row533|row433|row33|row23|\n"
                + "|row133|3|row53|row43|row333|row233|\n"
                + "|row133|3|row533|row433|row333|row233|\n";
        assertInnerJoin(SECOND_TABLE, properties, expectedTable);
    }

    @Test
    void shouldFailIfTablesContainEqualColumns()
    {
        Properties properties = createProperties(JOINT_COLUMN, JOINT_COLUMN, PATH_1);
        String tableWithEqualColumns = "|column1|joinID|column6|\n"
                + "|row11  |1     |row21  |\n"
                + "|row12  |2     |row22  |\n"
                + "|row13  |3     |row23  |\n"
                + "|row133 |3     |row233 |\n"
                + "|row14  |4     |row24  |\n";
        mockCreateExamplesTable(PATH_1, FIRST_TABLE);
        mockCreateExamplesTable(tableWithEqualColumns);
        verifyIllegalArgumentException(tableWithEqualColumns, properties, "Tables must contain different "
                + "columns (except joint column), but found the same columns: [column1]");
    }

    @Test
    void shouldFailIfTableDoesNotContainLeftJoinColumn()
    {
        Properties properties =  createProperties(JOINT_COLUMN, OTHER_JOINT_COLUMN, PATH_1);
        mockCreateExamplesTable(PATH_1, FIRST_TABLE);
        mockCreateExamplesTable(SECOND_TABLE);
        verifyIllegalArgumentException(SECOND_TABLE, properties,
                "The left table doesn't contain the following column: joinID1");
    }

    @Test
    void shouldFailIfTableDoesNotContainRightJoinColumn()
    {
        Properties properties =  createProperties(OTHER_JOINT_COLUMN, JOINT_COLUMN, PATH_1);
        mockCreateExamplesTable(PATH_1, FIRST_TABLE);
        mockCreateExamplesTable(SECOND_TABLE);
        verifyIllegalArgumentException(SECOND_TABLE, properties,
                "The right table doesn't contain the following column: joinID1");
    }

    @Test
    void shouldFailIfMoreThanTwoTables()
    {
        Properties properties =  createProperties(JOINT_COLUMN, JOINT_COLUMN, THREE_PATHS);
        verifyIllegalArgumentException(SECOND_TABLE, properties, "Please, specify only two ExamplesTable-s");
    }

    @Test
    void shouldTransformToEmptyTableIfOneTableEmpty()
    {
        Properties properties =  createProperties(JOINT_COLUMN, JOINT_COLUMN, PATH_1);
        mockCreateExamplesTable(PATH_1, FIRST_TABLE);
        mockCreateExamplesTable(EMPTY_TABLE);
        String expectedTable = "|column1|column3|column2|joinID|";
        assertInnerJoin(EMPTY_TABLE, properties, expectedTable);
    }

    private void mockCreateExamplesTable(String table)
    {
        mockCreateExamplesTable(table, table);
    }

    private void mockCreateExamplesTable(String input, String table)
    {
        when(factory.createExamplesTable(input)).thenReturn(new ExamplesTable(table));
    }

    private void assertInnerJoin(String tableBody, Properties properties, String expected)
    {
        when(configuration.examplesTableFactory()).thenReturn(factory);
        TableProperties tableProperties = new TableProperties(StringUtils.EMPTY, keywords, parameterConverters);
        tableProperties.getProperties().putAll(properties);
        when(configuration.keywords()).thenReturn(keywords);
        assertEquals(expected, transformer.transform(tableBody, null, tableProperties));
    }

    private Properties createProperties(String rightTableJoinColumn, String leftTableJoinColumn,  String tables)
    {
        Properties properties = new Properties();
        properties.setProperty(RIGHT_COLUMN_PARAMETER_NAME, rightTableJoinColumn);
        properties.setProperty(LEFT_COLUMN_PARAMETER_NAME, leftTableJoinColumn);
        properties.setProperty("tables", tables);
        return properties;
    }

    private void verifyIllegalArgumentException(String tableBody, Properties properties, String message)
    {
        when(configuration.examplesTableFactory()).thenReturn(factory);
        TableProperties tableProperties = new TableProperties(StringUtils.EMPTY, keywords, parameterConverters);
        tableProperties.getProperties().putAll(properties);
        when(configuration.keywords()).thenReturn(keywords);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(tableBody, null, tableProperties));
        assertEquals(message, exception.getMessage());
    }
}
