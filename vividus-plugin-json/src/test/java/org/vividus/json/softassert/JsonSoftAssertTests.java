/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.json.softassert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import net.javacrumbs.jsonunit.JsonMatchers;

class JsonSoftAssertTests
{
    private static final String CONDITION_IS_TRUE = "Condition is true";
    private static final String TEXT = "text";
    private static final String QUOTE = "\"";

    private final JsonSoftAssert jsonSoftAssert = new JsonSoftAssert();

    @Test
    void testGetAssertionDescriptionString()
    {
        String actual = jsonSoftAssert.getAssertionDescriptionString(QUOTE + TEXT + QUOTE,
                JsonMatchers.jsonEquals(TEXT));
        assertEquals(CONDITION_IS_TRUE, actual);
    }

    @Test
    void testGetAssertionDescriptionStringNotMatched()
    {
        String differentTest = "text2";
        String actual = jsonSoftAssert.getAssertionDescriptionString(QUOTE + TEXT + QUOTE,
                JsonMatchers.jsonEquals(differentTest));
        assertNotNull(actual);
        assertTrue(actual.contains(TEXT));
        assertTrue(actual.contains(differentTest));
    }

    @Test
    void testGetAssertionDescriptionStringSuperMethod()
    {
        String actual = jsonSoftAssert.getAssertionDescriptionString(QUOTE + TEXT + QUOTE,
                Matchers.equalTo(TEXT));
        assertNotEquals(CONDITION_IS_TRUE, actual);
    }
}
