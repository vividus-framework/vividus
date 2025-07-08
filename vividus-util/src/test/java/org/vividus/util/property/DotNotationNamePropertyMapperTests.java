/*
 * Copyright 2019-2024 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DotNotationNamePropertyMapperTests
{
    private static final String PROPERTY_PREFIX_SEPARATOR = ".";
    private static final String PROPERTY_PREFIX = "user.";
    private static final String ADMIN = "admin.user";
    private static final String ADMIN_SIMPLE = "admin";

    private static final String FIRST_NAME = "John";
    private static final String MIDDLE_NAME = "Junior";
    private static final String LAST_NAME = "Smith";
    private static final String PATTERN = ".*";
    private static final String DOB = "01.01.1900";
    private static final Set<JsonDeserializer<?>> DESERIALIZERS = Set.of(new PatternDeserializer(),
            new SupplierDeserializer());

    @Mock private PropertyParser propertyParser;

    private DotNotationNamePropertyMapper propertyMapper;

    @BeforeEach
    void beforeEach()
    {
        propertyMapper = new DotNotationNamePropertyMapper(PROPERTY_PREFIX_SEPARATOR,
                PropertyNamingStrategies.KEBAB_CASE, propertyParser, DESERIALIZERS);
    }

    @Test
    void shouldMapMergedPropertiesToCollectionOfObjects() throws IOException
    {
        Map<String, String> properties = new HashMap<>(createObjectProperties(PROPERTY_PREFIX + ADMIN
                + PROPERTY_PREFIX_SEPARATOR));
        properties.putAll(createObjectProperties(PROPERTY_PREFIX + ADMIN_SIMPLE + PROPERTY_PREFIX_SEPARATOR));
        when(propertyParser.getPropertiesByPrefix(PROPERTY_PREFIX)).thenReturn(properties);

        PropertyMappedCollection<User> result = propertyMapper.readValuesCaseInsensitively(PROPERTY_PREFIX, User.class);
        Map<String, User> data = result.getData();
        assertEquals(2, data.size());
        assertUser(data.get(ADMIN));
        assertUser(data.get(ADMIN_SIMPLE));
    }

    private Map<String, String> createObjectProperties(String objectPropertyPrefix)
    {
        return Map.of(
            objectPropertyPrefix + "first-name", FIRST_NAME,
            objectPropertyPrefix + "last-name", LAST_NAME,
            objectPropertyPrefix + "pattern", PATTERN,
            objectPropertyPrefix + "middle-name", MIDDLE_NAME,
            objectPropertyPrefix + "dob", DOB
        );
    }

    private void assertUser(User user)
    {
        assertNotNull(user);
        assertAll(
            () -> assertEquals(FIRST_NAME, user.getFirstName()),
            () -> assertEquals(LAST_NAME, user.getLastName()),
            () -> assertEquals(PATTERN, user.getPattern().toString()),
            () -> assertEquals(Optional.of(MIDDLE_NAME), user.getMiddleName()),
            () -> assertEquals(DOB, user.getDob().get())
        );
    }

    private static final class User
    {
        private String firstName;
        private Optional<String> middleName;
        private String lastName;
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

        public Optional<String> getMiddleName()
        {
            return middleName;
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
