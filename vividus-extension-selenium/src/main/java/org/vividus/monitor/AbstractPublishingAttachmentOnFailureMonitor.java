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

package org.vividus.monitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.apache.commons.lang3.function.FailableRunnable;
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

    public AbstractPublishingAttachmentOnFailureMonitor(RunContext runContext, IWebDriverProvider webDriverProvider,
            EventBus eventBus, String metaNameToSkipPublishing)
    {
        this.runContext = runContext;
        this.webDriverProvider = webDriverProvider;
        this.eventBus = eventBus;
        this.metaNameToSkipPublishing = metaNameToSkipPublishing;
    }

    @Override
    public void beforePerforming(String step, boolean dryRun, Method method)
    {
        if (isPublishingEnabled(method) && !isPublishingDisabledInStoryMeta()
                && !isPublishingDisabledInScenarioMeta())
        {
            publishAttachmentOnFailureEnabled.set(Boolean.TRUE);
        }
    }

    @Override
    public void afterPerforming(String step, boolean dryRun, Method method)
    {
        if (isPublishingEnabled(method))
        {
            publishAttachmentOnFailureEnabled.set(Boolean.FALSE);
        }
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event)
    {
        if (publishAttachmentOnFailureEnabled.get() && webDriverProvider.isWebDriverInitialized())
        {
            publishAttachment();
        }
    }

    private boolean isPublishingDisabledInStoryMeta()
    {
        RunningStory runningStory = runContext.getRunningStory();
        return runningStory.getStory().getMeta().hasProperty(metaNameToSkipPublishing);
    }

    private boolean isPublishingDisabledInScenarioMeta()
    {
        return Optional.of(runContext.getRunningStory()).map(RunningStory::getRunningScenario)
                .map(RunningScenario::getScenario).map(Scenario::getMeta)
                .map(m -> m.hasProperty(metaNameToSkipPublishing)).orElse(Boolean.FALSE);
    }

    protected abstract boolean isPublishingEnabled(Method method);

    protected abstract void publishAttachment();

    protected void publishAttachment(byte[] content, String fileName)
    {
        Attachment attachment = new Attachment(content, fileName);
        eventBus.post(new AttachmentPublishEvent(attachment));
    }

    protected Annotation getAnnotation(Method method, Class<? extends Annotation> annotationClass)
    {
        if (method != null)
        {
            AnnotatedElement annotatedElement = method.isAnnotationPresent(annotationClass) ? method
                    : method.getDeclaringClass();
            return annotatedElement.getAnnotation(annotationClass);
        }
        return null;
    }

    protected void performOperation(FailableRunnable<RuntimeException> operation, String errorMessage)
    {
        try
        {
            operation.run();
        }
        // CHECKSTYLE:OFF
        catch (RuntimeException e)
        {
            LOGGER.error(errorMessage,  e);
        }
        // CHECKSTYLE:ON
    }
}
