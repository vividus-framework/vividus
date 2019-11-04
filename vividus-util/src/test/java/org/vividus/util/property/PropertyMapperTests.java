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

package org.vividus.util.property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.Whitebox;

@ExtendWith(MockitoExtension.class)
class PropertyMapperTests
{
    private static final String PROPERTY_PREFIX = "user.";
    private static final String ADMINISTRATOR = "administrator";
    private static final String ADMINISTRATOR_PROPERTY_FAMILY = PROPERTY_PREFIX + ADMINISTRATOR + ".";
    private static final String ADMINISTRATOR_FIRST_NAME = ADMINISTRATOR_PROPERTY_FAMILY + "first-name";
    private static final String ADMINISTRATOR_LAST_NAME = ADMINISTRATOR_PROPERTY_FAMILY + "last-name";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Smith";

    @Mock
    private PropertyParser propertyParser;

    @InjectMocks
    private PropertyMapper propertyMapper;

    @Test
    void testSuccessfulPropertyMappingToCollectionOfObjects() throws IOException
    {
        Map<String, String> properties = new HashMap<>();
        properties.put(ADMINISTRATOR_FIRST_NAME, FIRST_NAME);
        properties.put(ADMINISTRATOR_LAST_NAME, LAST_NAME);
        String pattern = ".*";
        String surName = "surName";
        String dob = "01.01.1900";
        properties.put(ADMINISTRATOR_PROPERTY_FAMILY + "pattern", pattern);
        properties.put(ADMINISTRATOR_PROPERTY_FAMILY + "sur-name", surName);
        properties.put(ADMINISTRATOR_PROPERTY_FAMILY + "dob", dob);
        when(propertyParser.getPropertiesByPrefix(PROPERTY_PREFIX)).thenReturn(properties);
        Whitebox.setInternalState(propertyMapper, "deserializers", Set.of(new PatternDeserializer(),
                new SupplierDeserializer()));
        propertyMapper.init();
        Map<String, User> result = propertyMapper.readValues(PROPERTY_PREFIX, User.class);
        assertEquals(1, result.size());
        assertThat(result, hasKey(ADMINISTRATOR));
        User administrator = result.get(ADMINISTRATOR);
        assertEquals(FIRST_NAME, administrator.getFirstName());
        assertEquals(LAST_NAME, administrator.getLastName());
        assertEquals(pattern, administrator.getPattern().toString());
        assertEquals(pattern, administrator.getPattern().toString());
        assertEquals(Optional.of(surName), administrator.getSurName());
        assertEquals(dob, administrator.getDob().get());
    }

    private static final class User
    {
        private String firstName;
        private String lastName;
        private Optional<String> surName;
        private Pattern pattern;
        private Supplier<String> dob;

        public String getFirstName()
        {
            return firstName;
        }

        public String getLastName()
        {
            return lastName;
        }

        public Pattern getPattern()
        {
            return pattern;
        }

        public Optional<String> getSurName()
        {
            return surName;
        }

        public Supplier<String> getDob()
        {
            return dob;
        }
    }

    private static final class PatternDeserializer extends JsonDeserializer<Pattern>
    {
        @Override
        public Pattern deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            return Pattern.compile(p.getText());
        }
    }

    private static final class SupplierDeserializer extends JsonDeserializer<Supplier<?>>
    {
        @Override
        public Supplier<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            String text = p.getText();
            return () -> text;
        }
    }
}
