/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class SpringStepFactoryBeanPostProcessorTests
{
    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private SpringStepFactory springStepFactory;

    @Test
    void testDuplicateStepBeanNameNotAllowed()
    {
        SpringStepFactoryBeanPostProcessor postProcessor = new SpringStepFactoryBeanPostProcessor();
        postProcessor.setApplicationContext(applicationContext);

        String myBean = "myBean";
        String stepBeanName = "stepBeanNames-" + myBean;
        when(applicationContext.getBeanNamesForType(List.class)).thenReturn(new String[]{stepBeanName});
        when(applicationContext.getBean(stepBeanName, List.class)).thenReturn(List.of(myBean, myBean));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> postProcessor.postProcessAfterInitialization(springStepFactory, "anotherBean")
        );
        String expectedMessage =
                String.format("Duplicate step beans names are found: %s. Please, consider renaming to avoid conflicts",
                        myBean);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
