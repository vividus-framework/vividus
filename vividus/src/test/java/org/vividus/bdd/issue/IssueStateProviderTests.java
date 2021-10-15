/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.bdd.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IssueStateProviderTests
{
    private static final String VALID_ISSUE_ID = "ISSUE-123";
    private static final Optional<String> UNKNOWN = Optional.of("UNKNOWN");

    @InjectMocks
    private IssueStateProvider issueStateProvider;

    @Test
    void testGetStatusSuccess()
    {
        Optional<String> actualStatus = issueStateProvider.getIssueStatus(VALID_ISSUE_ID);
        assertEquals(UNKNOWN, actualStatus);
    }

    @Test
    void testGetResolutionSuccess()
    {
        Optional<String> actualStatus = issueStateProvider.getIssueResolution(VALID_ISSUE_ID);
        assertEquals(UNKNOWN, actualStatus);
    }
}
