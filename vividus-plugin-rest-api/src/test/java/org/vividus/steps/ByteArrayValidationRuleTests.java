/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class ByteArrayValidationRuleTests
{
    private static final byte[] EXPECTED_BYTES = { 1, 0, 1, 1, 1, 1 };
    private static final byte[] ACTUAL_BYTES = { 1, 1, 1, 1, 0, 1 };
    private static final String ARRAYS_ARE_EQUAL = "Expected and actual arrays are equal";
    private static final String ARRAYS_SIZE = "Arrays size";

    @Mock
    private ISoftAssert softAssert;

    @Test
    void testIsEqualToDifferentSize()
    {
        when(softAssert.assertEquals(ARRAYS_SIZE, 6, 1)).thenReturn(false);
        ByteArrayValidationRule.IS_EQUAL_TO.assertMatchesRule(softAssert, EXPECTED_BYTES, new byte[] { 1 });
        verify(softAssert, never()).recordFailedAssertion(anyString());
        verify(softAssert, never()).recordPassedAssertion(anyString());
    }

    @Test
    void testIsEqualToArraysAreEqual()
    {
        when(softAssert.assertEquals(ARRAYS_SIZE, 6, 6)).thenReturn(true);
        ByteArrayValidationRule.IS_EQUAL_TO.assertMatchesRule(softAssert, EXPECTED_BYTES, EXPECTED_BYTES);
        verify(softAssert).recordPassedAssertion(ARRAYS_ARE_EQUAL);
    }

    @Test
    void testIsEqualToArraysDontMatch()
    {
        when(softAssert.assertEquals(ARRAYS_SIZE, 6, 6)).thenReturn(true);
        ByteArrayValidationRule.IS_EQUAL_TO.assertMatchesRule(softAssert, EXPECTED_BYTES, ACTUAL_BYTES);
        verify(softAssert).recordFailedAssertion("First mismatch at index 1 expected 0 but was 1");
    }

    @Test
    void testIsNotEqualToArraysArentEqual()
    {
        ByteArrayValidationRule.IS_NOT_EQUAL_TO.assertMatchesRule(softAssert, EXPECTED_BYTES, ACTUAL_BYTES);
        verify(softAssert).recordPassedAssertion("Expected and actual arrays are not equal");
    }

    @Test
    void testIsNotEqualToArraysAreEqual()
    {
        ByteArrayValidationRule.IS_NOT_EQUAL_TO.assertMatchesRule(softAssert, EXPECTED_BYTES, EXPECTED_BYTES);
        verify(softAssert).recordFailedAssertion(ARRAYS_ARE_EQUAL);
    }
}
