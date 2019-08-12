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

package org.vividus.ui.web.action;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.ui.Wait;

class WaitFactoryTests
{
    private static final Duration TIMEOUT = Duration.ofSeconds(42);

    private WaitFactory waitFactory;

    @BeforeEach
    void beforeEach()
    {
        waitFactory = new WaitFactory();
        waitFactory.setTimeout(TIMEOUT);
        waitFactory.setPollingPeriod(TIMEOUT);
    }

    @Test
    void testCreateWait()
    {
        SearchContext input = Mockito.mock(SearchContext.class);
        Wait<SearchContext> wait = waitFactory.createWait(input);
        assertThat(wait, instanceOf(DescriptiveWait.class));
        DescriptiveWait<?> descriptiveWait = (DescriptiveWait<?>) wait;
        assertEquals(TIMEOUT, descriptiveWait.getTimeout());
    }

    @Test
    void testCreateWaitWithCustomTimeout()
    {
        SearchContext input = Mockito.mock(SearchContext.class);
        Duration timeout = Duration.ofHours(1);
        Wait<SearchContext> wait = waitFactory.createWait(input, timeout);
        assertThat(wait, instanceOf(DescriptiveWait.class));
        DescriptiveWait<?> descriptiveWait = (DescriptiveWait<?>) wait;
        assertEquals(timeout, descriptiveWait.getTimeout());
    }
}
