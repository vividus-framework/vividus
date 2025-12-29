/*
 * Copyright 2019-2025 the original author or authors.
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

import java.util.List;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

import org.jbehave.core.annotations.Then;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.json.JsonJackson3Utils;

import tools.jackson.databind.JsonNode;

public class JsonSchemaValidationSteps
{
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private final JsonJackson3Utils jsonUtils;
    private final ISoftAssert softAssert;

    public JsonSchemaValidationSteps(JsonJackson3Utils jsonUtils, ISoftAssert softAssert)
    {
        this.jsonUtils = jsonUtils;
        this.softAssert = softAssert;
    }

    /**
     * Validates json against json schema. The step validates JSON according to
     * <a href="https://json-schema.org/specification-links.html">schema specification</a> version provided in the
     * schema itself, e.g.: <br><i>"$schema": "https://json-schema.org/draft/2020-12/schema"</i><br>
     * If the version is not present in the schema then JSON is validated according to
     * <a href="https://json-schema.org/specification-links.html#2020-12">2020-12</a> version.
     * @param json The JSON to validate.
     * @param schema The JSON schema.
     */
    @Then("JSON `$json` is valid against schema `$schema`")
    public void validateJsonAgainstSchema(String json, String schema)
    {
        JsonNode schemaNode = jsonUtils.readTree(schema);
        Schema jsonSchema;
        try
        {
            jsonSchema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(schemaNode);
        }
        catch (SchemaException e)
        {
            softAssert.recordFailedAssertion(e);
            return;
        }
        JsonNode jsonNode = jsonUtils.readTree(json);
        List<Error> validationMessages = jsonSchema.validate(jsonNode);
        assertValidationMessages(validationMessages);
    }

    private void assertValidationMessages(List<Error> validationMessages)
    {
        boolean passed = validationMessages.isEmpty();
        StringBuilder errorMessageBuilder = new StringBuilder("JSON is ");
        if (!passed)
        {
            errorMessageBuilder.append("not ");
        }
        errorMessageBuilder.append("valid against schema");
        int index = 1;
        for (Error validationMessage : validationMessages)
        {
            errorMessageBuilder.append(index == 1 ? ':' : ',')
                    .append(LINE_SEPARATOR)
                    .append(index++).append(") ")
                    .append(validationMessage);
        }
        softAssert.recordAssertion(passed, errorMessageBuilder.toString());
    }
}
