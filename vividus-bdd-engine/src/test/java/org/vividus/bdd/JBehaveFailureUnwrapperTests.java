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

package org.vividus.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.junit.jupiter.api.Test;

class JBehaveFailureUnwrapperTests
{
    @Test
    void shouldDoNothingWithUnwrappableException()
    {
        IOException ioException = new IOException();
        assertEquals(ioException, JBehaveFailureUnwrapper.unwrapCause(ioException));
    }

    @Test
    void shouldUnwrapUUIDExceptionWrapper()
    {
        IOException ioException = new IOException();
        UUIDExceptionWrapper uuidExceptionWrapper = new UUIDExceptionWrapper(ioException);
        assertEquals(ioException, JBehaveFailureUnwrapper.unwrapCause(uuidExceptionWrapper));
    }

    @Test
    void shouldUnwrapUUIDExceptionWrapperWrappingBeforeOrAfterFailed()
    {
        IOException ioException = new IOException();
        BeforeOrAfterFailed beforeOrAfterFailed = new BeforeOrAfterFailed(ioException);
        UUIDExceptionWrapper uuidExceptionWrapper = new UUIDExceptionWrapper(beforeOrAfterFailed);
        assertEquals(ioException, JBehaveFailureUnwrapper.unwrapCause(uuidExceptionWrapper));
    }
}
