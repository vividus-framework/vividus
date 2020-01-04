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

package org.vividus.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanFactory;

@ExtendWith(MockitoExtension.class)
class SpringStepFactoryTests
{
    private static final Class<?> STEPS_CLASS = SpringStepFactoryTests.class;

    private final List<Class<?>> stepTypes = List.of(STEPS_CLASS);

    private SpringStepFactory springStepFactory;

    @Mock
    private BeanFactory beanFactory;

    @BeforeEach
    void beforeEach()
    {
        springStepFactory = new SpringStepFactory(null);
        springStepFactory.setBeanFactory(beanFactory);
        springStepFactory.setStepTypes(stepTypes);
    }

    @Test
    void testCreateInstanceOfType()
    {
        springStepFactory.createInstanceOfType(STEPS_CLASS);
        verify(beanFactory).getBean(STEPS_CLASS);
    }

    @Test
    void testStepsTypes()
    {
        assertEquals(stepTypes, springStepFactory.stepsTypes());
    }
}
