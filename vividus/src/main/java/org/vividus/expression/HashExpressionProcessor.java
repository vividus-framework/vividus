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

package org.vividus.expression;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.vividus.converter.FluentTrimmedEnumConverter;
import org.vividus.util.ResourceUtils;

@Named
public class HashExpressionProcessor extends AbstractExpressionProcessor<String>
{
    private static final Pattern HASH_PATTERN = Pattern.compile("^(calculate(?:File)?Hash)\\((.+), (.+)\\)$",
            Pattern.CASE_INSENSITIVE);

    private static final int INPUT_CALCULATE_TYPE_GROUP = 1;
    private static final int INPUT_ALGORITHM_GROUP = 2;
    private static final int INPUT_DATA_GROUP = 3;

    private final FluentTrimmedEnumConverter fluentTrimmedEnumConverter;

    protected HashExpressionProcessor(FluentTrimmedEnumConverter fluentTrimmedEnumConverter)
    {
        super(HASH_PATTERN);
        this.fluentTrimmedEnumConverter = fluentTrimmedEnumConverter;
    }

    @Override
    protected String evaluateExpression(Matcher expressionMatcher)
    {
        HashAlgorithmType hashAlgorithmType = (HashAlgorithmType) fluentTrimmedEnumConverter
                .convertValue(expressionMatcher.group(INPUT_ALGORITHM_GROUP).replace("-", ""), HashAlgorithmType.class);
        String data = expressionMatcher.group(INPUT_DATA_GROUP);
        if ("calculateHash".equalsIgnoreCase(expressionMatcher.group(INPUT_CALCULATE_TYPE_GROUP)))
        {
            return hashAlgorithmType.getHash(data);
        }
        else
        {
            try
            {
                return hashAlgorithmType.getHash(ResourceUtils.loadResourceOrFileAsByteArray(data));
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
        }
    }
}
