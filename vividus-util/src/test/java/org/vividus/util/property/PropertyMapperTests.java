/*
 * Copyright 2019-2022 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@ExtendWith(MockitoExtension.class)
class PropertyMapperTests
{
    private static final String PROPERTY_PREFIX = "user.";
    private static final String KEY_PREFIX = "key-prefix-";
    private static final String ADMIN = "admin";
    private static final String READER = "reader";

    private static final String FIRST_NAME = "John";
    private static final String MIDDLE_NAME = "Junior";
    private static final String LAST_NAME = "Smith";
    private static final String PATTERN = ".*";
    private static final String DOB = "01.01.1900";
    private static final Set<JsonDeserializer<?>> DESERIALIZERS = Set.of(new PatternDeserializer(),
            new SupplierDeserializer());

    @Mock
    private PropertyParser propertyParser;

    private PropertyMapper propertyMapper;

    @BeforeEach
    void beforeEach()
    {
        propertyMapper = new PropertyMapper(".", PropertyNamingStrategies.KEBAB_CASE, propertyParser, DESERIALIZERS);
    }

    @Test
    void shouldMapPropertiesToSingleObject() throws IOException
    {
        Map<String, String> properties = createObjectProperties("");
        when(propertyParser.getPropertyValuesByPrefix(PROPERTY_PREFIX)).thenReturn(properties);
        Optional<User> result = propertyMapper.readValue(PROPERTY_PREFIX, User.class);
        assertTrue(result.isPresent());
        assertUser(result.get());
    }

    @Test
    void shouldMapPropertiesToEmptyOptional() throws IOException
    {
        when(propertyParser.getPropertyValuesByPrefix(PROPERTY_PREFIX)).thenReturn(Map.of());
        Optional<User> result = propertyMapper.readValue(PROPERTY_PREFIX, User.class);
        assertFalse(result.isPresent());
    }

    @Test
    void shouldMapInputPropertiesToSingleObject() throws IOException
    {
        Map<String, String> properties = createObjectProperties("");
        User result = propertyMapper.readValue(properties, User.class);
        assertUser(result);
    }

    @Test
    void shouldMapMergedPropertiesToCollectionOfObjects() throws IOException
    {
        String addressPrefix = "address.";
        String streetNameKey = "street-name";
        String streetName = "Stakanalievo";

        Map<String, String> properties = new HashMap<>(createObjectProperties(PROPERTY_PREFIX + ADMIN + '.'));
        properties.put(PROPERTY_PREFIX + ADMIN + '.' + addressPrefix + streetNameKey, streetName);
        when(propertyParser.getPropertiesByPrefix(PROPERTY_PREFIX)).thenReturn(properties);

        Map<String, String> baseProperties = Map.of(
            addressPrefix + streetNameKey, "Broadway",
            addressPrefix + "house-number", "228"
        );
        when(propertyParser.getPropertiesByPrefix(addressPrefix)).thenReturn(baseProperties);

        PropertyMappedCollection<User> result = propertyMapper.readValues(PROPERTY_PREFIX, addressPrefix,
                User.class);
        assertCollection(result, ADMIN);
        Address address = result.getData().get(ADMIN).getAddress();
        assertNotNull(address);
        assertEquals(streetName, address.getStreetName());
        assertEquals(228, address.getHouseNumber());
    }

    @Test
    void shouldMapPropertiesToCollectionOfObjects() throws IOException
    {
        mockObjectProperties();
        PropertyMappedCollection<User> result = propertyMapper.readValues(PROPERTY_PREFIX, User.class);
        assertCollection(result, ADMIN);
    }

    @Test
    void shouldMapPropertiesToCollectionOfObjectsWithUpdateKeys() throws IOException
    {
        mockObjectProperties();
        PropertyMappedCollection<User> result = propertyMapper.readValues(PROPERTY_PREFIX, KEY_PREFIX::concat,
                User.class);
        assertCollection(result, KEY_PREFIX + ADMIN);
    }

    @Test
    void shouldMapPropertiesToCollectionOfObjectsSortedByKey() throws IOException
    {
        Map<String, String> adminProperties = createObjectProperties(PROPERTY_PREFIX + ADMIN + '.');
        Map<String, String> readerProperties = createObjectProperties(PROPERTY_PREFIX + READER + '.');
        Map<String, String> allProperties = new HashMap<>(readerProperties);
        allProperties.putAll(adminProperties);
        when(propertyParser.getPropertiesByPrefix(PROPERTY_PREFIX)).thenReturn(allProperties);
        PropertyMappedCollection<User> result = propertyMapper.readValues(PROPERTY_PREFIX, KEY_PREFIX::concat,
                Comparator.naturalOrder(), User.class);
        Map<String, User> data = result.getData();
        assertEquals(2, data.size());
        Iterator<Entry<String, User>> iterator = data.entrySet().iterator();
        Entry<String, User> entry = iterator.next();
        assertEquals(KEY_PREFIX + ADMIN, entry.getKey());
        assertUser(entry.getValue());
        entry = iterator.next();
        assertEquals(KEY_PREFIX + READER, entry.getKey());
        assertUser(entry.getValue());
    }

    @Test
    void shouldMapPropertiesToKeysInsensitiveMap() throws IOException
    {
        Map<String, String> adminProperties = createObjectProperties(PROPERTY_PREFIX + ADMIN + '.');
        Map<String, String> readerProperties = createObjectProperties(PROPERTY_PREFIX + READER + '.');
        Map<String, String> allProperties = new HashMap<>(readerProperties);
        allProperties.putAll(adminProperties);
        when(propertyParser.getPropertiesByPrefix(PROPERTY_PREFIX)).thenReturn(allProperties);
        PropertyMappedCollection<User> result = propertyMapper
            .readValuesCaseInsensitively(PROPERTY_PREFIX, User.class);
        assertUser(result.getNullable("AdMiN").orElse(null));
        assertUser(result.getNullable("READER").orElse(null));
        assertUser(result.getNullable(ADMIN).orElse(null));
        assertUser(result.getNullable(READER).orElse(null));
    }

    private void mockObjectProperties()
    {
        Map<String, String> properties = createObjectProperties(PROPERTY_PREFIX + ADMIN + '.');
        when(propertyParser.getPropertiesByPrefix(PROPERTY_PREFIX)).thenReturn(properties);
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

    private void assertCollection(PropertyMappedCollection<User> collection, String objectKey)
    {
        Map<String, User> data = collection.getData();
        assertEquals(1, data.size());
        assertUser(data.get(objectKey));
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
        private Address address;

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

        public Address getAddress()
        {
            return address;
        }
    }

    private static final class Address
    {
        @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
        private String streetName;
        private int houseNumber;

        public String getStreetName()
        {
            return streetName;
        }

        public int getHouseNumber()
        {
            return houseNumber;
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
