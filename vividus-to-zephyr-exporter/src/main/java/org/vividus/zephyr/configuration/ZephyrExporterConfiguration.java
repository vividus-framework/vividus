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

import java.util.Map;

import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.vividus.zephyr.model.TestCaseStatus;

@ConstructorBinding
@ConfigurationProperties("zephyr")
public class ZephyrExporterConfiguration
{
    @NotBlank(message = "Property 'zephyr.project-key' should not be empty")
    private final String projectKey;

    @NotBlank(message = "Property 'zephyr.version-name' should not be empty")
    private final String versionName;

    @NotBlank(message = "Property 'zephyr.cycle-name' should not be empty")
    private final String cycleName;

    private final String folderName;

    private final Map<TestCaseStatus, String> statuses;

    public ZephyrExporterConfiguration(String projectKey, String versionName,
            String cycleName, String folderName, Map<TestCaseStatus, String> statuses)
    {
        this.projectKey = projectKey;
        this.versionName = versionName;
        this.cycleName = cycleName;
        this.folderName = folderName;
        this.statuses = statuses;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public String getVersionName()
    {
        return versionName;
    }

    public String getCycleName()
    {
        return cycleName;
    }

    public String getFolderName()
    {
        return folderName;
    }

    public Map<TestCaseStatus, String> getStatuses()
    {
        return statuses;
    }
}
