/*
 * Copyright 2019-2022 the original author or authors.
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.vividus.json.JsonContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;

public abstract class AbstractJsonSteps
{
    private final JsonSteps jsonSteps;
    private final ISoftAssert softAssert;
    private final JsonContext jsonContext;

    protected AbstractJsonSteps(JsonContext jsonContext, ISoftAssert softAssert, JsonSteps jsonSteps)
    {
        this.jsonContext = jsonContext;
        this.softAssert = softAssert;
        this.jsonSteps = jsonSteps;
    }

    protected Optional<List<?>> getElements(String json, String jsonPath)
    {
        Optional<Optional<Object>> jsonObject = jsonSteps.getDataByJsonPathSafely(json, jsonPath, false);
        return jsonObject.map(e -> e.map(value -> value instanceof List ? (List<?>) value : List.of(value))
                .orElseGet(() -> Collections.singletonList(null)));
    }

    protected static int countElementsNumber(Optional<List<?>> elements)
    {
        return elements.map(List::size).orElse(0).intValue();
    }

    protected boolean assertJsonElementsNumber(String jsonPath, int actualNumber, ComparisonRule comparisonRule,
            int expectedElementsNumber)
    {
        return softAssert.assertThat("The number of JSON elements by JSON path: " + jsonPath, actualNumber,
                comparisonRule.getComparisonRule(expectedElementsNumber));
    }

    protected String getActualJson()
    {
        return jsonContext.getJsonContext();
    }

    protected  JsonContext getJsonContext()
    {
        return jsonContext;
    }
}
