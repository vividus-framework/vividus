/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.xray.facade;

import java.io.IOException;
import java.util.List;

public interface XrayClient
{
    /**
     * Imports a test execution. Returns the Jira issue key of the created/updated test execution.
     *
     * @param executionJson serialized test execution JSON
     * @return the Jira issue key of the test execution
     * @throws IOException in case of any I/O errors
     */
    String importExecution(String executionJson) throws IOException;

    /**
     * Adds test cases to a test set identified by the given Jira issue key.
     *
     * @param testSetKey   the Jira issue key of the target test set
     * @param testCaseKeys the list of Jira issue keys of test cases to add
     * @throws IOException in case of any I/O errors
     */
    void addTestsToTestSet(String testSetKey, List<String> testCaseKeys) throws IOException;
}
