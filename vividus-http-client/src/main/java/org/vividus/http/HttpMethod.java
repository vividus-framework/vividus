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

package org.vividus.http;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;

public enum HttpMethod
{
    GET
    {
        @Override
        public HttpRequestBase createRequest(URI uri)
        {
            return new HttpGet(uri);
        }
    },
    HEAD
    {
        @Override
        public HttpRequestBase createRequest(URI uri)
        {
            return new HttpHead(uri);
        }
    },
    DELETE
    {
        @Override
        public HttpRequestBase createRequest(URI uri)
        {
            return new HttpDelete(uri);
        }

        @Override
        HttpEntityEnclosingRequestBase createEntityEnclosingRequest(URI uri)
        {
            return new HttpDeleteWithBody(uri);
        }
    },
    OPTIONS
    {
        @Override
        public HttpRequestBase createRequest(URI uri)
        {
            return new HttpOptions(uri);
        }
    },
    PATCH
    {
        @Override
        HttpEntityEnclosingRequestBase createEntityEnclosingRequest(URI uri)
        {
            return new HttpPatch(uri);
        }
    },
    POST
    {
        @Override
        HttpEntityEnclosingRequestBase createEntityEnclosingRequest(URI uri)
        {
            return new HttpPost(uri);
        }
    },
    PUT
    {
        @Override
        HttpEntityEnclosingRequestBase createEntityEnclosingRequest(URI uri)
        {
            return new HttpPut(uri);
        }

        @Override
        public HttpRequestBase createRequest(URI uri)
        {
            return new HttpPutWithoutBody(uri);
        }
    },
    TRACE
    {
        @Override
        public HttpRequestBase createRequest(URI uri)
        {
            return new HttpTrace(uri);
        }
    },
    DEBUG
    {
        @Override
        public HttpRequestBase createRequest(URI uri)
        {
            return new HttpDebug(uri);
        }
    };

    public HttpRequestBase createRequest(URI uri)
    {
        throw new IllegalStateException(generateExceptionMessage("must"));
    }

    HttpEntityEnclosingRequestBase createEntityEnclosingRequest(URI uri)
    {
        throw new IllegalStateException(generateExceptionMessage("can't"));
    }

    public HttpEntityEnclosingRequestBase createEntityEnclosingRequest(URI uri, HttpEntity httpEntity)
    {
        HttpEntityEnclosingRequestBase request = createEntityEnclosingRequest(uri);
        request.setEntity(httpEntity);
        return request;
    }

    private String generateExceptionMessage(String verb)
    {
        return "HTTP " + name() + " request " + verb + " include body";
    }
}
