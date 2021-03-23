/*
 * Copyright 2021 the original author or authors.
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

package org.vividus.azure.storage.queue.model;

import java.util.Objects;

public class Queue
{
    private String name;
    private String endpoint;
    private String sasToken;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public String getSasToken()
    {
        return sasToken;
    }

    public void setSasToken(String sasToken)
    {
        this.sasToken = sasToken;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(endpoint, name, sasToken);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Queue other = (Queue) obj;
        return Objects.equals(endpoint, other.endpoint) && Objects.equals(name, other.name)
                && Objects.equals(sasToken, other.sasToken);
    }
}
