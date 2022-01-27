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

package org.vividus.exporter.config.facade;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.exporter.facade.ExporterFacade;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class ExporterFacadeTests
{
    private static final String REQUIREMENT_ID = "STUB-REQ-0";
    private static final String ISSUE_ID = "ISSUE_ID";
    private static final String LINK_NAME = "Tests";

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(ExporterFacade.class);

    @Mock private JiraFacade jiraFacade;

    @Test
    void createLinkIfRequirementIdPresents() throws IOException, JiraConfigurationException
    {
        ExporterFacade.createTestsLink(ISSUE_ID, Optional.of(REQUIREMENT_ID), jiraFacade);
        assertThat(testLogger.getLoggingEvents(), is(Collections.singletonList(
                info("Create '{}' link from {} to {}", LINK_NAME, ISSUE_ID, REQUIREMENT_ID))));
        verify(jiraFacade).createIssueLink(ISSUE_ID, REQUIREMENT_ID, LINK_NAME);
    }

    @Test
    void createLinkIfRequirementIdAbsent() throws IOException, JiraConfigurationException
    {
        ExporterFacade.createTestsLink(ISSUE_ID, Optional.empty(), jiraFacade);
        verifyNoMoreInteractions(jiraFacade);
    }
}
