/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class LocatorFactoryTests
{
    private static final LocatorFactory.Locator FIRST_LOCATOR = new LocatorFactory.Locator("xpath", ".//");
    private static final LocatorFactory.Locator SECOND_LOCATOR = new LocatorFactory.Locator("cssSelector", "div");

    @Test
    void convertStringToLocatorsTest()
    {
        Set<LocatorFactory.Locator> expected = new HashSet<>();
        expected.add(FIRST_LOCATOR);
        expected.add(SECOND_LOCATOR);
        assertEquals(expected, LocatorFactory.convertStringToLocators("By.xpath(.//), By.cssSelector(div)"));
    }

    @Test
    void convertStringToLocatorsSourceIsBlankTest()
    {
        assertEquals(Collections.emptySet(), LocatorFactory.convertStringToLocators(""));
    }

    @Test
    void validateHashCodeAndEqualsOfLocator()
    {
        EqualsVerifier.forClass(LocatorFactory.Locator.class).verify();
    }
}
