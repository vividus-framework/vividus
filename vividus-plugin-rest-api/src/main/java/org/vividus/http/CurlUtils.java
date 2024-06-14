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

package org.vividus.http;

import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;

public final class CurlUtils
{
    private static final Pattern NAME_FILE_NAME_PATTERN = Pattern.compile("name=\"(.+)\"; filename=\"(.+)\"");
    private static final Pattern NAME_CONTENT_PATTERN = Pattern.compile("name=\"(.+)\"\r\nContent-Type:.+\r\n\r\n(.*)");
    private static final String SINGLE_QUOTE = "'";
    private static final String END_OF_LINE = " \\\n";
    private static final String DOUBLE_QUOTE = "\"";

    private CurlUtils()
    {
    }

    public static String buildCurlCommand(HttpRequest request, String mimeType,
                                          byte[] body, boolean binary) throws URISyntaxException
    {
        StringBuilder curlCommand = new StringBuilder("curl ");
        appendMethodAndUri(curlCommand, request);
        appendHeaders(curlCommand, request.getHeaders());
        if (body != null)
        {
            Charset charset = ContentTypeHeaderParser.getCharset(request.getHeaders()).orElse(StandardCharsets.UTF_8);
            appendBody(curlCommand, mimeType, charset, body, binary);
        }
        return curlCommand.toString();
    }

    private static void appendMethodAndUri(StringBuilder curlCommand, HttpRequest request) throws URISyntaxException
    {
        curlCommand.append("-X ").append(request.getMethod()).append(" '")
                .append(request.getUri()).append(SINGLE_QUOTE);
    }

    private static void appendHeaders(StringBuilder curlCommand, Header... headers)
    {
        Stream.of(headers).forEach(h -> curlCommand.append(END_OF_LINE)
                .append("-H '").append(h.getName()).append(": ").append(h.getValue()).append(SINGLE_QUOTE));
    }

    private static void appendBody(StringBuilder curlCommand, String mimeType, Charset charset,
                                   byte[] body, boolean binary)
    {
        String bodyAsString = new String(body, charset);
        if (binary)
        {
            curlCommand.append(END_OF_LINE).append("--data-binary '@<path_to_binary_content>'");
            return;
        }
        if (mimeType.contains("multipart"))
        {
            appendMultipartData(curlCommand, bodyAsString);
            return;
        }
        curlCommand.append(END_OF_LINE).append("-d '").append(bodyAsString).append(SINGLE_QUOTE);
    }

    private static void appendMultipartData(StringBuilder curlCommand, String bodyAsString)
    {
        String regex = bodyAsString.split("\\R", 2)[0];
        String[] formDataArray = bodyAsString.split(regex + ".*");

        Stream.of(formDataArray).forEach(e ->
        {
            Matcher matcher = NAME_FILE_NAME_PATTERN.matcher(e);
            String formStringStart = "-F '";
            if (matcher.find())
            {
                curlCommand.append(END_OF_LINE).append(formStringStart).append(matcher.group(1))
                        .append("=@\"<path-to-file>").append(matcher.group(2))
                        .append(DOUBLE_QUOTE).append(SINGLE_QUOTE);
            }
            else
            {
                matcher = NAME_CONTENT_PATTERN.matcher(e);
                if (matcher.find())
                {
                    curlCommand.append(END_OF_LINE).append(formStringStart).append(matcher.group(1))
                            .append("=\"").append(matcher.group(2)).append(DOUBLE_QUOTE).append(SINGLE_QUOTE);
                }
            }
        });
    }
}
