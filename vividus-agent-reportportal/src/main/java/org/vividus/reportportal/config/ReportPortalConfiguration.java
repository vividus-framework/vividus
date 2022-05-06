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

package org.vividus.reportportal.config;

import com.google.common.eventbus.EventBus;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.vividus.ExtendedStoryReporterBuilder;
import org.vividus.reportportal.config.condition.AttachmentPublishingPropertyCondition;
import org.vividus.reportportal.config.condition.ReportPortalEnablePropertyCondition;
import org.vividus.reportportal.jbehave.AdaptedReportPortalFormat;
import org.vividus.reportportal.jbehave.AdaptedReportPortalFormat.TestEntity;
import org.vividus.reportportal.listener.AttachmentListener;

@Conditional(ReportPortalEnablePropertyCondition.class)
@Configuration
public class ReportPortalConfiguration implements InitializingBean
{
    @Value("#{systemProperties['rp.test-entity']}")
    private TestEntity testEntity;

    @Autowired
    private AdaptedReportPortalFormat adaptedReportPortalFormat;

    @Autowired
    private ExtendedStoryReporterBuilder storyReporterBuilder;

    @Bean
    @Conditional(AttachmentPublishingPropertyCondition.class)
    public AttachmentListener attachmentListener(EventBus eventBus)
    {
        AttachmentListener attachmentListener = new AttachmentListener();
        eventBus.register(attachmentListener);
        return attachmentListener;
    }

    @Bean
    public AdaptedReportPortalFormat adaptedReportPortalFormat(EventBus eventBus)
    {
        AdaptedReportPortalFormat reportPortalFormat = new AdaptedReportPortalFormat(eventBus);
        reportPortalFormat.setTestEntity(testEntity);
        return reportPortalFormat;
    }

    @Override
    public void afterPropertiesSet()
    {
        storyReporterBuilder.withFormats(adaptedReportPortalFormat);
    }
}
