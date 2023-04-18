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

package org.vividus.xray.configuration;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("xray-exporter")
public class XrayExporterOptions
{
    private Path jsonResultsDirectory;
    private String testSetKey;
    private String testExecutionKey;
    private String testExecutionSummary;

    public Path getJsonResultsDirectory()
    {
        return jsonResultsDirectory;
    }

    public void setJsonResultsDirectory(Path jsonResultsDirectory)
    {
        this.jsonResultsDirectory = jsonResultsDirectory;
    }

    public String getTestSetKey()
    {
        return testSetKey;
    }

    public void setTestSetKey(String testSetKey)
    {
        this.testSetKey = testSetKey;
    }

    public String getTestExecutionKey()
    {
        return testExecutionKey;
    }

    public void setTestExecutionKey(String testExecutionKey)
    {
        this.testExecutionKey = testExecutionKey;
    }

    public String getTestExecutionSummary()
    {
        return testExecutionSummary;
    }

    public void setTestExecutionSummary(String testExecutionSummary)
    {
        this.testExecutionSummary = testExecutionSummary;
    }
}
