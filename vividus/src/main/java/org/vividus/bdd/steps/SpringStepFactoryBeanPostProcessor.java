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

package org.vividus.bdd.steps;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringStepFactoryBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware
{
    private static final String CUSTOM_STEPS_BEAN_PREFIX = "stepBeanNames-";

    private ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
    {
        if (!(bean instanceof SpringStepFactory))
        {
            return bean;
        }
        List<String> allStepBeanNames = new LinkedList<>();
        Stream.of(applicationContext.getBeanDefinitionNames())
                .filter(bdn -> bdn.startsWith(CUSTOM_STEPS_BEAN_PREFIX))
                .map(bdn -> applicationContext.getBean(bdn, Collection.class))
                .forEach(allStepBeanNames::addAll);

        List<Class<?>> stepTypes = allStepBeanNames.stream().map(applicationContext::getType).collect(toList());
        ((SpringStepFactory) bean).setStepTypes(stepTypes);
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }
}
