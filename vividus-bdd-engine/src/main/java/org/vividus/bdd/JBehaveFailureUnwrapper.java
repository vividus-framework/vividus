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

import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.UUIDExceptionWrapper;

public final class JBehaveFailureUnwrapper
{
    private JBehaveFailureUnwrapper()
    {
    }

    public static Throwable unwrapCause(Throwable throwable)
    {
        Throwable cause = throwable instanceof UUIDExceptionWrapper ? throwable.getCause() : null;
        if (cause instanceof BeforeOrAfterFailed)
        {
            cause = cause.getCause();
        }
        else
        {
            if (cause == null)
            {
                cause = throwable;
            }
        }
        return cause;
    }
}
