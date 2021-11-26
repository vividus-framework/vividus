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

package org.vividus.report.allure.adapter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.formatter.IAssertionFormatter;
import org.vividus.softassert.model.SoftAssertionError;

@ExtendWith(MockitoExtension.class)
class VerificationErrorAdapterTests
{
    private static final String ERROR_MESSAGE = "Error message";

    @Mock
    private IAssertionFormatter assertionFormatter;

    @Mock
    private SoftAssertionError softAssertionError;

    @Mock
    private VerificationError verificationError;

    @InjectMocks
    private VerificationErrorAdapter verificationErrorAdapter;

    @Test
    void testAdapt()
    {
        List<SoftAssertionError> errors = List.of(softAssertionError);
        when(verificationError.getErrors()).thenReturn(errors);
        when(assertionFormatter.getErrorsMessage(errors, false)).thenReturn(ERROR_MESSAGE);
        StackTraceElement[] stackTraceElement = {
                new StackTraceElement("ClassName", "methodName", "fileName", 1)};
        when(verificationError.getStackTrace()).thenReturn(stackTraceElement);
        VerificationError actualError = verificationErrorAdapter.adapt(verificationError);
        assertEquals(errors, actualError.getErrors());
        assertEquals(ERROR_MESSAGE, actualError.getMessage());
        assertArrayEquals(stackTraceElement, actualError.getStackTrace());
    }
}
