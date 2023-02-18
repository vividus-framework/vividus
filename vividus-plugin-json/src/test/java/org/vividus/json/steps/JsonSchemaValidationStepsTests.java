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
        verify(softAssert).recordAssertion(true, "JSON is valid against schema");
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
        var errorMessage = "JSON is not valid against schema:" + lineSeparator
                + "1) $.price: is missing but it is required," + lineSeparator
                + "2) $.tags: integer found, array expected";
        verify(softAssert).recordAssertion(false, errorMessage);
        verifyNoMoreInteractions(softAssert);
    }

    private String loadResource(String schemaResourceName)
    {
        return ResourceUtils.loadResource(getClass(), schemaResourceName);
    }
}
