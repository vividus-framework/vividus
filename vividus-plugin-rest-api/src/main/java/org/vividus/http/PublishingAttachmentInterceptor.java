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

package org.vividus.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.RequestLine;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.handler.HttpResponseHandler;
import org.vividus.reporter.event.IAttachmentPublisher;

public class PublishingAttachmentInterceptor implements HttpRequestInterceptor, HttpResponseHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishingAttachmentInterceptor.class);

    private final IAttachmentPublisher attachmentPublisher;

    public PublishingAttachmentInterceptor(IAttachmentPublisher attachmentPublisher)
    {
        this.attachmentPublisher = attachmentPublisher;
    }

    @Override
    public void process(HttpRequest request, HttpContext context)
    {
        byte[] body = null;
        String mimeType = null;
        if (request instanceof HttpEntityEnclosingRequest)
        {
            HttpEntityEnclosingRequest requestWithBody = (HttpEntityEnclosingRequest) request;
            HttpEntity entity = requestWithBody.getEntity();
            if (entity != null)
            {
                mimeType = getMimeType(requestWithBody.getAllHeaders())
                        .orElseGet(() ->
                                Optional.ofNullable(ContentType.getLenient(entity))
                                        .orElse(ContentType.DEFAULT_TEXT).getMimeType()
                        );
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream((int) entity.getContentLength()))
                {
                    // https://github.com/apache/httpcomponents-client/commit/09cefc2b8970eea56d81b1a886d9bb769a48daf3
                    entity.writeTo(baos);
                    body = baos.toByteArray();
                }
                catch (IOException e)
                {
                    LOGGER.error("Error is occurred at HTTP message parsing", e);
                }
            }
        }
        RequestLine requestLine = request.getRequestLine();
        String attachmentTitle = String.format("Request: %s %s", requestLine.getMethod(), requestLine.getUri());
        attachApiMessage(attachmentTitle, request.getAllHeaders(), body, mimeType, -1);
    }

    @Override
    public void handle(HttpResponse response) throws IOException
    {
        Header[] headers = response.getResponseHeaders();
        String attachmentTitle = String.format("Response: %s %s", response.getMethod(), response.getFrom());
        String mimeType = getMimeType(headers).orElseGet(ContentType.DEFAULT_TEXT::getMimeType);
        attachApiMessage(attachmentTitle, headers, response.getResponseBody(), mimeType, response.getStatusCode());
    }

    private void attachApiMessage(String title, Header[] headers, byte[] body, String mimeType, int statusCode)
    {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("headers", headers);
        dataMap.put("body", body != null ? new String(body, StandardCharsets.UTF_8) : null);
        dataMap.put("bodyContentType", mimeType);
        dataMap.put("statusCode", statusCode);

        attachmentPublisher.publishAttachment("/org/vividus/http/attachment/api-message.ftl", dataMap, title);
    }

    private Optional<String> getMimeType(Header... headers)
    {
        return Stream.of(headers)
                .filter(h -> HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(h.getName())
                        && StringUtils.isNoneBlank(h.getValue()))
                .findFirst()
                .map(Header::getElements)
                .map(elements -> elements[0])
                .map(HeaderElement::getName);
    }
}
