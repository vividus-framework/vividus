/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.http.validation.model;

import java.net.URI;
import java.util.Objects;

public abstract class AbstractResourceValidation<T extends AbstractResourceValidation<T>> implements Comparable<T>
{
    private URI uri;
    private int statusCode = -1;
    private CheckStatus checkStatus;

    protected AbstractResourceValidation(URI uri)
    {
        this.uri = uri;
    }

    public URI getUri()
    {
        return uri;
    }

    public void setUri(URI uri)
    {
        this.uri = uri;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
    }

    public CheckStatus getCheckStatus()
    {
        return checkStatus;
    }

    public void setCheckStatus(CheckStatus checkStatus)
    {
        this.checkStatus = checkStatus;
    }

    public abstract T copy();

    protected void copyParameters(AbstractResourceValidation<T> newValidation)
    {
        newValidation.statusCode = this.statusCode;
        newValidation.uri = this.uri;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uri);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof AbstractResourceValidation))
        {
            return false;
        }
        @SuppressWarnings("unchecked")
        AbstractResourceValidation<T> other = (AbstractResourceValidation<T>) obj;
        return Objects.equals(uri.toString(), other.uri.toString());
    }

    @Override
    public int compareTo(T object)
    {
        int result = Integer.compare(this.getCheckStatus().getWeight(), object.getCheckStatus().getWeight());
        if (0 == result && this.getUri() != null && object.getUri() != null)
        {
            return this.getUri().toString().compareTo(object.getUri().toString());
        }
        return result;
    }
}
