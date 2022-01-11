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

import javax.inject.Named;

@Named
public class RemoveWrappingDoubleQuotesExpressionProcessor extends UnaryExpressionProcessor
{
    private static final char DOUBLE_QUOTE = '"';

    public RemoveWrappingDoubleQuotesExpressionProcessor()
    {
        super("removeWrappingDoubleQuotes", RemoveWrappingDoubleQuotesExpressionProcessor::removeWrappingQuotes);
    }

    /**
     * Removes wrapping quotes from string
     * @param inputData value to remove quotes
     * @return input data without wrapping quotes if they were exist, otherwise - input data
     */
    private static String removeWrappingQuotes(String inputData)
    {
        if (inputData.length() > 1 && inputData.charAt(0) == DOUBLE_QUOTE
                && inputData.charAt(inputData.length() - 1) == DOUBLE_QUOTE)
        {
            return inputData.substring(1, inputData.length() - 1);
        }
        return inputData;
    }
}
