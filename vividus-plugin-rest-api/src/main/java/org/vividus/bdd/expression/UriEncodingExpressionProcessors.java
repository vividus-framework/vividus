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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiFunction;

import javax.inject.Named;

import org.springframework.web.util.UriUtils;

@Named
public class UriEncodingExpressionProcessors extends DelegatingExpressionProcessor
{
    public UriEncodingExpressionProcessors()
    {
        super(List.of(
            createEncodingExpression("encodeUriUserInfo",       UriUtils::encodeUserInfo),
            createEncodingExpression("encodeUriHost",           UriUtils::encodeHost),
            createEncodingExpression("encodeUriPath",           UriUtils::encodePath),
            createEncodingExpression("encodeUriPathSegment",    UriUtils::encodePathSegment),
            createEncodingExpression("encodeUriQuery",          UriUtils::encodeQuery),
            createEncodingExpression("encodeUriQueryParameter", UriUtils::encodeQueryParam),
            createEncodingExpression("encodeUriFragment",       UriUtils::encodeFragment)
        ));
    }

    private static UnaryExpressionProcessor createEncodingExpression(String functionName,
            BiFunction<String, Charset, String> transformer)
    {
        return new UnaryExpressionProcessor(functionName, input -> transformer.apply(input, StandardCharsets.UTF_8));
    }
}
