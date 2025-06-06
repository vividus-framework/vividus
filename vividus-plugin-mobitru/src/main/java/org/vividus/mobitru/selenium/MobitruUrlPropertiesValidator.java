/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.mobitru.selenium;

import java.net.URI;
import java.util.Map;

import org.vividus.util.property.IPropertyParser;

public class MobitruUrlPropertiesValidator implements MobitruPropertiesValidator
{
    private static final String INCORRECT_PROPERTY_FOR_MOBITRU_MESSAGE =
            "Incorrect 'selenium.grid.%s' property for Mobitru: %s";
    private static final String URL = "url";
    private static final String HOST = "host";

    private final IPropertyParser propertyParser;

    public MobitruUrlPropertiesValidator(IPropertyParser propertyParser)
    {
        this.propertyParser = propertyParser;
    }

    @Override
    public void validate()
    {
        Map<String, String> gridProps = propertyParser.getPropertyValuesByPrefix("selenium.grid.");
        String host = gridProps.get(HOST);
        if (!host.contains("mobitru"))
        {
            throw new IllegalStateException(String.format(INCORRECT_PROPERTY_FOR_MOBITRU_MESSAGE, HOST, host));
        }
        URI uri = URI.create(gridProps.get(URL));
        String authority = gridProps.get("username") + ":" + gridProps.get("password") + "@" + host;
        if (!uri.getAuthority().contains(authority))
        {
            throw new IllegalStateException(String.format(INCORRECT_PROPERTY_FOR_MOBITRU_MESSAGE, URL, uri));
        }
    }
}
