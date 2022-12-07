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

package org.vividus.expression;

import org.apache.commons.lang3.StringUtils;

final class NormalizingArgumentsUtils
{
    private static final String WRAP_TOKEN = "\"\"\"";

    private NormalizingArgumentsUtils()
    {
    }

    static String normalize(String argument)
    {
        if (StringUtils.isNotBlank(argument))
        {
            String trimmed = argument.trim();
            boolean wrappedInQuotes = trimmed.startsWith(WRAP_TOKEN) && trimmed.endsWith(WRAP_TOKEN);
            if (wrappedInQuotes)
            {
                int wrapTokenLength = WRAP_TOKEN.length();
                return trimmed.substring(wrapTokenLength, trimmed.length() - wrapTokenLength);
            }
            return trimmed.replace("\\,", ",");
        }
        return argument;
    }
}
