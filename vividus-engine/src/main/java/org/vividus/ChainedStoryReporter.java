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

package org.vividus;

import java.util.ArrayList;

import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.reporters.DelegatingStoryReporter;
import org.jbehave.core.reporters.StoryReporter;

public class ChainedStoryReporter extends DelegatingStoryReporter
{
    public ChainedStoryReporter()
    {
        super(new ArrayList<>(1));
    }

    protected Throwable unwrap(Throwable throwable)
    {
        if (throwable instanceof UUIDExceptionWrapper)
        {
            return unwrap(throwable.getCause());
        }
        return throwable;
    }

    public void setNext(StoryReporter next)
    {
        getDelegates().add(next);
    }
}
