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

package org.vividus.http.expression;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiFunction;

import org.jbehave.core.expressions.DelegatingExpressionProcessor;
import org.jbehave.core.expressions.SingleArgExpressionProcessor;
import org.springframework.web.util.UriUtils;

public class UriExpressionProcessors extends DelegatingExpressionProcessor
{
    @SuppressWarnings("checkstyle:SingleSpaceSeparator")
    public UriExpressionProcessors()
    {
        super(List.of(
            createUriExpression("encodeUri",               UriUtils::encode),
            createUriExpression("encodeUriUserInfo",       UriUtils::encodeUserInfo),
            createUriExpression("encodeUriHost",           UriUtils::encodeHost),
            createUriExpression("encodeUriPath",           UriUtils::encodePath),
            createUriExpression("encodeUriPathSegment",    UriUtils::encodePathSegment),
            createUriExpression("encodeUriQuery",          UriUtils::encodeQuery),
            createUriExpression("encodeUriQueryParameter", UriUtils::encodeQueryParam),
            createUriExpression("encodeUriFragment",       UriUtils::encodeFragment),
            createUriExpression("decodeUri",               UriUtils::decode)
        ));
    }

    private static SingleArgExpressionProcessor<String> createUriExpression(String expressionName,
            BiFunction<String, Charset, String> transformer)
    {
        return new SingleArgExpressionProcessor<>(expressionName,
                input -> transformer.apply(input, StandardCharsets.UTF_8));
    }
}
