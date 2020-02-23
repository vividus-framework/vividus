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

package org.vividus.bdd.issue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.vividus.softassert.issue.IKnownIssueDataProvider;

public class KnownIssueCheckerBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware
{
    private static final String DATA_PROVIDER_PREFIX = "knownIssueChecker-";
    private ApplicationContext applicationContext;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
    {
        if (!(bean instanceof DelegatingKnownIssueDataProvider))
        {
            return bean;
        }
        DelegatingKnownIssueDataProvider issueChecker = (DelegatingKnownIssueDataProvider) bean;
        issueChecker.setKnownIssueDataProviders(getDataProviders());
        return issueChecker;
    }

    private Map<String, IKnownIssueDataProvider> getDataProviders()
    {
        Map<String, IKnownIssueDataProvider> allDataProviders = new HashMap<>();
        Stream.of(applicationContext.getBeanNamesForType(Map.class))
                .filter(name -> name.startsWith(DATA_PROVIDER_PREFIX))
                .map(name -> applicationContext.getBean(name, Map.class))
                .forEach(allDataProviders::putAll);
        return allDataProviders;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }
}
