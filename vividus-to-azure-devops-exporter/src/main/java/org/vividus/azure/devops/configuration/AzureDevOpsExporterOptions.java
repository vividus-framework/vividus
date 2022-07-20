/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.azure.devops.configuration;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("azure-devops-exporter")
public class AzureDevOpsExporterOptions
{
    private Path jsonResultsDirectory;
    private String organization;
    private String project;
    private String area;
    private SectionMapping sectionMapping;
    private boolean createTestRun;

    private TestRun testRun = new TestRun();

    public Path getJsonResultsDirectory()
    {
        return jsonResultsDirectory;
    }

    public void setJsonResultsDirectory(Path jsonResultsDirectory)
    {
        this.jsonResultsDirectory = jsonResultsDirectory;
    }

    public String getOrganization()
    {
        return organization;
    }

    public void setOrganization(String organization)
    {
        this.organization = organization;
    }

    public String getProject()
    {
        return project;
    }

    public void setProject(String project)
    {
        this.project = project;
    }

    public String getArea()
    {
        return area;
    }

    public void setArea(String area)
    {
        this.area = area;
    }

    public SectionMapping getSectionMapping()
    {
        return sectionMapping;
    }

    public void setSectionMapping(SectionMapping sectionMapping)
    {
        this.sectionMapping = sectionMapping;
    }

    public boolean isCreateTestRun()
    {
        return createTestRun;
    }

    public void setCreateTestRun(boolean createTestRun)
    {
        this.createTestRun = createTestRun;
    }

    public TestRun getTestRun()
    {
        return testRun;
    }

    public static class TestRun
    {
        private String name;
        private Integer testPlanId;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public Integer getTestPlanId()
        {
            return testPlanId;
        }

        public void setTestPlanId(Integer testPlanId)
        {
            this.testPlanId = testPlanId;
        }
    }
}
