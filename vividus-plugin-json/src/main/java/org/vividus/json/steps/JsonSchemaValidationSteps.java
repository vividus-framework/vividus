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

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;

import org.jbehave.core.annotations.Then;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.json.JsonUtils;

public class JsonSchemaValidationSteps
{
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private final JsonUtils jsonUtils;
    private final ISoftAssert softAssert;

    public JsonSchemaValidationSteps(JsonUtils jsonUtils, ISoftAssert softAssert)
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
        SpecVersion.VersionFlag version;
        try
        {
            version = SpecVersionDetector.detectOptionalVersion(schemaNode).orElse(SpecVersion.VersionFlag.V202012);
        }
        catch (JsonSchemaException e)
        {
            softAssert.recordFailedAssertion(e);
            return;
        }
        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(version).getSchema(schemaNode);
        JsonNode jsonNode = jsonUtils.readTree(json);
        Set<ValidationMessage> validationMessages = jsonSchema.validate(jsonNode);
        assertValidationMessages(validationMessages);
    }

    private void assertValidationMessages(Set<ValidationMessage> validationMessages)
    {
        boolean passed = validationMessages.isEmpty();
        StringBuilder errorMessageBuilder = new StringBuilder("JSON is ");
        if (!passed)
        {
            errorMessageBuilder.append("not ");
        }
        errorMessageBuilder.append("valid against schema");
        int index = 1;
        for (ValidationMessage validationMessage : validationMessages)
        {
            errorMessageBuilder.append(index == 1 ? ':' : ',')
                    .append(LINE_SEPARATOR)
                    .append(index++).append(") ")
                    .append(validationMessage);
        }
        softAssert.recordAssertion(passed, errorMessageBuilder.toString());
    }
}
