/*
 * Copyright 2019-2022 the original author or authors.
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

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IteratingTableTransformerTests
{
    private static final String TABLE = "";
    private final IteratingTableTransformer tableTransformer = new IteratingTableTransformer();
    private final Keywords  keywords = new Keywords();
    private final ParameterConverters parameterConverters = new ParameterConverters();

    private final TestLogger logger = TestLoggerFactory.getTestLogger(IteratingTableTransformer.class);

    @ParameterizedTest
    // CHECKSTYLE:OFF
    // @formatter:off
    @CsvSource({
            "'',                                          'startInclusive' is not set in ExamplesTable properties",
            "'startInclusive=1',                          'endInclusive' is not set in ExamplesTable properties",
            "'endInclusive=2',                            'startInclusive' is not set in ExamplesTable properties",
            "'startInclusive=2, endInclusive=1',          'startInclusive' value must be less than or equal to 'endInclusive' value",
            "'limit=1, startInclusive=1',                 Conflicting property declaration found: 'limit'. Use only 'startInclusive' and 'endInclusive'",
            "'limit=1, endInclusive=1',                   Conflicting property declaration found: 'limit'. Use only 'startInclusive' and 'endInclusive'",
            "'limit=1, startInclusive=1, endInclusive=1', Conflicting property declaration found: 'limit'. Use only 'startInclusive' and 'endInclusive'"
    })
    // CHECKSTYLE:ON
    // @formatter:on
    void shouldHandleInvalidInputs(String propertiesAsString, String errorMessage)
    {
        var tableProperties = new TableProperties(propertiesAsString, keywords, parameterConverters);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> tableTransformer.transform(TABLE, null, tableProperties));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldWithLimitPropertyReturnTableWithDesiredQuantityOfRows()
    {
        var tableProperties = new TableProperties("limit=2", keywords, parameterConverters);
        assertEquals("|iterator|\n|0|\n|1|", tableTransformer.transform(TABLE, null, tableProperties));
        assertThat(logger.getLoggingEvents(),
                is(List.of(warn("ExamplesTable property '{}' is deprecated and will be removed in VIVIDUS 0.6.0. Use "
                        + "'{}' and '{}' instead", "limit", "startInclusive", "endInclusive"))));
    }

    @ParameterizedTest
    @CsvSource({
            "'startInclusive=1, endInclusive=1', '|iterator|\n|1|'",
            "'startInclusive=1, endInclusive=3', '|iterator|\n|1|\n|2|\n|3|'"
    })
    void shouldReturnTableWithDesiredQuantityOfRows(String propertiesAsString, String expected)
    {
        var tableProperties = new TableProperties(propertiesAsString, keywords, parameterConverters);
        assertEquals(expected, tableTransformer.transform(TABLE, null, tableProperties));
    }
}
