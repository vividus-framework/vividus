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

package org.vividus.bdd.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.ExamplesTableProperties;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SortingTableTransformerTests
{
    private static final String TABLE_WITH_SAME_VALUES = "|key1|key2|\n|10|0|\n|1|0|";
    private static final String TABLE = "|key1|key2|\n|4|3|\n|1|0|";

    @Mock
    private Supplier<ExamplesTableFactory> examplesTableFactorySupplier;

    @Mock
    private ExamplesTableFactory examplesTableFactory;

    @InjectMocks
    private SortingTableTransformer transformer;

    @ParameterizedTest
    @MethodSource("tableSource")
    void testTransform(String expectedTable, Properties properties, String tableToTransform)
    {
        when(examplesTableFactorySupplier.get()).thenReturn(examplesTableFactory);
        when(examplesTableFactory.createExamplesTable(tableToTransform))
                .thenReturn(new ExamplesTable(tableToTransform));
        assertEquals(expectedTable, transformer.transform(tableToTransform, new TableParsers(),
                new ExamplesTableProperties(properties)));
    }

    @Test
    void testFailOnMissingTableProperty()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE, new TableParsers(), new ExamplesTableProperties(new Properties())));
        assertEquals("'byColumns' is not set in ExamplesTable properties", exception.getMessage());
    }

    private static Properties createProperties(String keys)
    {
        Properties properties = new Properties();
        properties.setProperty("byColumns", keys);
        return properties;
    }

    // CHECKSTYLE:OFF
    static Stream<Arguments> tableSource() {
        return Stream.of(
            Arguments.of(TABLE,                                createProperties("key5"),      TABLE),
            Arguments.of("|key1|key2|\n|1|0|\n|4|3|",          createProperties("key1"),      TABLE),
            Arguments.of(TABLE_WITH_SAME_VALUES,               createProperties("key2"),      TABLE_WITH_SAME_VALUES),
            Arguments.of("|key1|key2|key3|\n|0|2|1|\n|0|2|5|", createProperties("key1|key3"), "|key1|key2|key3|\n|0|2|5|\n|0|2|1|")
        );
    }
    // CHECKSTYLE:ON
}
