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

package org.vividus.bdd.resource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.vividus.util.property.IPropertyParser;

public class ResourceLoadConfiguration implements IResourceLoadConfiguration
{
    private static final String VARIABLES_PROPERTY_FAMILY = "bdd.resource-loader";

    private IPropertyParser propertyParser;

    private Map<String, String> resourceLoadParameters;

    public void init()
    {
        resourceLoadParameters = propertyParser.getPropertyValuesByFamily(VARIABLES_PROPERTY_FAMILY);
    }

    @Override
    public Map<String, String> getResourceLoadParameters()
    {
        return resourceLoadParameters;
    }

    @Override
    public List<String> getResourceLoadParametersValues()
    {
        return resourceLoadParameters.values().stream().filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.toList());
    }

    public void setPropertyParser(IPropertyParser propertyParser)
    {
        this.propertyParser = propertyParser;
    }
}
