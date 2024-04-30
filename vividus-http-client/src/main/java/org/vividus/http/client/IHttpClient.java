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

package org.vividus.http.client;

import java.io.IOException;
import java.net.URI;

import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHost;

public interface IHttpClient
{
    HttpHost getHttpHost();

    /**
     * Executes HTTP GET request and handles URI user info in the fully-automated way: converts user info provided as
     * a part of URI to non-preemptive basic authentication configuration.
     *
     * @param uri URI to execute HTTP GET request
     * @return HTTP response
     * @throws IOException in case of any I/O errors
     */
    HttpResponse doHttpGet(URI uri) throws IOException;

    /**
     * Executes HTTP GET request and handles URI user info in the fully-automated way: converts user info provided as
     * a part of URI to basic authentication configuration.
     *
     * @param uri                               URI to execute HTTP GET request
     * @param usePreemptiveBasicAuthIfAvailable If true, preemptive basic authentication configuration will be used,
     *                                          otherwise - non-preemptive (the parameters is used only when URI has
     *                                          user info)
     * @return HTTP response
     * @throws IOException in case of any I/O errors
     */
    HttpResponse doHttpGet(URI uri, boolean usePreemptiveBasicAuthIfAvailable) throws IOException;

    /**
     * Executes HTTP HEAD request and handles URI user info in the fully-automated way: converts user info provided as
     * a part of URI to non-preemptive basic authentication configuration.
     *
     * @param uri URI to execute HTTP HEAD request
     * @return HTTP response
     * @throws IOException in case of any I/O errors
     */
    HttpResponse doHttpHead(URI uri) throws IOException;

    /**
     * Executes HTTP HEAD request and handles URI user info in the fully-automated way: converts user info provided as
     * a part of URI to basic authentication configuration.
     *
     * @param uri                               URI to execute HTTP HEAD request
     * @param usePreemptiveBasicAuthIfAvailable If true, preemptive basic authentication configuration will be used,
     *                                          otherwise - non-preemptive (the parameters is used only when URI has
     *                                          user info)
     * @return HTTP response
     * @throws IOException in case of any I/O errors
     */
    HttpResponse doHttpHead(URI uri, boolean usePreemptiveBasicAuthIfAvailable) throws IOException;

    HttpResponse execute(ClassicHttpRequest request) throws IOException;

    HttpResponse execute(ClassicHttpRequest request, HttpClientContext context) throws IOException;
}
