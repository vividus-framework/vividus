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

package org.vividus.xray.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("jira.fields-mapping")
public class JiraFieldsMapping
{
    private String testCaseType;
    private String manualSteps;
    private String cucumberScenarioType;
    private String cucumberScenario;

    public String getCucumberScenarioType()
    {
        return cucumberScenarioType;
    }

    public void setCucumberScenarioType(String cucumberScenarioType)
    {
        this.cucumberScenarioType = cucumberScenarioType;
    }

    public String getCucumberScenario()
    {
        return cucumberScenario;
    }

    public void setCucumberScenario(String cucumberScenario)
    {
        this.cucumberScenario = cucumberScenario;
    }

    public String getTestCaseType()
    {
        return testCaseType;
    }

    public void setTestCaseType(String testCaseType)
    {
        this.testCaseType = testCaseType;
    }

    public String getManualSteps()
    {
        return manualSteps;
    }

    public void setManualSteps(String manualSteps)
    {
        this.manualSteps = manualSteps;
    }
}
