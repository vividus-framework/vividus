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

package org.vividus.crawler.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.ui.web.configuration.AuthenticationMode;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.util.UriUtils;

class FetchingUrlsTableTransformerTests
{
    private static final URI PAGE_URI = UriUtils.createUri("https://example.com");
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
        var expected = """
                |urls|
                |/first|
                |/second|
                |/fourth%25|
                |/third|""";
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
        TableProperties props = new TableProperties(StringUtils.EMPTY, new Keywords(), new ParameterConverters());
        transformer.setWebApplicationConfiguration(new WebApplicationConfiguration(null, AuthenticationMode.URL));
        var exception = assertThrows(IllegalArgumentException.class,
                () -> transformer.getMainApplicationPageUri(props));
        assertEquals("URL of the main application page should be non-blank", exception.getMessage());
    }

    @Test
    void shouldReturnMainAppUrlFromTransformerParameter()
    {
        WebApplicationConfiguration webAppCfg = mock();
        transformer.setWebApplicationConfiguration(webAppCfg);
        transformer.setMainPageUrl(null);
        TableProperties props = new TableProperties("mainPageUrl=%s".formatted(PAGE_URI), new Keywords(),
                new ParameterConverters());
        assertEquals(PAGE_URI, transformer.getMainApplicationPageUri(props));
        verifyNoInteractions(webAppCfg);
    }

    @Test
    void shouldReturnMainAppUrlFromMainPageUrlProperty()
    {
        WebApplicationConfiguration webAppCfg = mock();
        transformer.setWebApplicationConfiguration(webAppCfg);
        transformer.setMainPageUrl(PAGE_URI);
        TableProperties props = new TableProperties(StringUtils.EMPTY, new Keywords(), new ParameterConverters());
        assertEquals(PAGE_URI, transformer.getMainApplicationPageUri(props));
        verifyNoInteractions(webAppCfg);
    }

    @Test
    void shouldReturnMainAppUrlFromWebConfiguration()
    {
        WebApplicationConfiguration webAppCfg = mock();
        when(webAppCfg.getMainApplicationPageUrl()).thenReturn(PAGE_URI);
        transformer.setWebApplicationConfiguration(webAppCfg);
        transformer.setMainPageUrl(null);
        TableProperties props = new TableProperties(StringUtils.EMPTY, new Keywords(), new ParameterConverters());
        assertEquals(PAGE_URI, transformer.getMainApplicationPageUri(props));
        verify(webAppCfg).getMainApplicationPageUrl();
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
