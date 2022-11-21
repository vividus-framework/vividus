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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.vividus.spring.BeanFactoryUtils;

public class SpringStepFactoryBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware
{
    private ApplicationContext applicationContext;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
    {
        if (bean instanceof SpringStepFactory)
        {
            List<String> stepBeanNames = BeanFactoryUtils.mergeLists(applicationContext, "stepBeanNames-");
            String duplicateStepBeanNames = stepBeanNames.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet().stream()
                    .filter(stepBeanNameCount -> stepBeanNameCount.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining(", "));
            Validate.validState(duplicateStepBeanNames.isEmpty(),
                    "Duplicate step beans names are found: %s. Please, consider renaming to avoid conflicts",
                    duplicateStepBeanNames);
            ((SpringStepFactory) bean).setStepTypes(
                    stepBeanNames.stream().map(applicationContext::getType).collect(toList()));
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }
}
