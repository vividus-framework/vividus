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

package org.vividus.bdd.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ResourceLoadExceptionTests
{
    private static final String MESSAGE = "message";
    private static final Exception CAUSE = new IllegalArgumentException();

    @Test
    void testExamplesTableLoadExceptionConstructorWith2Parameters()
    {
        ResourceLoadException exception = new ResourceLoadException(MESSAGE, CAUSE);
        assertEquals(MESSAGE, exception.getMessage());
        assertEquals(CAUSE, exception.getCause());
    }

    @Test
    void testExamplesTableLoadExceptionConstructorWithMessage()
    {
        ResourceLoadException exception = new ResourceLoadException(MESSAGE);
        assertEquals(MESSAGE, exception.getMessage());
    }

    @Test
    void testExamplesTableLoadExceptionConstructorWithCause()
    {
        ResourceLoadException exception = new ResourceLoadException(CAUSE);
        assertEquals(CAUSE, exception.getCause());
    }
}
