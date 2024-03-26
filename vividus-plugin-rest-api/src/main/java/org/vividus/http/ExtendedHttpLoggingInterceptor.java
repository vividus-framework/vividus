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

import static org.apache.hc.core5.http.ContentType.APPLICATION_JSON;
import static org.apache.hc.core5.http.ContentType.APPLICATION_XML;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.handler.HttpResponseHandler;

public class ExtendedHttpLoggingInterceptor implements HttpRequestInterceptor, HttpResponseHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestExecutor.class);

    private static final String NEW_LINE = System.lineSeparator();
    private static final String HEADERS_FORMAT = String.format("%nHeaders:%n{}");
    private static final String BODY_FORMAT = String.format("%nBody:%n{}");

    private static final Set<String> LOGGED_CONTENT_TYPES = Set.of(APPLICATION_JSON.getMimeType(),
            APPLICATION_XML.getMimeType());

    private final boolean extendedLogging;

    public ExtendedHttpLoggingInterceptor(boolean extendedLogging)
    {
        this.extendedLogging = extendedLogging;
    }

    @Override
    public void process(HttpRequest request, EntityDetails entityDetails, HttpContext context) throws IOException
    {
        String argBrackets = " {}";
        LoggingEventBuilder loggingEventBuilder = LOGGER.atInfo();
        StringBuilder loggerFormat = new StringBuilder("Request:");

        ProtocolVersion protocolVersion = request.getVersion();
        if (protocolVersion != null)
        {
            loggingEventBuilder = loggingEventBuilder.addArgument(protocolVersion);
            loggerFormat.append(argBrackets);
        }
        loggingEventBuilder = loggingEventBuilder.addArgument(request);
        loggerFormat.append(argBrackets);

        if (extendedLogging)
        {
            loggingEventBuilder = loggingEventBuilder.addArgument(
                    () -> Stream.of(request.getHeaders()).map(Object::toString).collect(Collectors.joining(NEW_LINE)));
            loggerFormat.append(HEADERS_FORMAT);

            if (request instanceof HttpEntityContainer httpEntityContainer)
            {
                HttpEntity entity = httpEntityContainer.getEntity();
                if (entity instanceof StringEntity stringEntity)
                {
                    loggingEventBuilder = loggingEventBuilder.addArgument(() ->
                    {
                        try
                        {
                            return new String(stringEntity.getContent().readAllBytes(), StandardCharsets.UTF_8);
                        }
                        catch (IOException e)
                        {
                            return "Unable to get body content: " + e.getMessage();
                        }
                    });
                    loggerFormat.append(BODY_FORMAT);
                }
                else if (entity != null)
                {
                    int bodySizeInBytes = entity.getContent().available();
                    loggingEventBuilder = loggingEventBuilder.addArgument(bodySizeInBytes);
                    loggerFormat.append(NEW_LINE).append("Body: {} bytes of binary data");
                }
            }
        }
        loggingEventBuilder.log(loggerFormat.toString());
    }

    @Override
    public void handle(HttpResponse httpResponse)
    {
        LoggingEventBuilder loggingEventBuilder = LOGGER.atInfo().addArgument(httpResponse.getStatusCode())
                .addArgument(httpResponse.getFrom());
        StringBuilder loggerFormat = new StringBuilder("Response: status code {}, {}");

        if (extendedLogging)
        {
            Header[] headers = httpResponse.getResponseHeaders();
            loggingEventBuilder = loggingEventBuilder
                    .addArgument(() -> Stream.of(headers).map(Object::toString).collect(Collectors.joining(NEW_LINE)));
            loggerFormat.append(HEADERS_FORMAT);

            if (httpResponse.getResponseBody() != null)
            {
                String mimeType = ContentTypeHeaderParser.getMimeTypeFromHeadersWithDefault(headers);
                if (mimeType.startsWith("text/") || LOGGED_CONTENT_TYPES.contains(mimeType))
                {
                    loggingEventBuilder = loggingEventBuilder.addArgument(httpResponse::getResponseBodyAsString);
                    loggerFormat.append(BODY_FORMAT);
                }
            }
        }
        loggingEventBuilder.log(loggerFormat.toString());
    }
}
