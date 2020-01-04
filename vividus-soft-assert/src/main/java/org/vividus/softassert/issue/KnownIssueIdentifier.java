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

package org.vividus.softassert.issue;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KnownIssueIdentifier
{
    private KnownIssueType type;
    private Pattern testSuiteCompiledPattern;
    private Pattern testCaseCompiledPattern;
    private Pattern testStepCompiledPattern;
    private Pattern assertionCompiledPattern;
    private Map<String, Pattern> additionalCompiledPatterns = Collections.emptyMap();
    private Map<String, Pattern> dynamicCompiledPatterns = Collections.emptyMap();

    public KnownIssueType getType()
    {
        return type;
    }

    public void setType(KnownIssueType type)
    {
        this.type = type;
    }

    public void setTestSuitePattern(String testSuitePattern)
    {
        this.testSuiteCompiledPattern = Pattern.compile(testSuitePattern, Pattern.DOTALL);
    }

    public void setTestCasePattern(String testCasePattern)
    {
        this.testCaseCompiledPattern = Pattern.compile(testCasePattern, Pattern.DOTALL);
    }

    public void setTestStepPattern(String testStepPattern)
    {
        this.testStepCompiledPattern = Pattern.compile(testStepPattern, Pattern.DOTALL);
    }

    public void setAssertionPattern(String assertionPattern)
    {
        this.assertionCompiledPattern = Pattern.compile(assertionPattern, Pattern.DOTALL);
    }

    public Map<String, Pattern> getAdditionalCompiledPatterns()
    {
        return additionalCompiledPatterns;
    }

    public void setAdditionalPatterns(Map<String, String> additionalPatterns)
    {
        this.additionalCompiledPatterns = additionalPatterns.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Pattern.compile(e.getValue(), Pattern.DOTALL)));
    }

    public void setDynamicPatterns(Map<String, String> dynamicPatterns)
    {
        this.dynamicCompiledPatterns = dynamicPatterns.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Pattern.compile(e.getValue(), Pattern.DOTALL)));
    }

    public Map<String, Pattern> getDynamicCompiledPatterns()
    {
        return dynamicCompiledPatterns;
    }

    public Pattern getTestSuiteCompiledPattern()
    {
        return testSuiteCompiledPattern;
    }

    public Pattern getTestCaseCompiledPattern()
    {
        return testCaseCompiledPattern;
    }

    public Pattern getTestStepCompiledPattern()
    {
        return testStepCompiledPattern;
    }

    public Pattern getAssertionCompiledPattern()
    {
        return assertionCompiledPattern;
    }
}
