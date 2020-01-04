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

package org.vividus.bdd.examples;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.vividus.bdd.resource.IBddResourceLoader;
import org.vividus.bdd.resource.ResourceLoadException;
import org.vividus.bdd.steps.ParameterAdaptor;

public class ExamplesTableFileLoader implements IExamplesTableLoader
{
    private static final Map<String, String> TABLES_CACHE = new ConcurrentHashMap<>();
    private IBddResourceLoader bddResourceLoader;
    private ParameterAdaptor parameterAdaptor;
    private boolean cacheTables;

    @Override
    public String loadExamplesTable(String exampleTablePath)
    {
        String tablePath = (String) parameterAdaptor.convert(exampleTablePath);
        String table;
        if (cacheTables)
        {
            table = TABLES_CACHE.computeIfAbsent(tablePath, this::loadTable);
        }
        else
        {
            table = loadTable(tablePath);
        }
        return table;
    }

    private String loadTable(String exampleTablePath)
    {
        Resource resource = bddResourceLoader.getResource(exampleTablePath);
        try (InputStream inputStream = resource.getInputStream())
        {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new ResourceLoadException(e.getMessage(), e);
        }
    }

    public void setBddResourceLoader(IBddResourceLoader bddResourceLoader)
    {
        this.bddResourceLoader = bddResourceLoader;
    }

    public void setParameterAdaptor(ParameterAdaptor parameterAdaptor)
    {
        this.parameterAdaptor = parameterAdaptor;
    }

    public void setCacheTables(boolean cacheTables)
    {
        this.cacheTables = cacheTables;
    }
}
