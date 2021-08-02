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

package org.vividus.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DeprecatedPropertiesHandlerTests
{
    private static final String FALSE = "false";

    private static final String TRUE = "true";

    private static final String PROP1 = "norm1";

    private static final String PROP2 = "norm2";

    private static final String DEPR_PROP1 = "deprecated-1";
    private static final String DEPR_PROP2 = "deprecated-2";

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

    static Stream<Object> deprecatedPropertyValues()
    {
        return Stream.of("deprVal1", 2);
    }

    @ParameterizedTest
    @MethodSource("deprecatedPropertyValues")
    void shouldWarnOnDeprecatedProperty(Object propertyValue)
    {
        DeprecatedPropertiesHandler spy = spy(handler);

        spy.warnIfDeprecated(DEPR_PROP1, propertyValue);

        verify(spy).warnDeprecated("Deprecated property found: '{}'. Use '{}' instead", DEPR_PROP1, PROP1);
    }

    @Test
    void testDeprecatedProperty()
    {
        DeprecatedPropertiesHandler spy = spy(handler);

        spy.warnIfDeprecated(PROP2, " ${property-1} and ${deprecated-1} and ${deprecated-2} and ${non-deprecated-2}");

        String deprecatePlaceholderMessage = "Property '{}' uses deprecated placeholder '${{}}'. Use '${{}}' instead";
        verify(spy).warnDeprecated(deprecatePlaceholderMessage, PROP2, DEPR_PROP1, PROP1);
        verify(spy).warnDeprecated(deprecatePlaceholderMessage, PROP2, DEPR_PROP2, PROP2);
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
    void shouldFlipBooleanValueIfKeyHasExclamationMarkBeforeDeprecatedPropertyValue()
    {
        Properties deprecatedProperties = new Properties();
        String newKey = "new.key";
        deprecatedProperties.put("!some.key", newKey);
        String newKey1 = "new.key1";
        deprecatedProperties.put("!some.key1", newKey1);
        String newKey2 = "new.key2";
        deprecatedProperties.put("!some.key2", newKey2);
        DeprecatedPropertiesHandler handler = new DeprecatedPropertiesHandler(deprecatedProperties, PLACEHOLDER_PREFIX,
                PLACEHOLDER_SUFFIX);
        Properties properties = new Properties();
        properties.put("some.key", TRUE);
        properties.put("some.key1", FALSE);
        String fortyTwo = "42";
        properties.put("some.key2", fortyTwo);
        handler.replaceDeprecated(properties, properties);
        assertEquals(FALSE, properties.get(newKey));
        assertEquals(TRUE, properties.get(newKey1));
        assertEquals(fortyTwo, properties.get(newKey2));
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
        DeprecatedPropertiesHandler spy = spy(handler);
        Properties props = new Properties();
        spy.replaceDeprecated(props);
        verify(spy).replaceDeprecated(props, props);
    }

    @Test
    void shouldReplaceDeprecatedPropertyWithDynamicPart()
    {
        Properties deprecatedProperties = new Properties();
        deprecatedProperties.put("!oldKey.data-(\\d+).value", "newKey.data-(\\d+).value");
        deprecatedProperties.put("(a+)-value-(b+)", "(a+)-new-value-(b+)");
        DeprecatedPropertiesHandler handler = new DeprecatedPropertiesHandler(deprecatedProperties, PLACEHOLDER_PREFIX,
                PLACEHOLDER_SUFFIX);
        Properties properties = new Properties();
        properties.put("oldKey-data-1.value", TRUE);
        properties.put("oldKey-data-2.value", FALSE);
        properties.put("aaaaaaa-value-b", VALUE);
        String dynamicArgumentNotMatches = "oldKey-data-x.value";
        properties.put(dynamicArgumentNotMatches, FALSE);
        handler.replaceDeprecated(properties, properties);
        assertEquals(FALSE, properties.get("newKey.data-1.value"));
        assertEquals(TRUE, properties.get("newKey.data-2.value"));
        assertEquals(FALSE, properties.get(dynamicArgumentNotMatches));
        assertEquals(VALUE, properties.get("aaaaaaa-new-value-b"));
    }

    @Test
    void shouldThrowAnExceptionIfNotAlignedNumberOfDynamicPartsPassed()
    {
        Properties deprecatedProperties = new Properties();
        deprecatedProperties.put("key.(a+).value", "newKey.(a+).somemore.(b+).value");
        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                () -> new DeprecatedPropertiesHandler(deprecatedProperties, PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX));
        assertEquals(
            "Deprecated property: key.(a+).value and new property:"
            + " newKey.(a+).somemore.(b+).value keys have different dynamic values number", iae.getMessage());
    }
}
