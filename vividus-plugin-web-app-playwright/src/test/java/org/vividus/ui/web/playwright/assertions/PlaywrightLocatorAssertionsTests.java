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

package org.vividus.ui.web.playwright.assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaywrightLocatorAssertionsTests
{
    private static final double ASSERTION_NO_WAIT_TIMEOUT = 0.1;
    private static final Pattern PATTERN = Pattern.compile("\\d+");

    @Mock private Locator locator;

    @Test
    void shouldAssertElementHasTextMatchingRegex()
    {
        shouldAssertElement(locatorAssertions ->
        {
            PlaywrightLocatorAssertions.assertElementHasTextMatchingRegex(locator, PATTERN, false);
            ArgumentCaptor<LocatorAssertions.ContainsTextOptions> captor = ArgumentCaptor
                    .forClass(LocatorAssertions.ContainsTextOptions.class);
            verify(locatorAssertions).containsText(eq(PATTERN), captor.capture());
            assertEquals(ASSERTION_NO_WAIT_TIMEOUT, captor.getValue().timeout);
        });
    }

    @Test
    void shouldAssertElementHasTextMatchingRegexWaitForState()
    {
        shouldAssertElement(locatorAssertions ->
        {
            PlaywrightLocatorAssertions.assertElementHasTextMatchingRegex(locator, PATTERN, true);
            verify(locatorAssertions).containsText(PATTERN, null);
        });
    }

    @Test
    void shouldAssertElementVisibleWithWaitForState()
    {
        shouldAssertElement(locatorAssertions -> {
            PlaywrightLocatorAssertions.assertElementVisible(locator, true);
            verify(locatorAssertions).isVisible(null);
        });
    }

    @Test
    void shouldAssertElementVisible()
    {
        shouldAssertElement(locatorAssertions -> {
            PlaywrightLocatorAssertions.assertElementVisible(locator, false);
            ArgumentCaptor<LocatorAssertions.IsVisibleOptions> captor = ArgumentCaptor.forClass(
                    LocatorAssertions.IsVisibleOptions.class);
            verify(locatorAssertions).isVisible(captor.capture());
            assertEquals(ASSERTION_NO_WAIT_TIMEOUT, captor.getValue().timeout);
        });
    }

    @Test
    void shouldAssertElementHiddenWithWaitForState()
    {
        shouldAssertElement(locatorAssertions -> {
            PlaywrightLocatorAssertions.assertElementHidden(locator, true);
            verify(locatorAssertions).isHidden(null);
        });
    }

    @Test
    void shouldAssertElementHidden()
    {
        shouldAssertElement(locatorAssertions -> {
            PlaywrightLocatorAssertions.assertElementHidden(locator, false);
            ArgumentCaptor<LocatorAssertions.IsHiddenOptions> captor = ArgumentCaptor.forClass(
                    LocatorAssertions.IsHiddenOptions.class);
            verify(locatorAssertions).isHidden(captor.capture());
            assertEquals(ASSERTION_NO_WAIT_TIMEOUT, captor.getValue().timeout);
        });
    }

    @Test
    void shouldAssertElementEnabledWithWaitForState()
    {
        shouldAssertElement(locatorAssertions -> {
            PlaywrightLocatorAssertions.assertElementEnabled(locator, true);
            verify(locatorAssertions).isEnabled(null);
        });
    }

    @Test
    void shouldAssertElementEnabled()
    {
        shouldAssertElement(locatorAssertions -> {
            PlaywrightLocatorAssertions.assertElementEnabled(locator, false);
            ArgumentCaptor<LocatorAssertions.IsEnabledOptions> captor = ArgumentCaptor.forClass(
                    LocatorAssertions.IsEnabledOptions.class);
            verify(locatorAssertions).isEnabled(captor.capture());
            assertEquals(ASSERTION_NO_WAIT_TIMEOUT, captor.getValue().timeout);
        });
    }

    @Test
    void shouldAssertElementDisabledWithWaitForState()
    {
        shouldAssertElement(locatorAssertions -> {
            PlaywrightLocatorAssertions.assertElementDisabled(locator, true);
            verify(locatorAssertions).isDisabled(null);
        });
    }

    @Test
    void shouldAssertElementDisabled()
    {
        shouldAssertElement(locatorAssertions -> {
            PlaywrightLocatorAssertions.assertElementDisabled(locator, false);
            ArgumentCaptor<LocatorAssertions.IsDisabledOptions> captor = ArgumentCaptor.forClass(
                    LocatorAssertions.IsDisabledOptions.class);
            verify(locatorAssertions).isDisabled(captor.capture());
            assertEquals(ASSERTION_NO_WAIT_TIMEOUT, captor.getValue().timeout);
        });
    }

    @Test
    void shouldAssertElementSelectedWithWaitForState()
    {
        shouldAssertElement(locatorAssertions -> {
            PlaywrightLocatorAssertions.assertElementSelected(locator, true);
            ArgumentCaptor<LocatorAssertions.IsCheckedOptions> captor = ArgumentCaptor.forClass(
                    LocatorAssertions.IsCheckedOptions.class);
            verify(locatorAssertions).isChecked(captor.capture());
            assertTrue(captor.getValue().checked);
        });
    }

    @Test
    void shouldAssertElementSelected()
    {
        shouldAssertElement(locatorAssertions -> {
            PlaywrightLocatorAssertions.assertElementSelected(locator, false);
            ArgumentCaptor<LocatorAssertions.IsCheckedOptions> captor = ArgumentCaptor.forClass(
                    LocatorAssertions.IsCheckedOptions.class);
            verify(locatorAssertions).isChecked(captor.capture());
            assertEquals(ASSERTION_NO_WAIT_TIMEOUT, captor.getValue().timeout);
            assertTrue(captor.getValue().checked);
        });
    }

    @Test
    void shouldAssertElementNotSelectedWithWaitForState()
    {
        shouldAssertElement(locatorAssertions -> {
            PlaywrightLocatorAssertions.assertElementNotSelected(locator, true);
            ArgumentCaptor<LocatorAssertions.IsCheckedOptions> captor = ArgumentCaptor.forClass(
                    LocatorAssertions.IsCheckedOptions.class);
            verify(locatorAssertions).isChecked(captor.capture());
            assertFalse(captor.getValue().checked);
        });
    }

    @Test
    void shouldAssertElementNotSelected()
    {
        shouldAssertElement(locatorAssertions -> {
            PlaywrightLocatorAssertions.assertElementNotSelected(locator, false);
            ArgumentCaptor<LocatorAssertions.IsCheckedOptions> captor = ArgumentCaptor.forClass(
                    LocatorAssertions.IsCheckedOptions.class);
            verify(locatorAssertions).isChecked(captor.capture());
            assertEquals(ASSERTION_NO_WAIT_TIMEOUT, captor.getValue().timeout);
            assertFalse(captor.getValue().checked);
        });
    }

    private void shouldAssertElement(Consumer<LocatorAssertions> test)
    {
        try (var playwrightAssertionsStaticMock = Mockito.mockStatic(PlaywrightAssertions.class))
        {
            var locatorAssertions = mock(LocatorAssertions.class, RETURNS_SELF);
            playwrightAssertionsStaticMock.when(() -> PlaywrightAssertions.assertThat(locator))
                    .thenReturn(locatorAssertions);
            test.accept(locatorAssertions);
        }
    }
}
