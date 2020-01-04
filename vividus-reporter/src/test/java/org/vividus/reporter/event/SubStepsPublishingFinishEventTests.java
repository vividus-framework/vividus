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

package org.vividus.reporter.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class SubStepsPublishingFinishEventTests
{
    @Test
    void testSetGetSubStepTitle()
    {
        String subStepTitle = "subStepTitle";
        SubStepsPublishingFinishEvent event = new SubStepsPublishingFinishEvent();
        event.setSubStepTitle(subStepTitle);
        assertEquals(subStepTitle, event.getSubStepTitle());
    }

    @Test
    void testSetGetSubStepThrowable()
    {
        Throwable throwable = mock(Throwable.class);
        SubStepsPublishingFinishEvent event = new SubStepsPublishingFinishEvent();
        event.setSubStepThrowable(throwable);
        assertEquals(throwable, event.getSubStepThrowable());
    }
}
