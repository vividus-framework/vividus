/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.runner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.vividus.configuration.BeanFactory;

class DeprecatedPropertiesReplacerTests
{
    private static final String PROPERTIES_ROOT_ARG = "--propertiesRootDirectory";
    private static final String PROPERTIES_ROOT_DIRECTORY = "root/vividus/vividus-tests/src/main/resources/properties";
    private static final String PROPERTIES_SEARCH_PATTERN = "/**/*.properties";
    private static final String DEPRECATED_PROPERTIES_CLASSPATH =
            "classpath*:/properties/deprecated/deprecated.properties";

    @Test
    void shouldReplaceProperties() throws IOException, ParseException
    {
        String properties1ContentDeprecated = "# static.deprecated.property\nstatic.deprecated.property=value\n";
        String properties1ContentProcessed = "# static.deprecated.property\nstatic.actual.property=value\n";
        String properties2ContentDeprecated = "dynamic.deprecated.property.key1=value1\ndynamic.deprecated.property."
                + "key2=value2\ndynamic.actual.property.key3=value3";
        String properties2ContentProcessed = "dynamic.actual.property.key1=value1\ndynamic.actual.property.key2=value2"
                + "\ndynamic.actual.property.key3=value3";

        try (var beanFactory = mockStatic(BeanFactory.class);
             var fileUtils = mockStatic(FileUtils.class))
        {
            var resourcePatternResolver = mock(ResourcePatternResolver.class);
            beanFactory.when(BeanFactory::getResourcePatternResolver).thenReturn(resourcePatternResolver);

            Resource deprecatedProperties1 = mockResource("static.deprecated.property=static.actual.property",
                    "jar:file:/some.jar!/properties/deprecated/deprecated.properties");
            Resource deprecatedProperties2 = mockResource(
                    "dynamic\\.deprecated\\.property\\.(.*)=dynamic.actual.property.$1",
                    "jar:file:/some1.jar!/properties/deprecated/deprecated.properties");
            when(resourcePatternResolver.getResources(DEPRECATED_PROPERTIES_CLASSPATH))
                    .thenReturn(new Resource[] { deprecatedProperties1, deprecatedProperties2 });

            Resource properties1 = mockResource(properties1ContentDeprecated,
                    "file:tests/src/main/resources/properties/environment/environment.properties");
            Resource properties2 = mockResource(properties2ContentDeprecated,
                    "file:tests/src/main/resources/properties/environment/integration/environment.properties");
            when(resourcePatternResolver
                    .getResources(Path.of(PROPERTIES_ROOT_DIRECTORY).toUri() + PROPERTIES_SEARCH_PATTERN))
                            .thenReturn(new Resource[] { properties1, properties2, deprecatedProperties1 });

            DeprecatedPropertiesReplacer.main(new String[] { PROPERTIES_ROOT_ARG, PROPERTIES_ROOT_DIRECTORY });
            verifyReplacedProperties(fileUtils, properties1ContentProcessed);
            verifyReplacedProperties(fileUtils, properties2ContentProcessed);
            verify(deprecatedProperties1).getContentAsString(StandardCharsets.UTF_8);
            verify(deprecatedProperties2).getContentAsString(StandardCharsets.UTF_8);
        }
    }

    private Resource mockResource(String resourceContentAsString, String url) throws IOException
    {
        Resource resource = mock(Resource.class);
        when(resource.getContentAsString(StandardCharsets.UTF_8)).thenReturn(resourceContentAsString);
        when(resource.getURL()).thenReturn(new URL(url));
        return resource;
    }

    private void verifyReplacedProperties(MockedStatic<FileUtils> fileUtils, String newPropertiesContent)
    {
        fileUtils
                .verify(() -> FileUtils.writeStringToFile(any(), eq(newPropertiesContent), eq(StandardCharsets.UTF_8)));
    }
}
