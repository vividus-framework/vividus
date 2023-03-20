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

package org.vividus.ui.web.expression;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jbehave.core.expressions.SingleArgExpressionProcessor;

public class DecodeDataUrlExpression extends SingleArgExpressionProcessor<Object>
{
    public DecodeDataUrlExpression()
    {
        super("decodeDataUrl", DecodeDataUrlExpression::decode);
    }

    private static Object decode(String dataUrlString)
    {
        DataUrl dataUrl = new DataUrl(dataUrlString);
        Validate.isTrue(dataUrl.data != null, "Supplied argument `%s` is invalid Data URL", dataUrlString);
        if (dataUrl.base64Encoded)
        {
            byte[] decoded = Base64.getDecoder().decode(dataUrl.data.getBytes(UTF_8));
            return dataUrl.text ? new String(decoded, UTF_8) : decoded;
        }
        return dataUrl.data;
    }

    private static final class DataUrl
    {
        private static final Pattern DATA_URL_PATTERN = Pattern.compile("data:([^;]*);?([^,]*),(.+)");
        private static final int DATA_GROUP = 3;
        private static final int MIME_TYPE_GROUP = 1;
        private static final int PARAMETERS_GROUP = 2;
        private final String dataUrlString;

        private String data;
        private boolean text;
        private boolean base64Encoded;

        private DataUrl(String dataUrlString)
        {
            this.dataUrlString = dataUrlString;
            parseDataUrl();
        }

        private void parseDataUrl()
        {
            Matcher matcher = DATA_URL_PATTERN.matcher(dataUrlString);
            if (matcher.find())
            {
                String mimeType = matcher.group(MIME_TYPE_GROUP);
                text = StringUtils.isBlank(mimeType) || mimeType.contains("text");
                String parameters = matcher.group(PARAMETERS_GROUP);
                base64Encoded = StringUtils.contains(parameters, "base64");
                data = matcher.group(DATA_GROUP);
            }
        }
    }
}
