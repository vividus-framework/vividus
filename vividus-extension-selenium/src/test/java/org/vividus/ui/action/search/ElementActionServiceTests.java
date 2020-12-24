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

package org.vividus.ui.action.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.testdouble.TestElementFilter;
import org.vividus.testdouble.TestElementSearch;
import org.vividus.testdouble.TestLocatorType;

@ExtendWith(MockitoExtension.class)
class ElementActionServiceTests
{
    @Spy private Set<IElementAction> elementActions = new HashSet<>();
    @InjectMocks private ElementActionService service;

    @BeforeEach
    void init() throws IllegalArgumentException
    {
        elementActions.add(new TestElementFilter());
        elementActions.add(new TestElementSearch());
        service.init();
    }

    @AfterEach
    void cleanUp()
    {
        elementActions.clear();
    }

    @Test
    void shouldReturnSearchLocatorTypes()
    {
        assertEquals(Set.of(TestLocatorType.SEARCH), service.getSearchLocatorTypes());
    }

    @Test
    void shouldReturnFilterLocatorTypes()
    {
        assertEquals(Set.of(TestLocatorType.FILTER), service.getFilterLocatorTypes());
    }

    @CsvSource({
        "FILTER, org.vividus.testdouble.TestElementFilter",
        "SEARCH, org.vividus.testdouble.TestElementSearch"
    })
    @ParameterizedTest
    void testFind(TestLocatorType type, Class<?> expectedClass)
    {
        assertThat(service.find(type), instanceOf(expectedClass));
    }

    @Test
    void testFindAttributeTypeUnknown()
    {
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> service.find(TestLocatorType.COMPETING_FILTER));
        assertEquals("There is no mapped element action for attribute: Competing Filter", exception.getMessage());
    }
}
