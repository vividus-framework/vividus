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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.property.IPropertyParser;

@ExtendWith(MockitoExtension.class)
class ResourceLoadConfigurationTests
{
    private static final String VARIABLES_PROPERTY_FAMILY = "bdd.resource-loader";

    private static final String LOCALE_KEY = "locale";

    private static final String LOCALE_VALUE = "";

    private static final String BRAND_KEY = "brand";

    private static final String BRAND_VALUE = "cnc";

    private static final String MARKET_KEY = "market";

    private static final String MARKET_VALUE = "ca";

    private static final String NOBODYCARES_KEY = "nobodycares";

    private static final String NOBODYCARES_VALUE = null;

    private final Map<String, String> propsMap = new HashMap<>();

    @Mock
    private IPropertyParser propertyParser;

    @InjectMocks
    private ResourceLoadConfiguration resourceLoadConfiguration;

    private void initProperties(Map<String, String> props)
    {
        Mockito.when(propertyParser.getPropertyValuesByFamily(VARIABLES_PROPERTY_FAMILY)).thenReturn(props);
        resourceLoadConfiguration.init();
    }

    @Test
    void testGetResourceLoadParameters()
    {
        initProperties(propsMap);
        Map<String, String> actualMap = resourceLoadConfiguration.getResourceLoadParameters();
        assertThat(actualMap, Matchers.equalTo(propsMap));
    }

    @Test
    void testGetResourceLoadParametersValues()
    {
        propsMap.put(LOCALE_KEY, LOCALE_VALUE);
        propsMap.put(BRAND_KEY, BRAND_VALUE);
        propsMap.put(MARKET_KEY, MARKET_VALUE);
        propsMap.put(NOBODYCARES_KEY, NOBODYCARES_VALUE);
        initProperties(propsMap);
        List<String> actualValues = resourceLoadConfiguration.getResourceLoadParametersValues();
        assertThat(actualValues.size(), Matchers.equalTo(2));
        assertThat(actualValues, Matchers.containsInAnyOrder(BRAND_VALUE, MARKET_VALUE));
    }

    @Test
    void testGetResourceLoadParametersValuesNoProperties()
    {
        initProperties(new HashMap<>());
        List<String> actualValues = resourceLoadConfiguration.getResourceLoadParametersValues();
        assertNotNull(actualValues);
        assertTrue(actualValues.isEmpty());
    }

    @Test
    void testGetResourceLoadParametersNoProperties()
    {
        initProperties(new HashMap<>());
        Map<String, String> actualValues = resourceLoadConfiguration.getResourceLoadParameters();
        assertNotNull(actualValues);
        assertTrue(actualValues.isEmpty());
    }
}
