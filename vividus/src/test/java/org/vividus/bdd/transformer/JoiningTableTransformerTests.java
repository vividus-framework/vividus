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

package org.vividus.bdd.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JoiningTableTransformerTests
{
    private static final String PATH = "/test.table";
    private static final String MERGING_TRANSFORMER = "{transformer=MERGING, mergeMode=columns, tables="
            + "\\{transformer=FROM_EXCEL\\, path=test.csv\\, sheet=data\\, range=A2:A3\\, column=test1\\};"
            + "\\{transformer=FROM_EXCEL\\, path=test.csv\\, sheet=data\\, range=B2:B3\\, column=test2\\}}";

    @Mock private ExamplesTableFactory factory;
    @Mock private Configuration configuration;
    @InjectMocks private JoiningTableTransformer joiningTableTransformer;

    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters = new ParameterConverters()
            .addConverters(new FluentEnumConverter());

    @Test
    void testTransformInColumnsModeWithoutColumnName()
    {
        when(configuration.examplesTableFactory()).thenReturn(factory);
        var tableProperties = new TableProperties("joinMode=columns", keywords, parameterConverters);
        var exception = assertThrows(IllegalArgumentException.class,
            () -> joiningTableTransformer.transform(PATH, null, tableProperties));
        assertEquals("'joinedColumn' is not set in ExamplesTable properties", exception.getMessage());
    }

    static Stream<Arguments> tableSource()
    {
        // CHECKSTYLE:OFF
        // @formatter:off
        return Stream.of(
                arguments(PATH,                "|var7|var8|\n|z|t|\n|y|e|",                         "joinMode=columns, columnsToJoin=var8, joinedColumn=var9",      "|var7|var9|\n|z|t|\n|y|e|"),
                arguments(PATH,                "|var1|var2|\n|a|b|\n|c|d|",                         "joinMode=columns, joinedColumn=var5",                          "|var5|\n|a b|\n|c d|"),
                arguments(PATH,                "|var3|var4|\n|e|f|\n|g|h|",                         "joinMode=rows",                                                "|var3|var4|\n|e g|f h|"),
                arguments(PATH,                "{valueSeparator=!}\n|var1|var2|\n!A!B|C!\n!D!E|F!", "joinMode=rows",                                                "|var1|var2|\n!A D!B|C E|F!"),
                arguments(PATH,                "|var1|var2|var4|\n|q|r|s|\n|t|u|v|",                "joinMode=columns, columnsToJoin=var1;var4, joinedColumn=var3", "|var3|var2|\n|q s|r|\n|t v|u|"),
                arguments(MERGING_TRANSFORMER, "|var5|var6|\n|i|j|\n|k|l|",                         "joinMode=rows",                                                "|var5|var6|\n|i k|j l|")
        );
        // CHECKSTYLE:ON
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("tableSource")
    void shouldJoinTables(String path, String table, String propertiesAsString, String expected)
    {
        when(configuration.examplesTableFactory()).thenReturn(factory);
        when(factory.createExamplesTable(path)).thenReturn(new ExamplesTable(table));
        var tableProperties = new TableProperties(propertiesAsString, keywords, parameterConverters);
        assertEquals(expected, joiningTableTransformer.transform(path, null, tableProperties));
    }
}
