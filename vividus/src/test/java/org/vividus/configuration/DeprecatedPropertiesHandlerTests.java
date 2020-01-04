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

package org.vividus.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;

import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DeprecatedPropertiesHandlerTests
{
    private static final String PROP1 = "norm1";

    private static final String PROP2 = "norm2";

    private static final String DEPR_PROP1 = "depr1";

    private static final String DEPR_PROP2 = "depr2";

    private static final String DEPR_PLACEHOLDER1 = "${depr1}";

    private static final String DEPR_PLACEHOLDER2 = "${depr2}";

    private static final String DEPR_PROPERTY_WARN_MSG = "Deprecated property found: '{}'. Use '{}' instead";

    private static final String DEPR_PLACEHOLDER_WARN_MSG = "Property '{}' uses deprecated placeholder '${{}}'."
            + " Use '${{}}' instead";

    private static final String VALUE = "value";

    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";

    private DeprecatedPropertiesHandler handler;

    @BeforeEach
    void beforeEach()
    {
        Properties deprecatedProps = new Properties();
        deprecatedProps.put(DEPR_PROP1, PROP1);
        deprecatedProps.put(DEPR_PROP2, PROP2);
        handler = new DeprecatedPropertiesHandler(deprecatedProps, PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);
    }

    @Test
    void testDeprecatedProperty()
    {
        DeprecatedPropertiesHandler spy = Mockito.spy(handler);

        spy.warnIfDeprecated(DEPR_PROP1, "deprVal1");
        verify(spy).warnDeprecated(DEPR_PROPERTY_WARN_MSG, DEPR_PROP1, PROP1);

        spy.warnIfDeprecated(PROP1, DEPR_PLACEHOLDER2);
        verify(spy).warnDeprecated(DEPR_PLACEHOLDER_WARN_MSG, PROP1, DEPR_PROP2, PROP2);

        spy.warnIfDeprecated(PROP2, DEPR_PLACEHOLDER1 + "something" + DEPR_PLACEHOLDER2);
        verify(spy).warnDeprecated(DEPR_PLACEHOLDER_WARN_MSG, PROP2, DEPR_PROP1, PROP1);
        verify(spy).warnDeprecated(DEPR_PLACEHOLDER_WARN_MSG, PROP2, DEPR_PROP2, PROP2);
    }

    @Test
    void testRemoveDeprecated()
    {
        Properties props = new Properties();
        props.put(PROP1, VALUE);
        props.put(PROP2, DEPR_PROP1);
        props.put(DEPR_PROP1, "blablabla");

        handler.removeDeprecated(props);
        assertEquals(2, props.size());
        assertFalse(props.containsKey(DEPR_PROP1));
    }

    @Test
    void testReplaceDeprecatedSimpleValue()
    {
        Properties props = new Properties();
        props.put(DEPR_PROP1, VALUE);
        props.put(PROP1, "simpleValue");
        handler.replaceDeprecated(props, props);
        assertEquals(VALUE, props.getProperty(PROP1));
    }

    @Test
    void testReplaceDeprecatedPropertyMissed()
    {
        Properties props = new Properties();
        props.put(DEPR_PROP1, VALUE);
        handler.replaceDeprecated(props, props);
        assertEquals(VALUE, props.getProperty(PROP1));
    }

    @Test
    void testReplaceDeprecatedPlaceholderValue()
    {
        String prop1Value = PLACEHOLDER_PREFIX + VALUE + PLACEHOLDER_SUFFIX;
        Properties props = new Properties();
        props.put(DEPR_PROP1, VALUE);
        props.put(PROP1, prop1Value);
        handler.replaceDeprecated(props, props);
        assertEquals(prop1Value, props.getProperty(PROP1));
    }

    @Test
    void testReplaceDeprecatedPlaceholderAndTextCombination()
    {
        String prop1Value = VALUE + PLACEHOLDER_PREFIX + VALUE + PLACEHOLDER_SUFFIX + VALUE;
        Properties props = new Properties();
        props.put(DEPR_PROP1, VALUE);
        props.put(PROP1, prop1Value);
        handler.replaceDeprecated(props, props);
        assertEquals(prop1Value, props.getProperty(PROP1));
    }

    @Test
    void testReplaceDeprecatedSinglePropertiesList()
    {
        DeprecatedPropertiesHandler spy = Mockito.spy(handler);
        Properties props = new Properties();
        spy.replaceDeprecated(props);
        verify(spy).replaceDeprecated(props, props);
    }
}
