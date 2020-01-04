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

package org.vividus.reportportal.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;

import java.util.List;

import com.epam.reportportal.jbehave.ReportPortalStoryReporter;

import org.jbehave.core.reporters.StoryReporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportPortalConfigurationTests
{
    @Mock
    private List<StoryReporter> storyReporters;

    @InjectMocks
    private ReportPortalConfiguration configuration;

    @Test
    void shouldAddReportPortalStoryReporterAfterPropertiesSet()
    {
        configuration.afterPropertiesSet();
        verify(storyReporters).add(isA(ReportPortalStoryReporter.class));
    }

    @Test
    void assertionFailureListener()
    {
        assertNotNull(configuration.assertionFailureListener());
    }

    @Test
    void reportPortalViewGenerator()
    {
        assertNotNull(configuration.reportPortalViewGenerator());
    }
}
