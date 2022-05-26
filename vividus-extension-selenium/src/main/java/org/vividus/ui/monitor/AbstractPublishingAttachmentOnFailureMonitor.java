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

package org.vividus.ui.monitor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.steps.NullStepMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.event.AssertionFailedEvent;

public abstract class AbstractPublishingAttachmentOnFailureMonitor extends NullStepMonitor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPublishingAttachmentOnFailureMonitor.class);

    private final ThreadLocal<Boolean> publishAttachmentOnFailureEnabled = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final RunContext runContext;
    private final IWebDriverProvider webDriverProvider;
    private final EventBus eventBus;
    private final String metaNameToSkipPublishing;
    private final String errorLogMessageOnFailure;

    protected AbstractPublishingAttachmentOnFailureMonitor(RunContext runContext, IWebDriverProvider webDriverProvider,
            EventBus eventBus, String metaNameToSkipPublishing, String errorLogMessageOnFailure)
    {
        this.runContext = runContext;
        this.webDriverProvider = webDriverProvider;
        this.eventBus = eventBus;
        this.metaNameToSkipPublishing = metaNameToSkipPublishing;
        this.errorLogMessageOnFailure = errorLogMessageOnFailure;
    }

    @Override
    public void beforePerforming(String step, boolean dryRun, Method method)
    {
        if (isPublishingEnabled(method) && !isPublishingDisabledInMeta())
        {
            publishAttachmentOnFailureEnabled.set(Boolean.TRUE);
        }
    }

    @Override
    public void afterPerforming(String step, boolean dryRun, Method method)
    {
        publishAttachmentOnFailureEnabled.remove();
    }

    @Subscribe
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void onAssertionFailure(@SuppressWarnings("unused") AssertionFailedEvent event)
    {
        if (publishAttachmentOnFailureEnabled.get() && webDriverProvider.isWebDriverInitialized())
        {
            try
            {
                createAttachment().map(AttachmentPublishEvent::new).ifPresent(eventBus::post);
            }
            // CHECKSTYLE:OFF
            catch (RuntimeException | IOException e)
            {
                LOGGER.error(errorLogMessageOnFailure,  e);
            }
            // CHECKSTYLE:ON
        }
    }

    private boolean isPublishingDisabledInMeta()
    {
        RunningStory runningStory = runContext.getRunningStory();
        return isPublishingDisabledInStoryMeta(runningStory) || isPublishingDisabledInScenarioMeta(runningStory);
    }

    private boolean isPublishingDisabledInStoryMeta(RunningStory runningStory)
    {
        return runningStory.getStory().getMeta().hasProperty(metaNameToSkipPublishing);
    }

    private boolean isPublishingDisabledInScenarioMeta(RunningStory runningStory)
    {
        return Optional.of(runningStory)
                .map(RunningStory::getRunningScenario)
                .map(RunningScenario::getScenario)
                .map(Scenario::getMeta)
                .map(m -> m.hasProperty(metaNameToSkipPublishing))
                .orElse(Boolean.FALSE);
    }

    protected <T extends Annotation> Optional<T> getAnnotation(Method method, Class<T> annotationClass)
    {
        return Optional.ofNullable(method)
                .map(m -> m.isAnnotationPresent(annotationClass) ? m : m.getDeclaringClass())
                .map(annotatedElement -> annotatedElement.getAnnotation(annotationClass));
    }

    protected abstract boolean isPublishingEnabled(Method method);

    protected abstract Optional<Attachment> createAttachment() throws IOException;
}
