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

package org.vividus.ui.web.playwright.converter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.jupiter.api.Test;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

class StringToPlaywrightLocatorSetConverterTests
{
    @Test
    void shouldConvertStringToPlaywrightLocatorsSet()
    {
        var playwrightLocators = new StringToPlaywrightLocatorSetConverter().convertValue("By.xpath(//div),By.id(user)",
                TypeUtils.parameterize(Set.class, PlaywrightLocator.class));
        assertEquals(2, playwrightLocators.size());
        var iterator = playwrightLocators.iterator();
        assertAll(
                () -> assertEquals("xpath=//div", iterator.next().getLocator()),
                () -> assertEquals("id=user", iterator.next().getLocator())
        );
    }
}
