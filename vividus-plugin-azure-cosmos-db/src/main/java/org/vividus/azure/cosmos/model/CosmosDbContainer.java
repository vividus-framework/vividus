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

package org.vividus.azure.cosmos.model;

import java.util.Objects;

public class CosmosDbContainer
{
    private String dbKey;
    private String id;

    public String getDbKey()
    {
        return dbKey;
    }

    public void setDbKey(String dbKey)
    {
        this.dbKey = dbKey;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(dbKey, id);
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
        CosmosDbContainer other = (CosmosDbContainer) obj;
        return Objects.equals(dbKey, other.dbKey) && Objects.equals(id, other.id);
    }
}
