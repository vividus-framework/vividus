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

package org.vividus.configuration;

import java.io.IOException;
import java.util.Map;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.io.support.ResourcePatternResolver;

public final class BeanFactory
{
    private static final String[] LOCATIONS = {"classpath*:/org/vividus/spring.xml", "classpath*:/spring.xml"};

    private static GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();

    private BeanFactory()
    {
    }

    public static ResourcePatternResolver getResourcePatternResolver()
    {
        return getApplicationContext();
    }

    private static GenericXmlApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    public static boolean isActive()
    {
        return getApplicationContext().isActive();
    }

    public static void open()
    {
        String[] profiles = getActiveProfiles();
        open(profiles);
    }

    public static void open(String[] profiles)
    {
        if (isActive())
        {
            return;
        }
        getApplicationContext().getEnvironment().setActiveProfiles(profiles);
        getApplicationContext().load(LOCATIONS);
        getApplicationContext().refresh();
        getApplicationContext().registerShutdownHook();
        getApplicationContext().start();
    }

    public static Object getBean(String beanName)
    {
        return getApplicationContext().getBean(beanName);
    }

    public static <T> T getBean(Class<T> beanClass)
    {
        return getApplicationContext().getBean(beanClass);
    }

    public static <T> T getBean(String beanName, Class<T> beanClass)
    {
        return getApplicationContext().getBean(beanName, beanClass);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> beanClass)
    {
        return getApplicationContext().getBeansOfType(beanClass);
    }

    public static String[] getBeanDefinitionNames()
    {
        return getApplicationContext().getBeanDefinitionNames();
    }

    public static void reset()
    {
        close();
        applicationContext = new GenericXmlApplicationContext();
    }

    public static void close()
    {
        if (!isActive())
        {
            return;
        }
        getApplicationContext().close();
    }

    private static String[] getActiveProfiles()
    {
        try
        {
            String value = ConfigurationResolver.getInstance().getProperties()
                    .getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
            return value != null ? value.split(",") : new String[]{};
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
