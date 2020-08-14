/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UriEncodingExpressionProcessorsTests
{
    private final IExpressionProcessor processor = new UriEncodingExpressionProcessors();

    @Test
    void testExecuteWithUnsupportedException()
    {
        assertEquals(Optional.empty(), processor.execute("encodeUri(value)"));
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\", result is \"{1}\"")
    @CsvSource({
        "encodeUriUserInfo(user@vividus.dev:pass), user%40vividus.dev:pass",
        "encodeUriHost(vividus.бел),               vividus.%D0%B1%D0%B5%D0%BB",
        "encodeUriPath(/path/with spaces/),        /path/with%20spaces/",
        "encodeUriPathSegment(path/segment),       path%2Fsegment",
        "encodeUriQuery(a&b=c d),                  a&b=c%20d",
        "encodeUriQueryParameter(a&b),             a%26b",
        "encodeUriFragment(frag ment),             frag%20ment"
    })
    void testExecute(String expression, String expected)
    {
        assertEquals(Optional.of(expected), processor.execute(expression));
    }
}
