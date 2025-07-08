/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;

public class SpringResourceFileLoader implements ResourceFileLoader
{
    private final ResourceLoader resourceLoader;

    public SpringResourceFileLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public File loadFile(String filePath) throws IOException
    {
        Resource resource = resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + filePath);
        if (!resource.exists())
        {
            resource = resourceLoader.getResource(ResourceUtils.FILE_URL_PREFIX + filePath);
        }
        File file = ResourceUtils.isFileURL(resource.getURL())
                ? resource.getFile() : unpackFile(resource, filePath);
        Validate.isTrue(file.exists(), "File %s exists", filePath);
        return file;
    }

    private static File unpackFile(Resource resource, String filePath) throws IOException
    {
        File file = new File(filePath);
        try (InputStream resourceInputStream = resource.getInputStream())
        {
            FileUtils.copyInputStreamToFile(resourceInputStream, file);
            file.deleteOnExit();
        }
        return file;
    }
}
