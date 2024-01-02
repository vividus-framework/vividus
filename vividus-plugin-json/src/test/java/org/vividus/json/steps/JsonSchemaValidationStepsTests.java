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

package org.vividus.json.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.networknt.schema.JsonSchemaException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonUtils;

@ExtendWith(MockitoExtension.class)
class JsonSchemaValidationStepsTests
{
    private static final String SCHEMA_RESOURCE_NAME = "schema.json";
    private static final String OPEN_API_SCHEMA_RESOURCE_NAME = "open-api-schema.json";

    private static final String JSON_IS_VALID_AGAINST_SCHEMA = "JSON is valid against schema";
    private static final String JSON_IS_NOT_VALID_AGAINST_SCHEMA = "JSON is not valid against schema:";

    @Mock private ISoftAssert softAssert;
    private JsonSchemaValidationSteps steps;

    @BeforeEach
    void beforeEach()
    {
        steps = new JsonSchemaValidationSteps(new JsonUtils(), softAssert);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            SCHEMA_RESOURCE_NAME,
            "schema-without-schema-tag.json"
    })
    void shouldValidateJsonAgainstSchema(String schemaResourceName)
    {
        var schema = loadResource(schemaResourceName);
        steps.validateJsonAgainstSchema(loadResource("valid-against-schema.json"), schema);
        verify(softAssert).recordAssertion(true, JSON_IS_VALID_AGAINST_SCHEMA);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldFailJSONSchemaWithWrongSpecification()
    {
        var schema = "{\"$schema\": \"https://wrong-specification\"}";
        steps.validateJsonAgainstSchema(any(), schema);
        verify(softAssert).recordFailedAssertion(
                argThat((ArgumentMatcher<Exception>) e -> e instanceof JsonSchemaException
                        && "'https://wrong-specification' is unrecognizable schema".equals(e.getMessage())));
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldFailValidationAgainstSchema()
    {
        var schema = loadResource(SCHEMA_RESOURCE_NAME);
        steps.validateJsonAgainstSchema(loadResource("not-valid-against-schema.json"), schema);
        var lineSeparator = System.lineSeparator();
        var errorMessage = JSON_IS_NOT_VALID_AGAINST_SCHEMA + lineSeparator
                + "1) $.price: is missing but it is required," + lineSeparator
                + "2) $.tags: integer found, array expected";
        verify(softAssert).recordAssertion(false, errorMessage);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldValidateJsonAgainstOpenAPISchema()
    {
        var schema = loadResource(OPEN_API_SCHEMA_RESOURCE_NAME);
        steps.validateJsonAgainstOpenApiSchema(loadResource("valid-against-open-api-schema.json"), schema);
        verify(softAssert).recordAssertion(true, JSON_IS_VALID_AGAINST_SCHEMA);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldFailValidationAgainstOpenAPISchema()
    {
        var schema = loadResource(OPEN_API_SCHEMA_RESOURCE_NAME);
        steps.validateJsonAgainstOpenApiSchema(loadResource("not-valid-against-open-api-schema.json"), schema);
        var lineSeparator = System.lineSeparator();
        var errorMessage = JSON_IS_NOT_VALID_AGAINST_SCHEMA + lineSeparator
                + "1) $.person.name: is missing but it is required," + lineSeparator
                + "2) $.person.gender: does not have a value in the enumeration [MALE, FEMALE, OTHER]";
        verify(softAssert).recordAssertion(false, errorMessage);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldFailValidationAgainstOpenAPISchemaWithoutComponentNode()
    {
        var schema = loadResource("open-api-schema-without-components.json");
        steps.validateJsonAgainstOpenApiSchema(any(), schema);
        verify(softAssert).recordFailedAssertion(
                argThat((ArgumentMatcher<String>) "Component node in OpenAPI schema could not be null"::equals));
        verifyNoMoreInteractions(softAssert);
    }

    private String loadResource(String schemaResourceName)
    {
        return ResourceUtils.loadResource(getClass(), schemaResourceName);
    }
}
