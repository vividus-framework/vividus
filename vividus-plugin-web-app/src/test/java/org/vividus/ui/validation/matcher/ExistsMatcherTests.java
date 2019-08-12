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

package org.vividus.ui.validation.matcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.hamcrest.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class ExistsMatcherTests
{
    private static final String EXISTS = "exists";
    private static final String DOESNT_EXISTS = "does not exist";

    @Mock
    private Description description;

    @InjectMocks
    private ExistsMatcher existsMatcher;

    @Test
    void testMatchesSafely()
    {
        assertTrue(existsMatcher.matchesSafely(List.of(mock(WebElement.class))));
    }

    @Test
    void testDescribeTo()
    {
        existsMatcher.describeTo(description);
        verify(description).appendText(EXISTS);
    }

    @Test
    void testDescribeMismatchSafely()
    {
        existsMatcher.describeMismatchSafely(List.of(), description);
        verify(description).appendText(DOESNT_EXISTS);
    }

    @Test
    void testExistsMatcher()
    {
        assertNotNull(ExistsMatcher.exists());
    }
}
