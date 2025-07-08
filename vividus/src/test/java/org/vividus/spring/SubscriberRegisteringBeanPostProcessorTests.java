/*
 * Copyright 2019-2024 the original author or authors.
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.eventbus.GlobalEventBus;

@ExtendWith(MockitoExtension.class)
class SubscriberRegisteringBeanPostProcessorTests
{
    private static final String BEAN_NAME = "not-used";

    private final SubscriberRegisteringBeanPostProcessor beanPostProcessor =
            new SubscriberRegisteringBeanPostProcessor();

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
        try (var globalEventBusStaticMock = mockStatic(GlobalEventBus.class))
        {
            EventBus eventBus = mock();
            globalEventBusStaticMock.when(GlobalEventBus::getEventBus).thenReturn(eventBus);
            beanPostProcessor.postProcessAfterInitialization(bean, BEAN_NAME);
            verify(eventBus).register(bean);
        }
    }

    @Test
    void testProcessingNonSubscriberObject()
    {
        try (var globalEventBusStaticMock = mockStatic(GlobalEventBus.class))
        {
            beanPostProcessor.postProcessAfterInitialization(new NonSubscriber(), BEAN_NAME);
            globalEventBusStaticMock.verifyNoInteractions();
        }
    }

    private static class Subscriber
    {
        @Subscribe
        public void doNothing()
        {
            // Do nothing
        }
    }

    private static final class InheritedSubscriber extends Subscriber
    {
    }

    private static final class NonSubscriber
    {
        public void doNothing()
        {
            // Do nothing
        }
    }
}
