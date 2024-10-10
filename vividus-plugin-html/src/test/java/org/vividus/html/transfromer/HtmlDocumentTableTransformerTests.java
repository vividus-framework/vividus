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

package org.vividus.html.transfromer;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.html.transfromer.HtmlDocumentTableTransformer.HttpConfiguration;

@ExtendWith(MockitoExtension.class)
class HtmlDocumentTableTransformerTests
{
    private static final String PAGE_URL = "https://example.com";
    private static final String DOC = """
        <!DOCTYPE html>
        <html>
        <body>
        <a href="/r">R</a>
        <a href="/g">G</a>
        <a href="/b">B</a>
        </body>
        </html>
        """;
    private static final Document HTML_DOC = Jsoup.parse(DOC);
    private static final String RELS = """
        |col|
        |/r|
        |/g|
        |/b|""";
    private static final String WRONG_PARAMS_EXPECTED_ERROR_MSG = "One of either ''pageUrl'', ''variableName''"
            + " or ''path'' should be specified";

    @Mock private VariableContext variableContext;

    @ParameterizedTest
    @CsvSource(value = {
        "'pageUrl=https://example.com, variableName=html', '" + WRONG_PARAMS_EXPECTED_ERROR_MSG + "'",
        "'column=col', '" + WRONG_PARAMS_EXPECTED_ERROR_MSG + "'",
        "'pageUrl=https://example.com, path=index.html', '" + WRONG_PARAMS_EXPECTED_ERROR_MSG + "'",
        "'pageUrl=https://example.com, path=index.html, variableName=html', '" + WRONG_PARAMS_EXPECTED_ERROR_MSG + "'",
        "'path= ', 'ExamplesTable property ''path'' is blank'",
        "'variableName= ', 'ExamplesTable property ''variableName'' is blank'"
    })
    void shouldFailOnInvalidInputParameters(String parameters, String expectedErrorMsg)
    {
        TableProperties tableProperties = new TableProperties(parameters, new Keywords(), new ParameterConverters());
        HtmlDocumentTableTransformer transformer = new HtmlDocumentTableTransformer(Optional.empty(), variableContext);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> transformer.transform(EMPTY, null, tableProperties));
        assertEquals(expectedErrorMsg, thrown.getMessage());
    }

    @Test
    void shouldBuildTableByElementAttributeFromVariableValue()
    {
        when(variableContext.getVariable("html")).thenReturn(DOC);
        TableProperties tableProperties = new TableProperties("column=col, variableName=html, xpathSelector=//a/@href",
                new Keywords(), new ParameterConverters());
        HtmlDocumentTableTransformer transformer = new HtmlDocumentTableTransformer(Optional.empty(),
                variableContext);
        String table = transformer.transform(EMPTY, null, tableProperties);
        assertEquals(RELS, table);
    }

    @Test
    void shouldBuildTableByElementAttributeFromResource()
    {
        TableProperties tableProperties = new TableProperties("column=col,"
                + " path=org/vividus/html/transfromer/index.html, xpathSelector=//a/text()",
                new Keywords(), new ParameterConverters());
        HtmlDocumentTableTransformer transformer = new HtmlDocumentTableTransformer(Optional.empty(),
                variableContext);
        String table = transformer.transform(EMPTY, null, tableProperties);
        String expectedTable = """
                |col|
                |More information...|""";
        assertEquals(expectedTable, table);
    }

    @Test
    void shouldBuildTableByElementAttribute() throws IOException
    {
        HttpConfiguration cfg = new HttpConfiguration();
        Map<String, String> headers = Map.of("credit-card-pin", "1234");
        cfg.setHeaders(headers);
        performTest(Optional.of(cfg), "//a/@href", t -> assertEquals(RELS, t), con -> verify(con).headers(headers));
    }

    @Test
    void shouldBuildTableByElementText() throws IOException
    {
        performTest(Optional.empty(), "//a/text()", t -> assertEquals("""
                |col|
                |R|
                |G|
                |B|""", t), Mockito::verifyNoMoreInteractions);
    }

    @Test
    void shouldBuildTableByElementHtml() throws IOException
    {
        performTest(Optional.empty(), "//a", t -> assertEquals("""
                |col|
                |<a href="/r">R</a>|
                |<a href="/g">G</a>|
                |<a href="/b">B</a>|""", t), Mockito::verifyNoMoreInteractions);
    }

    private void performTest(Optional<HttpConfiguration> httpConfiguration, String xpathSelector,
            Consumer<String> tableConsumer, Consumer<Connection> connectionConsumer) throws IOException
    {
        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class))
        {
            Connection connection = mock();

            jsoup.when(() -> Jsoup.connect(PAGE_URL)).thenReturn(connection);
            when(connection.get()).thenReturn(HTML_DOC);

            TableProperties tableProperties = new TableProperties(
                    "column=col, pageUrl=https://example.com, xpathSelector=%s".formatted(xpathSelector),
                    new Keywords(), new ParameterConverters());
            HtmlDocumentTableTransformer transformer = new HtmlDocumentTableTransformer(httpConfiguration,
                    variableContext);
            String table = transformer.transform(EMPTY, null, tableProperties);
            tableConsumer.accept(table);
            verify(connection).get();
            connectionConsumer.accept(connection);
        }
    }
}
