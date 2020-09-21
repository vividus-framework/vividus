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

package org.vividus.spring;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.stream.Stream;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriberRegisteringBeanPostProcessorTests
{
    @Mock
    private EventBus eventBus;

    @InjectMocks
    private SubscriberRegisteringBeanPostProcessor beanPostProcessor;

    static Stream<Object> subscribers()
    {
        return Stream.of(
            new Subscriber(),
            new InheritedSubscriber()
        );
    }

    @MethodSource("subscribers")
    @ParameterizedTest
    void testProcessingSubscriberObject(Object bean)
    {
        beanPostProcessor.postProcessAfterInitialization(bean, null);
        verify(eventBus).register(bean);
    }

    @Test
    void testProcessingNonSubscriberObject()
    {
        beanPostProcessor.postProcessAfterInitialization(new NonSubscriber(), null);
        verifyNoInteractions(eventBus);
    }

    private static class Subscriber
    {
        @Subscribe
        public void doNothing()
        {
            // Do nothing
        }
    }

    private static class InheritedSubscriber extends Subscriber
    {
    }

    private static class NonSubscriber
    {
        public void doNothing()
        {
            // Do nothing
        }
    }
}
