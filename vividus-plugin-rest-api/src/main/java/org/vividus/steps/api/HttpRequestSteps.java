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

package org.vividus.steps.api;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Timeout;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestExecutor;
import org.vividus.http.HttpTestContext;
import org.vividus.model.RequestPartType;
import org.vividus.steps.DataWrapper;

public class HttpRequestSteps
{
    private static final String NAME = "name";
    private static final String VALUE = "value";

    private String apiEndpoint;
    private final HttpTestContext httpTestContext;
    private final HttpRequestExecutor httpRequestExecutor;

    public HttpRequestSteps(HttpTestContext httpTestContext, HttpRequestExecutor httpRequestExecutor)
    {
        this.httpTestContext = httpTestContext;
        this.httpRequestExecutor = httpRequestExecutor;
    }

    /**
     * Sets HTTP request body that will be used while executing request. In case of textual content the default HTTP
     * request header with name 'Content-Type' and value 'text/plain; charset=UTF-8' is set. No HTTP request header
     * is set in case of binary content.
     *
     * @param content HTTP request body
     */
    @Given("request body: $content")
    public void request(DataWrapper content)
    {
        Object data = content.getData();
        HttpEntity requestEntity = data instanceof String ? new StringEntity((String) data, StandardCharsets.UTF_8)
                : new ByteArrayEntity((byte[]) data, null);
        httpTestContext.putRequestEntity(requestEntity);
    }

    /**
     * Sets application/x-www-form-urlencoded request entity that will be used while executing request.
     * HTTP request header with name 'Content-Type' and value 'application/x-www-form-urlencoded; charset=UTF-8'
     * is set.
     * <br>Example:
     * <code>
     *   <br>Given form data request:
     *   <br>|name     |value |
     *   <br>|firstName|Ivan  |
     *   <br>|lastName |Ivanov|
     * </code>
     * <br>
     * @param parameters ExamplesTable representing list of parameters with columns "name" and "value" specifying
     *                   form data request
     */
    @Given("form data request:$parameters")
    public void putUrlEncodedRequest(ExamplesTable parameters)
    {
        UrlEncodedFormEntity requestEntity = parameters.getRowsAsParameters(true).stream()
                .map(row -> new BasicNameValuePair(row.valueAs(NAME, String.class), row.valueAs(VALUE, String.class)))
                .collect(Collectors.collectingAndThen(toList(), params -> new UrlEncodedFormEntity(params,
                        StandardCharsets.UTF_8)));
        httpTestContext.putRequestEntity(requestEntity);
    }

    /**
     * Sets multipart request entity that will be used while executing request
     * <div>Example:</div>
     * <code>
     * <br>Given multipart request:
     * <br>|type|name    |value         |contentType|fileName       |
     * <br>|file|file-key|/data/file.txt|text/plain |anotherName.txt|
     * </code>
     * <br>
     * <br>where
     * <ul>
     *   <li><code>type</code> is one of request part types: STRING, FILE, BINARY</li>
     *   <li><code>name</code> is request part name</li>
     *   <li><code>value</code>
     *   <ul>
     *       <li>for <code>FILE</code> part type - the resource name or the file path</li>
     *       <li>for <code>STRING</code> or <code>STRING</code> part type - the actual content</li>
     *   </ul>
     *   </li>
     *   <li><code>contentType</code> is the content type</li>
     *   <li><code>fileName</code> is the name of the file contained in this request part. The parameter is not allowed
     *   for STRING part type, but it's required for BINARY one and optional for FILE part type</li>
     * </ul>
     * @param requestParts HTTP request parts
     * @throws IOException if an I/O error occurs
     */
    @Given("multipart request:$requestParts")
    public void putMultipartRequest(ExamplesTable requestParts) throws IOException
    {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        for (Parameters row : requestParts.getRowsAsParameters(true))
        {
            RequestPartType requestPartType = row.valueAs("type", RequestPartType.class);
            String name = row.valueAs(NAME, String.class);
            String value = row.valueAs(VALUE, String.class);
            String contentTypeStr = row.valueAs("contentType", String.class, null);
            String fileName = row.valueAs("fileName", String.class, null);
            ContentType contentType = StringUtils.isBlank(contentTypeStr) ? requestPartType.getDefaultContentType()
                    : ContentType.parse(contentTypeStr);
            ContentBody part = StringUtils.isBlank(fileName) ? requestPartType.createPart(value, contentType)
                    : requestPartType.createPart(value, contentType, fileName);
            multipartEntityBuilder.addPart(name, part);
        }
        httpTestContext.putRequestEntity(multipartEntityBuilder.build());
    }

    /**
     * Set up request headers that will be used while sending request
     * @param headers ExamplesTable representing list of headers with columns "name" and "value" specifying HTTP header
     * names and values respectively
     */
    @When("I set request headers:$headers")
    public void setUpRequestHeaders(ExamplesTable headers)
    {
        performWithHeaders(headers, httpTestContext::putRequestHeaders);
    }

    /**
     * Add request headers
     * @param headers ExamplesTable representing list of headers with columns "name" and "value" specifying HTTP header
     * names and values respectively
     */
    @When("I add request headers:$headers")
    public void addRequestHeaders(ExamplesTable headers)
    {
        performWithHeaders(headers, httpTestContext::addRequestHeaders);
    }

    /**
     * Executes the HTTP request to access a resource on the server identified by the URL. The step uses previously
     * set HTTP headers and request body. The response HTTP headers, response status code and response body can be
     * accessed by the corresponding steps and dynamic variables.
     *
     * @param httpMethod The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods">HTTP method</a>
     * @param url        The URL of the resource on the server
     * @throws IOException If an input or output exception occurred
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/">The HTTP methods.</a>
     */
    @When("I execute HTTP $httpMethod request for resource with URL `$url`")
    public void executeHttpRequest(HttpMethod httpMethod, String url) throws IOException
    {
        httpRequestExecutor.executeHttpRequest(httpMethod, url, Optional.empty());
    }

    /**
     * Executes the HTTP request for a resource identified by the URL relative to the base URL specified by the
     * <b>rest-api.http.endpoint</b> property. The step uses previously set HTTP headers and request body. The
     * HTTP response headers, status code and body can be accessed via the corresponding steps and dynamic variables.
     *
     * @param httpMethod  The <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods">HTTP method</a>
     * @param relativeURL The relative URL of the resource on the server
     * @throws IOException If an input or output exception occurred
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/">The HTTP methods.</a>
     */
    @When("I execute HTTP $httpMethod request for resource with relative URL `$relativeURL`")
    public void executeHttpRequestToRelativeURL(HttpMethod httpMethod, String relativeURL) throws IOException
    {
        httpRequestExecutor.executeHttpRequest(httpMethod, apiEndpoint, Optional.of(relativeURL));
    }

    /**
     * Set up custom request configurations
     * @param configItems Table representing list of configuration items with columns "name" and "value"
     *  specifying their names and values respectively.
     * Available configs:
     * <ul>
     * <li>expectContinueEnabled (boolean, default:{@code false})
     *  - determines whether the 'Expect: 100-Continue' handshake is enabled</li>
     * <li>redirectsEnabled (boolean, default:{@code true})
     *  - determines whether redirects should be handled automatically</li>
     * <li>circularRedirectsAllowed (boolean, default:{@code false})
     *  - determines whether circular redirects (redirects to the same location) should be allowed</li>
     * <li>authenticationEnabled (boolean, default:{@code true})
     *  - determines whether authentication should be handled automatically</li>
     * <li>contentCompressionEnabled (boolean, default:{@code true})
     *  - determines whether the target server is requested to compress content</li>
     * <li>maxRedirects (int, default:{@code 50}) - returns the maximum number of redirects to be followed</li>
     * <li>connectionRequestTimeout (int, default:{@code 180000})
     *  - returns the timeout in milliseconds used when requesting a connection from the connection manager</li>
     * <li>connectTimeout (int, default:{@code 180000})
     *  - determines the timeout in milliseconds until a connection is established</li>
     * <li>responseTimeout (int, default:{@code 0})
     *  - the timeout until arrival of a response from the opposite endpoint. A timeout value of zero is interpreted
     *    as an infinite timeout.</li>
     * <li>cookieSpec (String, default:{@code <not set>})
     *  - determines the name of the cookie specification to be used for HTTP state management</li>
     * </ul>
     * @throws ReflectiveOperationException if any unknown parameter is set in configuration items
     */
    @When("I set HTTP request configuration:$configItems")
    public void setCustomRequestConfig(ExamplesTable configItems) throws ReflectiveOperationException
    {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        for (Entry<String, String> entry : configItems.getRow(0).entrySet())
        {
            Field field = requestConfigBuilder.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);
            field.set(requestConfigBuilder, castType(field.getType().getName(), entry.getValue()));
        }
        httpTestContext.putRequestConfig(requestConfigBuilder.build());
    }

    private void performWithHeaders(ExamplesTable headers, Consumer<List<Header>> headersConsumer)
    {
        List<Header> requestHeaders = headers.getRowsAsParameters(true).stream()
                .map(row -> new BasicHeader(row.valueAs(NAME, String.class), row.valueAs(VALUE, String.class)))
                .collect(toList());
        headersConsumer.accept(requestHeaders);
    }

    private static Object castType(String typeName, String value)
    {
        switch (typeName)
        {
            case "boolean":
                return Boolean.parseBoolean(value);
            case "int":
                return Integer.parseInt(value);
            case "org.apache.hc.core5.util.Timeout":
                return Timeout.ofMilliseconds(Integer.parseInt(value));
            default:
                return value;
        }
    }

    public void setApiEndpoint(String apiEndpoint)
    {
        this.apiEndpoint = apiEndpoint;
    }
}
