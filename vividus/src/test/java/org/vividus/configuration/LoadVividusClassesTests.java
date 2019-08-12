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

package org.vividus.configuration;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

class LoadVividusClassesTests
{
    private static final String SPLITTER = ": ";
    private static final String LOCATION_PATTERN = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
            + "/org/vividus/**/*.class";
    private static final Pattern CLASS_PATTERN = Pattern.compile(".+(org/vividus/.+)\\.class");

    @Test
     void testClassLoading() throws Exception
    {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        ClassLoader classLoader = resourcePatternResolver.getClassLoader();
        for (Resource resource : resourcePatternResolver.getResources(LOCATION_PATTERN))
        {
            String url = resource.getURL().toExternalForm();
            Matcher matcher = CLASS_PATTERN.matcher(url);
            if (matcher.find())
            {
                String className = matcher.group(1);
                className = className.replace('/', '.');
                try
                {
                    Class.forName(className, false, classLoader);
                }
                catch (NoClassDefFoundError e)
                {
                    fail(className + SPLITTER + e.getClass().getCanonicalName() + SPLITTER + e.getMessage());
                }
            }
        }
    }
}
