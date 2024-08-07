/*
 * Copyright 2019-2024 the original author or authors.
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
class LeftJoinTableTransformerTests
{
    private static final String JOINT_COLUMN = "joinID";
    private static final String PATH_1 = "/test1.table";
    private static final String USERS = """
            |joinID|Customer Name|Country  |
            |1     |Alice        |USA      |
            |2     |Bob          |Canada   |
            |3     |Charlie      |UK       |
            |4     |David        |Australia|
            |5     |Eva          |Germany  |
            """;
    private static final String ORDERS = """
            |joinID|Order ID|Order Amount|
            |1     |101     |150.00      |
            |3     |102     |200.00      |
            |5     |103     |250.00      |
            |5     |104     |300.00      |
            """;

    @Mock private ExamplesTableFactory factory;
    @Mock private Configuration configuration;
    @InjectMocks private LeftJoinTableTransformer transformer;

    @Test
    void shouldTransform()
    {
        Properties properties = createProperties(JOINT_COLUMN, JOINT_COLUMN, PATH_1);
        mockCreateExamplesTable(PATH_1, USERS);
        mockCreateExamplesTable(ORDERS, ORDERS);
        String expectedTable = """
                |Country|joinID|Order Amount|Customer Name|Order ID|
                |USA|1|150.00|Alice|101|
                |Canada|2||Bob||
                |UK|3|200.00|Charlie|102|
                |Australia|4||David||
                |Germany|5|250.00|Eva|103|
                |Germany|5|300.00|Eva|104|
                """;
        assertJoin(ORDERS, properties, expectedTable);
    }

    private void mockCreateExamplesTable(String input, String table)
    {
        when(factory.createExamplesTable(input)).thenReturn(new ExamplesTable(table));
    }

    private void assertJoin(String tableBody, Properties properties, String expected)
    {
        Keywords keywords = new Keywords();
        ParameterConverters parameterConverters = new ParameterConverters().addConverters(new FluentEnumConverter());
        when(configuration.examplesTableFactory()).thenReturn(factory);
        TableProperties tableProperties = new TableProperties(StringUtils.EMPTY, keywords, parameterConverters);
        tableProperties.getProperties().putAll(properties);
        when(configuration.keywords()).thenReturn(keywords);
        assertEquals(expected, transformer.transform(tableBody, null, tableProperties));
    }

    private Properties createProperties(String rightTableJoinColumn, String leftTableJoinColumn,  String tables)
    {
        Properties properties = new Properties();
        properties.setProperty("rightTableJoinColumn", rightTableJoinColumn);
        properties.setProperty("leftTableJoinColumn", leftTableJoinColumn);
        properties.setProperty("tables", tables);
        return properties;
    }
}
