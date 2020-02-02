/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.batch;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class BatchResourceConfiguration
{
    private String resourceLocation;
    private List<String> resourceIncludePatterns = List.of();
    private List<String> resourceExcludePatterns = List.of();

    public String getResourceLocation()
    {
        return resourceLocation;
    }

    public void setResourceLocation(String resourceLocation)
    {
        this.resourceLocation = resourceLocation;
    }

    public List<String> getResourceIncludePatterns()
    {
        return Collections.unmodifiableList(resourceIncludePatterns);
    }

    public void setResourceIncludePatterns(String resourceIncludePatterns)
    {
        this.resourceIncludePatterns = convertToList(resourceIncludePatterns);
    }

    public List<String> getResourceExcludePatterns()
    {
        return Collections.unmodifiableList(resourceExcludePatterns);
    }

    public void setResourceExcludePatterns(String resourceExcludePatterns)
    {
        this.resourceExcludePatterns = convertToList(resourceExcludePatterns);
    }

    private List<String> convertToList(String list)
    {
        if (list == null)
        {
            return List.of();
        }
        return List.of(StringUtils.split(list, ','));
    }
}
