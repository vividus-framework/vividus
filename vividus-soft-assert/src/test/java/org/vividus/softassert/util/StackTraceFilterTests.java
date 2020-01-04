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

package org.vividus.softassert.util;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StackTraceFilterTests
{
    private static final String TEXT = "Caused by: ";
    private static final int LINE_NUMBER = 1;
    private final StackTraceElement stackTraceElement = new StackTraceElement(TEXT, TEXT, TEXT, LINE_NUMBER);
    private final StackTraceElement[] stackTraceElements = {stackTraceElement};

    @Mock
    private PrintWriter printWriter;

    @Mock
    private Throwable error;

    @InjectMocks
    private StackTraceFilter stackTraceFilter;

    @BeforeEach
    void beforeEach()
    {
        stackTraceFilter.setExclusions(List.of());
        stackTraceFilter.setInclusions(List.of());
    }

    @Test
    void testGetMessageEnabled()
    {
        stackTraceFilter.setEnabled(true);
        when(error.getStackTrace()).thenReturn(stackTraceElements);
        when(error.getCause()).thenReturn(error).thenReturn(null);
        stackTraceFilter.printFilteredStackTrace(error, printWriter);
        verify(printWriter).print(TEXT);
    }

    @Test
    void testGetMessageDisabled()
    {
        stackTraceFilter.printFilteredStackTrace(error, printWriter);
        verify(error).printStackTrace(printWriter);
    }

    @Test
    void testGetMessageIfStartsWithAnyIsTrue()
    {
        List<String> prefixes = new ArrayList<>();
        prefixes.add(TEXT);
        stackTraceFilter.setEnabled(true);
        stackTraceFilter.setExclusions(prefixes);
        stackTraceFilter.setInclusions(prefixes);
        when(error.getStackTrace()).thenReturn(stackTraceElements);
        stackTraceFilter.printFilteredStackTrace(error, printWriter);
        verify(error).getStackTrace();
    }
}
