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

package org.vividus.bdd.report.allure.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.exception.VerificationError;

@ExtendWith(MockitoExtension.class)
class AdaptedVerificationErrorTests
{
    private static final String ERROR_MESSAGE = "Error message";

    @Mock
    private PrintWriter printWriter;

    @Mock
    private VerificationError verificationErrorOriginal;

    @Test
    void testPrintStackTrace()
    {
        StackTraceElement[] stackTraceElement = new StackTraceElement[] {
                new StackTraceElement("ClassName", "methodName", "fileName", 1) };
        when(verificationErrorOriginal.getStackTrace()).thenReturn(stackTraceElement);
        AdaptedVerificationError verificationError = new AdaptedVerificationError(ERROR_MESSAGE,
                verificationErrorOriginal);
        verificationError.printStackTrace(printWriter);
        verify(printWriter).println();
        verify(verificationErrorOriginal).printStackTrace(printWriter);
    }
}
