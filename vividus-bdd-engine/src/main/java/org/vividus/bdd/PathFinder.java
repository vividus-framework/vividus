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

package org.vividus.bdd;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.core.io.Resource;
import org.vividus.bdd.batch.BatchResourceConfiguration;
import org.vividus.bdd.resource.IBddResourceLoader;

public class PathFinder implements IPathFinder
{
    private static final String PLUS_SIGN_REGEX = "\\+";
    private static final String PLUS_SIGN_ENCODED = "%2b";

    private IBddResourceLoader bddResourceLoader;

    @Override
    public List<String> findPaths(BatchResourceConfiguration batchResourceConfiguration) throws IOException
    {
        List<String> paths = new ArrayList<>();
        process(batchResourceConfiguration, batchResourceConfiguration.getResourceIncludePatterns(), paths::add);
        process(batchResourceConfiguration, batchResourceConfiguration.getResourceExcludePatterns(), paths::remove);
        Collections.sort(paths);
        return paths;
    }

    private void process(BatchResourceConfiguration batchResourceConfiguration, List<String> resourcePatterns,
            Consumer<String> consumer) throws IOException
    {
        for (String resourcePattern : resourcePatterns)
        {
            Resource[] foundResources = bddResourceLoader.getResources(batchResourceConfiguration.getResourceLocation(),
                    resourcePattern);
            for (Resource resource : foundResources)
            {
                String resourceUri = resource.getURI().toASCIIString().replaceAll(PLUS_SIGN_REGEX, PLUS_SIGN_ENCODED);
                consumer.accept(URLDecoder.decode(resourceUri, StandardCharsets.UTF_8));
            }
        }
    }

    public void setBddResourceLoader(IBddResourceLoader bddResourceLoader)
    {
        this.bddResourceLoader = bddResourceLoader;
    }
}
