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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonUtils;

@ExtendWith(MockitoExtension.class)
class JsonSchemaValidationStepsTests
{
    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Mock
    private ISoftAssert softAssert;

    private JsonSchemaValidationSteps steps;

    @BeforeEach
    void beforeEach()
    {
        steps = new JsonSchemaValidationSteps(new JsonUtils(), softAssert);
    }

    @Test
    void shouldValidateJsonAgainstSchema()
    {
        verifyValidationOfValidJson(loadSchema());
    }

    @Test
    void shouldValidateJsonAgainstSchemaWithoutSchemaTag()
    {
        var schema = ResourceUtils.loadResource(getClass(), "schema-without-schema-tag.json");
        verifyValidationOfValidJson(schema);
    }

    @Test
    void shouldFailJSONSchemaWithWrongSpecification()
    {
        var schema = "{\"$schema\": \"wrong-specification\"}";
        steps.validateJsonAgainstSchema(loadValidAgainstSchemaJson(), schema);
        verify(softAssert).recordFailedAssertion("`wrong-specification` is unrecognizable schema");
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldFailValidationAgainstSchema()
    {
        var notValidJson = ResourceUtils.loadResource(getClass(), "not-valid-against-schema.json");
        steps.validateJsonAgainstSchema(notValidJson, loadSchema());
        var errorMessage = "JSON is not valid against schema:" + LINE_SEPARATOR
                + "1) $.price: is missing but it is required," + LINE_SEPARATOR
                + "2) $.tags: integer found, array expected";
        verify(softAssert).recordAssertion(false, errorMessage);
        verifyNoMoreInteractions(softAssert);
    }

    private void verifyValidationOfValidJson(String jsonSchema)
    {
        steps.validateJsonAgainstSchema(loadValidAgainstSchemaJson(), jsonSchema);
        verify(softAssert).recordAssertion(true, "JSON is valid against schema");
        verifyNoMoreInteractions(softAssert);
    }

    private String loadValidAgainstSchemaJson()
    {
        return ResourceUtils.loadResource(getClass(), "valid-against-schema.json");
    }

    private String loadSchema()
    {
        return ResourceUtils.loadResource(getClass(), "schema.json");
    }
}
