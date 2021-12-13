/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FilenameUtils;
import org.jbehave.core.io.LoadFromClasspath;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.vividus.steps.VariableResolver;

public class StoryLoader extends LoadFromClasspath
{
    private final VariableResolver variableResolver;
    private final ExamplesTableLoader examplesTableLoader;
    private final ResourcePatternResolver resourcePatternResolver;

    public StoryLoader(VariableResolver variableResolver, ExamplesTableLoader examplesTableLoader,
            ResourcePatternResolver resourcePatternResolver)
    {
        super(StandardCharsets.UTF_8);
        this.variableResolver = variableResolver;
        this.examplesTableLoader = examplesTableLoader;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @Override
    public String loadResourceAsText(String resourcePath)
    {
        String path = (String) variableResolver.resolve(resourcePath);
        if ("table".equals(FilenameUtils.getExtension(path)))
        {
            return examplesTableLoader.loadExamplesTable(path);
        }
        return super.loadResourceAsText(path);
    }

    @Override
    protected InputStream resourceAsStream(String resourcePath)
    {
        try
        {
            return resourcePatternResolver.getResource(resourcePath).getInputStream();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
}
