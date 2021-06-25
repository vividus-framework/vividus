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

package org.vividus.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.ui.web.configuration.AuthenticationMode;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;

class FetchingUrlsTableTransformerTests
{
    private static final String URLS = "urls";
    private static final String COLUMN = "column";
    private final TestTransformer transformer = new TestTransformer();
    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters = new ParameterConverters();

    @Test
    void testTransformFromResults()
    {
        var tableProperties = new TableProperties("", keywords, parameterConverters);
        tableProperties.getProperties().setProperty(COLUMN, URLS);
        var expected = "|urls|\n"
                + "|/first|\n"
                + "|/second|\n"
                + "|/fourth%25|\n"
                + "|/third|";
        assertEquals(expected, transformer.transform("", null, tableProperties));
    }

    @ParameterizedTest
    @CsvSource({
            "'column= ', '',      ExamplesTable property 'column' is blank",
            "'',         |first|, Input table must be empty"
    })
    void shouldHandleInvalidInputs(String propertiesAsString, String tableAsString, String errorMessage)
    {
        var tableProperties = new TableProperties(propertiesAsString, keywords, parameterConverters);
        var exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(tableAsString, null, tableProperties));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testTransformWithoutMainApplicationPageUrl()
    {
        transformer.setWebApplicationConfiguration(new WebApplicationConfiguration(null, AuthenticationMode.URL));
        var exception = assertThrows(IllegalArgumentException.class, transformer::getMainApplicationPageUri);
        assertEquals("URL of the main application page should be non-blank", exception.getMessage());
    }

    private static final class TestTransformer extends AbstractFetchingUrlsTableTransformer
    {
        @Override
        protected Set<String> fetchUrls(TableProperties properties)
        {
            return new HashSet<>(List.of("http://someurl/first", "http://someurl.com/second", "/third", "/fourth%25"));
        }
    }
}
