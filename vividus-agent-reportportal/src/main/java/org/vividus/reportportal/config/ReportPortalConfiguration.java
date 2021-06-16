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

package org.vividus.reportportal.config;

import java.util.List;

import com.epam.reportportal.jbehave.ReportPortalStoryReporter;
import com.epam.reportportal.jbehave.ReportPortalViewGenerator;

import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.ViewGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.vividus.reportportal.config.condition.ReportPortalEnableCondition;
import org.vividus.reportportal.listener.AssertionFailureListener;

@Conditional(ReportPortalEnableCondition.class)
@Configuration
public class ReportPortalConfiguration implements InitializingBean
{
    @Autowired
    @Qualifier("storyReporters")
    private List<StoryReporter> storyReporters;

    @Override
    public void afterPropertiesSet()
    {
        storyReporters.add(new ReportPortalStoryReporter());
    }

    @Bean
    public AssertionFailureListener assertionFailureListener()
    {
        return new AssertionFailureListener();
    }

    @Bean
    public ViewGenerator reportPortalViewGenerator()
    {
        return new ReportPortalViewGenerator();
    }
}
