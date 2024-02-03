/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.reportportal.jbehave;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.epam.reportportal.jbehave.ReportPortalFormat;
import com.epam.reportportal.jbehave.ReportPortalScenarioStoryReporter;
import com.epam.reportportal.jbehave.ReportPortalStepStoryReporter;
import com.epam.reportportal.jbehave.ReportPortalStoryReporter;
import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.tree.TestItemTree;
import com.google.common.eventbus.EventBus;

import org.jbehave.core.reporters.FilePrintStreamFactory;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdaptedReportPortalFormat extends ReportPortalFormat
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AdaptedReportPortalFormat.class);

    private final EventBus eventBus;
    private TestEntity testEntity;

    public AdaptedReportPortalFormat(EventBus eventBus)
    {
        super(ReportPortal.builder().build());
        this.eventBus = eventBus;
    }

    @Override
    public StoryReporter createStoryReporter(FilePrintStreamFactory factory, StoryReporterBuilder storyReporterBuilder)
    {
        if (testEntity == TestEntity.STEP)
        {
            LOGGER.info("The reporting of steps as ReportPortal test cases is deprecated. As such, the property "
                    + "'system.rp.test-entity' is deprecated and will be removed in VIVIDUS 0.8.0. "
                            + "The default behavior will be to report scenarios as test cases");
        }
        return new AdaptedDelegatingReportPortalStoryReporter(eventBus, testEntity.buildReporter(launch, itemTree));
    }

    @Override
    protected ReportPortalStoryReporter createReportPortalReporter(FilePrintStreamFactory factory,
            StoryReporterBuilder storyReporterBuilder)
    {
        throw new UnsupportedOperationException();
    }

    public void setTestEntity(TestEntity testEntity)
    {
        this.testEntity = testEntity;
    }

    public enum TestEntity
    {
        @Deprecated(since = "0.6.7", forRemoval = true)
        STEP(ReportPortalStepStoryReporter::new),
        SCENARIO(ReportPortalScenarioStoryReporter::new);

        private final BiFunction<Supplier<Launch>, TestItemTree, ReportPortalStoryReporter> factory;

        TestEntity(BiFunction<Supplier<Launch>, TestItemTree, ReportPortalStoryReporter> factory)
        {
            this.factory = factory;
        }

        ReportPortalStoryReporter buildReporter(Supplier<Launch> launch, TestItemTree itemTree)
        {
            return factory.apply(launch, itemTree);
        }
    }
}
