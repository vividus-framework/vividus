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

package org.vividus.http;

import java.net.URI;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpTrace;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.vividus.http.exception.HttpRequestBuildException;

public enum HttpMethod
{
    GET
    {
        @Override
        public ClassicHttpRequest createRequest(URI uri)
        {
            return new HttpGet(uri);
        }
    },
    HEAD
    {
        @Override
        public ClassicHttpRequest createRequest(URI uri)
        {
            return new HttpHead(uri);
        }
    },
    DELETE
    {
        @Override
        public ClassicHttpRequest createRequest(URI uri)
        {
            return new HttpDelete(uri);
        }

        @Override
        ClassicHttpRequest createEntityEnclosingRequest(URI uri)
        {
            return new HttpDelete(uri);
        }
    },
    OPTIONS
    {
        @Override
        public ClassicHttpRequest createRequest(URI uri)
        {
            return new HttpOptions(uri);
        }
    },
    PATCH
    {
        @Override
        public ClassicHttpRequest createRequest(URI uri) throws HttpRequestBuildException
        {
            throw new HttpRequestBuildException(generateExceptionMessage("must"));
        }

        @Override
        ClassicHttpRequest createEntityEnclosingRequest(URI uri)
        {
            return new HttpPatch(uri);
        }
    },
    POST
    {
        @Override
        ClassicHttpRequest createEntityEnclosingRequest(URI uri)
        {
            return new HttpPost(uri);
        }

        @Override
        public ClassicHttpRequest createRequest(URI uri)
        {
            return new HttpPost(uri);
        }
    },
    PUT
    {
        @Override
        ClassicHttpRequest createEntityEnclosingRequest(URI uri)
        {
            return new HttpPut(uri);
        }

        @Override
        public ClassicHttpRequest createRequest(URI uri)
        {
            return new HttpPut(uri);
        }
    },
    TRACE
    {
        @Override
        public ClassicHttpRequest createRequest(URI uri)
        {
            return new HttpTrace(uri);
        }
    },
    DEBUG
    {
        @Override
        public ClassicHttpRequest createRequest(URI uri)
        {
            return new HttpDebug(uri);
        }
    };

    public abstract ClassicHttpRequest createRequest(URI uri) throws HttpRequestBuildException;

    ClassicHttpRequest createEntityEnclosingRequest(URI uri) throws HttpRequestBuildException
    {
        throw new HttpRequestBuildException(generateExceptionMessage("can't"));
    }

    public ClassicHttpRequest createEntityEnclosingRequest(URI uri, HttpEntity httpEntity)
            throws HttpRequestBuildException
    {
        ClassicHttpRequest request = createEntityEnclosingRequest(uri);
        request.setEntity(httpEntity);
        return request;
    }

    protected String generateExceptionMessage(String verb)
    {
        return "HTTP " + name() + " request " + verb + " include body";
    }
}
