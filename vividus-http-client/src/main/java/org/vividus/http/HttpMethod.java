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
        public HttpRequestBase createEmptyRequest()
        {
            return new HttpGet();
        }
    },
    HEAD
    {
        @Override
        public HttpRequestBase createEmptyRequest()
        {
            return new HttpHead();
        }
    },
    DELETE
    {
        @Override
        public HttpRequestBase createEmptyRequest()
        {
            return new HttpDelete();
        }

        @Override
        public HttpEntityEnclosingRequestBase createEmptyEnclosingEntityRequest()
        {
            return new HttpDeleteWithBody();
        }
    },
    OPTIONS
    {
        @Override
        public HttpRequestBase createEmptyRequest()
        {
            return new HttpOptions();
        }
    },
    PATCH
    {
        @Override
        public HttpEntityEnclosingRequestBase createEmptyEnclosingEntityRequest()
        {
            return new HttpPatch();
        }
    },
    POST
    {
        @Override
        public HttpEntityEnclosingRequestBase createEmptyEnclosingEntityRequest()
        {
            return new HttpPost();
        }
    },
    PUT
    {
        @Override
        public HttpEntityEnclosingRequestBase createEmptyEnclosingEntityRequest()
        {
            return new HttpPut();
        }

        @Override
        public HttpRequestBase createEmptyRequest()
        {
            return new HttpPutWithoutBody();
        }
    },
    TRACE
    {
        @Override
        public HttpRequestBase createEmptyRequest()
        {
            return new HttpTrace();
        }
    };

    public HttpRequestBase createEmptyRequest()
    {
        throw new IllegalStateException(generateExceptionMessage("must"));
    }

    public HttpEntityEnclosingRequestBase createEmptyEnclosingEntityRequest()
    {
        throw new IllegalStateException(generateExceptionMessage("can't"));
    }

    private String generateExceptionMessage(String verb)
    {
        return "HTTP " + name() + " request " + verb + " include body";
    }
}
