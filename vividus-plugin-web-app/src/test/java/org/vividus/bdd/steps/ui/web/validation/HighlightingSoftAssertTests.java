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

package org.vividus.bdd.steps.ui.web.validation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.context.WebUiContext;

@ExtendWith(MockitoExtension.class)
class HighlightingSoftAssertTests
{
    private static final String DESCRIPTION = "description";
    private static final boolean CONDITION = true;

    @Mock
    private WebElement webElement;

    @Mock
    private IDescriptiveSoftAssert descriptiveSoftAssert;

    @Mock
    private WebUiContext webUiContext;

    @InjectMocks
    private HighlightingSoftAssert highlightingSoftAssert;

    @Test
    void testWithHighlightedElement()
    {
        List<WebElement> list = List.of(webElement);
        highlightingSoftAssert.withHighlightedElement(webElement);
        verify(webUiContext).putAssertingWebElements(list);
    }

    @Test
    void testWithHighlightedElements()
    {
        List<WebElement> list = List.of(webElement);
        highlightingSoftAssert.withHighlightedElements(list);
        verify(webUiContext).putAssertingWebElements(list);
    }

    @Test
    void testAssertTrue()
    {
        boolean expectedResult = true;
        when(descriptiveSoftAssert.assertTrue(DESCRIPTION, CONDITION)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertTrue(DESCRIPTION, CONDITION);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testAssertFalse()
    {
        boolean expectedResult = true;
        when(descriptiveSoftAssert.assertFalse(DESCRIPTION, CONDITION)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertFalse(DESCRIPTION, CONDITION);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testAssertEqualsObject()
    {
        boolean expectedResult = true;
        Object expected = new Object();
        Object actual = new Object();
        when(descriptiveSoftAssert.assertEquals(DESCRIPTION, expected, actual)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertEquals(DESCRIPTION, expected, actual);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testAssertNotEqualsObject()
    {
        boolean expectedResult = true;
        Object expected = new Object();
        Object actual = new Object();
        when(descriptiveSoftAssert.assertNotEquals(DESCRIPTION, expected, actual)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertNotEquals(DESCRIPTION, expected, actual);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testAssertEqualsDouble()
    {
        boolean expectedResult = true;
        double expected = 1;
        double actual = 1;
        double delta = 1;
        when(descriptiveSoftAssert.assertEquals(DESCRIPTION, expected, actual, delta)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertEquals(DESCRIPTION, expected, actual, delta);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testAssertNotEqualsDouble()
    {
        boolean expectedResult = true;
        double expected = 1;
        double actual = 1;
        double delta = 1;
        when(descriptiveSoftAssert.assertNotEquals(DESCRIPTION, expected, actual, delta))
                .thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertNotEquals(DESCRIPTION, expected, actual, delta);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testAssertEqualsLong()
    {
        boolean expectedResult = true;
        long expected = 1L;
        long actual = 2L;
        when(descriptiveSoftAssert.assertEquals(DESCRIPTION, expected, actual)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertEquals(DESCRIPTION, expected, actual);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testAssertNotEqualsLong()
    {
        boolean expectedResult = true;
        long expected = 1L;
        long actual = 2L;
        when(descriptiveSoftAssert.assertNotEquals(DESCRIPTION, expected, actual)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertNotEquals(DESCRIPTION, expected, actual);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testAssertEqualsBoolean()
    {
        boolean expectedResult = true;
        boolean expected = true;
        boolean actual = true;
        when(descriptiveSoftAssert.assertEquals(DESCRIPTION, expected, actual)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertEquals(DESCRIPTION, expected, actual);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testAssertNotEqualsBoolean()
    {
        boolean expectedResult = true;
        boolean expected = true;
        boolean actual = true;
        when(descriptiveSoftAssert.assertNotEquals(DESCRIPTION, expected, actual)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertNotEquals(DESCRIPTION, expected, actual);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testAssertNotNull()
    {
        boolean expectedResult = true;
        Object object = new Object();
        when(descriptiveSoftAssert.assertNotNull(DESCRIPTION, object)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertNotNull(DESCRIPTION, object);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testAssertNull()
    {
        boolean expectedResult = true;
        Object object = new Object();
        when(descriptiveSoftAssert.assertNull(DESCRIPTION, object)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertNull(DESCRIPTION, object);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testAssertThat()
    {
        boolean expectedResult = true;
        Object object = new Object();
        Matcher<Object> matcher = equalTo(object);
        when(descriptiveSoftAssert.assertThat(DESCRIPTION, object, matcher)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertThat(DESCRIPTION, object, matcher);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testAssertThatWithSystemAndBusinessDescription()
    {
        boolean expectedResult = true;
        Object object = new Object();
        Matcher<Object> matcher = equalTo(object);
        when(descriptiveSoftAssert.assertThat(DESCRIPTION, DESCRIPTION, object, matcher))
                .thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.assertThat(DESCRIPTION, DESCRIPTION, object, matcher);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testRecordPassedAssertion()
    {
        boolean expectedResult = true;
        when(descriptiveSoftAssert.recordPassedAssertion(DESCRIPTION)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.recordPassedAssertion(DESCRIPTION);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testRecordFailedAssertion()
    {
        boolean expectedResult = true;
        when(descriptiveSoftAssert.recordFailedAssertion(DESCRIPTION)).thenReturn(expectedResult);
        boolean actualResult = highlightingSoftAssert.recordFailedAssertion(DESCRIPTION);
        verify(webUiContext).clearAssertingWebElements();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testRecordFailedAssertionException()
    {
        Exception exception = mock(Exception.class);
        when(descriptiveSoftAssert.recordFailedAssertion(exception)).thenReturn(false);
        boolean actualResult = highlightingSoftAssert.recordFailedAssertion(exception);
        verify(webUiContext).clearAssertingWebElements();
        assertFalse(actualResult);
    }

    @Test
    void testVerify()
    {
        highlightingSoftAssert.verify();
        verify(descriptiveSoftAssert).verify();
    }
}
