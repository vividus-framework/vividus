/*
 * Copyright 2019-2025 the original author or authors.
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
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;

@ConfigurationProperties("xray-exporter")
public class XrayExporterOptions
{
    private Path jsonResultsDirectory;
    private String testSetKey;
    private boolean testCaseUpdatesEnabled;
    @Name("test-case")
    private TestCaseOptions testCaseOptions;
    @Name("test-execution")
    private TestExecutionOptions testExecutionOptions;

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

    public boolean isTestCaseUpdatesEnabled()
    {
        return testCaseUpdatesEnabled;
    }

    public void setTestCaseUpdatesEnabled(boolean testCaseUpdatesEnabled)
    {
        this.testCaseUpdatesEnabled = testCaseUpdatesEnabled;
    }

    public TestCaseOptions getTestCaseOptions()
    {
        return testCaseOptions;
    }

    public void setTestCaseOptions(TestCaseOptions testCaseOptions)
    {
        this.testCaseOptions = testCaseOptions;
    }

    public TestExecutionOptions getTestExecutionOptions()
    {
        return testExecutionOptions;
    }

    public void setTestExecutionOptions(TestExecutionOptions testExecutionOptions)
    {
        this.testExecutionOptions = testExecutionOptions;
    }

    public static final class TestCaseOptions
    {
        private boolean useScenarioTitleAsDescription;

        public boolean isUseScenarioTitleAsDescription()
        {
            return useScenarioTitleAsDescription;
        }

        public void setUseScenarioTitleAsDescription(boolean useScenarioTitleAsDescription)
        {
            this.useScenarioTitleAsDescription = useScenarioTitleAsDescription;
        }
    }

    public static final class TestExecutionOptions
    {
        private String key;
        private String summary;
        private String description;
        private List<Path> attachments;

        public String getKey()
        {
            return key;
        }

        public void setKey(String key)
        {
            this.key = key;
        }

        public String getSummary()
        {
            return summary;
        }

        public void setSummary(String summary)
        {
            this.summary = summary;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public List<Path> getAttachments()
        {
            return attachments;
        }

        public void setAttachments(List<Path> attachments)
        {
            this.attachments = attachments;
        }
    }
}
