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

package org.vividus.zephyr.configuration;

import java.nio.file.Path;

import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("zephyr.exporter")
public class ZephyrExporterProperties
{
    @NotBlank(message = "Property 'zephyr.exporter.source-directory' must not be blank")
    private Path sourceDirectory;

    private boolean updateExecutionStatusesOnly;

    public Path getSourceDirectory()
    {
        return sourceDirectory;
    }

    public void setSourceDirectory(Path sourceDirectory)
    {
        this.sourceDirectory = sourceDirectory;
    }

    public boolean getUpdateExecutionStatusesOnly()
    {
        return updateExecutionStatusesOnly;
    }

    public void setUpdateExecutionStatusesOnly(boolean updateExecutionStatusesOnly)
    {
        this.updateExecutionStatusesOnly = updateExecutionStatusesOnly;
    }
}
