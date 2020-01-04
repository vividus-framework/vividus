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

package org.vividus.softassert.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.Test;

class AssertionCollectionTests
{
    private final AssertionCollection assertionCollection = new AssertionCollection();

    @Test
    void testAddPassed()
    {
        assertionCollection.addPassed();
        assertEquals(1, assertionCollection.getAssertionsCount());
        assertEquals(0, assertionCollection.getAssertionErrors().size());
    }

    @Test
    void testAddFailed()
    {
        SoftAssertionError error = mock(SoftAssertionError.class);
        assertionCollection.addFailed(error);
        assertEquals(1, assertionCollection.getAssertionsCount());
        List<SoftAssertionError> assertionErrors = assertionCollection.getAssertionErrors();
        assertEquals(1, assertionErrors.size());
        assertEquals(error, assertionErrors.get(0));
    }

    @Test
    void testClear()
    {
        assertionCollection.addPassed();
        assertionCollection.addFailed(mock(SoftAssertionError.class));
        assertionCollection.clear();
        assertEquals(0, assertionCollection.getAssertionsCount());
        assertEquals(0, assertionCollection.getAssertionErrors().size());
    }
}
