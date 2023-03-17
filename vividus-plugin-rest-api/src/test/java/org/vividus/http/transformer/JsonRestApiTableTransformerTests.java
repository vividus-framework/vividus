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

package org.vividus.http.transformer;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.util.ResourceUtils;
import org.vividus.util.UriUtils;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class JsonRestApiTableTransformerTests
{
    private static final String COMMA = ",";

    private static final String JSON_DATA = ResourceUtils.loadResource(JsonRestApiTableTransformerTests.class,
            "data.json");
    private static final String URL_VALUE = "https://example.com/";
    private static final String URL_PROPERTY = "url";
    private static final String VAR_NAME = "varName";

    private static final String VARIABLE_PROPERTY = "variable";
    private static final String VARIABLE_NAME_PROPERTY = "variableName";

    private static final String EQUAL = "=";
    private static final String VARIABLE_NAME_PARAMETER = VARIABLE_NAME_PROPERTY + EQUAL;
    private static final String VARIABLE_PARAMETER = VARIABLE_PROPERTY + EQUAL;

    @Mock private VariableContext variableContext;
    @Mock private IHttpClient httpClient;
    @InjectMocks private JsonRestApiTableTransformer jsonTableGenerator;

    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters =  new ParameterConverters();

    private final TestLogger logger = TestLoggerFactory.getTestLogger(JsonRestApiTableTransformer.class);

    @Test
    void testTransformFromUrl() throws IOException
    {
        var httpResponse = mock(HttpResponse.class);
        when(httpClient.doHttpGet(UriUtils.createUri(URL_VALUE))).thenReturn(httpResponse);
        when(httpResponse.getResponseBodyAsString()).thenReturn(JSON_DATA);
        testTransform(URL_PROPERTY + EQUAL + URL_VALUE);
        assertEquals(getDeprecatedEvent(URL_PROPERTY, VARIABLE_NAME_PROPERTY), logger.getLoggingEvents());
    }

    static Stream<Arguments> parameters()
    {
        return Stream.of(
            arguments(VARIABLE_PARAMETER, getDeprecatedEvent(VARIABLE_PROPERTY, VARIABLE_NAME_PROPERTY)),
            arguments(VARIABLE_NAME_PARAMETER, Collections.EMPTY_LIST)
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void testTransformFromVariable(String parameter, List<LoggingEvent> expectedLogEvent)
    {
        when(variableContext.getVariable(VAR_NAME)).thenReturn(JSON_DATA);
        testTransform(parameter + VAR_NAME);
        assertEquals(expectedLogEvent, logger.getLoggingEvents());
    }

    @Test
    void testTransformFromVariableValidationFails()
    {
        var exception = assertThrows(IllegalArgumentException.class,
            () -> testTransform(VARIABLE_NAME_PARAMETER + VAR_NAME + COMMA + VARIABLE_PARAMETER + VAR_NAME)
        );
        assertEquals(
            "Only one property can be used, but found `variable` and `variableName`. Please use `variableName`",
            exception.getMessage());

        assertEquals(getDeprecatedEvent(VARIABLE_PROPERTY, VARIABLE_NAME_PROPERTY), logger.getLoggingEvents());
    }

    private void testTransform(String source)
    {
        var tableProperties = createProperties(source + ",columns=column_code=$.superCodes..code;"
                        + "column_codeSystem=$.superCodes..codeSystem;"
                        + "column_type=$.superCodes..type");
        var table = jsonTableGenerator.transform(StringUtils.EMPTY, null, tableProperties);
        var expectedTable =
                "|column_code|column_codeSystem|column_type|\n"
                + "|107214|VIVIDUS|A|\n"
                + "|107224|VIVIDUS|B|\n"
                + "|107314|VIVIDUS|C|\n"
                + "|107324|VIVIDUS|D|\n"
                + "|107XX4|VIVIDUS|E|\n"
                + "|1|true|F|";
        assertEquals(expectedTable, table);
    }

    @Test
    void testTransformFromVariableWithComplexJsonPath()
    {
        var variableName = "varWithJson";
        when(variableContext.getVariable(variableName)).thenReturn(JSON_DATA);
        var columns = "columns=type=$..[?(@.codes[0].code==\"107214\")].type";
        var tableProperties = createProperties(columns + COMMA + VARIABLE_NAME_PARAMETER + variableName);
        var table = jsonTableGenerator.transform(StringUtils.EMPTY, null, tableProperties);
        var expectedTable = "|type|\n|A|";
        assertEquals(expectedTable, table);
        assertEquals(Collections.EMPTY_LIST, logger.getLoggingEvents());
    }

    @Test
    void testSourceTransformPropertyIsNotSpecifiedException()
    {
        var properties = createProperties("columns=key=value\\,url");
        var exception = assertThrows(IllegalArgumentException.class,
            () -> jsonTableGenerator.transform(StringUtils.EMPTY, null, properties));
        assertEquals("One of ExamplesTable properties must be set: either 'url' or 'variableName'",
            exception.getMessage());
    }

    @Test
    void testColumnsTransformPropertyIsNotSpecifiedException()
    {
        var properties = createProperties(VARIABLE_NAME_PROPERTY + EQUAL + "any");
        var exception = assertThrows(IllegalArgumentException.class,
            () -> jsonTableGenerator.transform(StringUtils.EMPTY, null, properties));
        assertEquals("'columns' is not set in ExamplesTable properties", exception.getMessage());
    }

    private TableProperties createProperties(String propertiesAsString)
    {
        return new TableProperties(propertiesAsString, keywords, parameterConverters);
    }

    private static List<LoggingEvent> getDeprecatedEvent(String current, String replacement)
    {
        return List.of(warn(
            "`{}` parameter of `FROM_JSON` table transformer is deprecated and will be removed in VIVIDUS 0.6.0. "
                + "`{}` parameter must be used instead.", current, replacement));
    }
}
