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

package org.vividus.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.jbehave.core.model.ExamplesTable.ExamplesTableProperties;
import org.junit.jupiter.api.Test;

class FetchingUrlsTableTransformerTests
{
    private static final String URLS = "urls";
    private static final String COLUMN = "column";
    private final TestTransformer transformer = new TestTransformer();

    @Test
    void testTransformFromResults()
    {
        ExamplesTableProperties properties = new ExamplesTableProperties(new Properties());
        properties.getProperties().setProperty(COLUMN, URLS);
        assertEquals("|urls|\n|/first|\n|/second|\n|/fourth%25|\n|/third|", transformer.transform("", null,
                properties));
    }

    @Test
    void testTransformEmptyColumnName()
    {
        Properties properties = new Properties();
        properties.setProperty(COLUMN, " ");
        ExamplesTableProperties tableProeprties = new ExamplesTableProperties(properties);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform("", null, tableProeprties));
        assertEquals("ExamplesTable property 'column' is blank", exception.getMessage());
    }

    @Test
    void testTransformWithNotEmptyTableAsStringParameter()
    {
        Properties properties = new Properties();
        ExamplesTableProperties tableProeprties = new ExamplesTableProperties(properties);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform("|first|", null, tableProeprties));
        assertEquals("Input table must be empty", exception.getMessage());
    }

    private static final class TestTransformer extends AbstractFetchingUrlsTableTransformer
    {
        @Override
        protected Set<String> fetchUrls(ExamplesTableProperties properties)
        {
            return new HashSet<>(Arrays.asList("http://someurl/first", "http://someurl.com/second",
                    "/third", "/fourth%25"));
        }
    }
}
