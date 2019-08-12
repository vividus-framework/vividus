/*
 * Copyright 2019 the original author or authors.
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

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

public interface IHttpClient
{
    HttpHost getHttpHost();

    HttpResponse doHttpGet(URI uri) throws IOException;

    HttpResponse doHttpHead(URI uri) throws IOException;

    HttpResponse doHttpGet(URI uri, HttpContext context) throws IOException;

    HttpResponse doHttpHead(URI uri, HttpContext context) throws IOException;

    HttpResponse execute(HttpUriRequest request) throws IOException;

    HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException;
}
