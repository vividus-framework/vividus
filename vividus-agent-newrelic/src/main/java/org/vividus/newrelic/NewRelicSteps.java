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

package org.vividus.newrelic;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.embedder.StoryControls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.batch.BatchStorage;
import org.vividus.context.RunContext;
import org.vividus.model.RunningStory;
import org.vividus.newrelic.model.RunPhase;
import org.vividus.newrelic.model.Status;
import org.vividus.newrelic.model.VividusRunEvent;
import org.vividus.newrelic.model.VividusStoryEvent;
import org.vividus.util.property.IPropertyParser;

public class NewRelicSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NewRelicSteps.class);

    private static final String ATTRIBUTE_PREFIX = "newrelic.event.attribute.";
    private static final int MAX_ATTRIBUTE_NAME_LENGTH = 255;

    private static final Set<String> RESERVED_ATTRIBUTES = Set.of("eventType", "runId", "storyName", "batchIndex",
            "phase", "status");
    private static final Pattern ATTRIBUTE_NAME = Pattern.compile("[A-Za-z0-9:._/]+");

    private final NewRelicEventPublisher publisher;
    private final RunContext runContext;

    private final AtomicBoolean runFailed = new AtomicBoolean();
    private final ThreadLocal<Status> storyStatus = ThreadLocal.withInitial(() -> Status.PASSED);
    private final ConcurrentLinkedQueue<CompletableFuture<Void>> pendingStoryEvents = new ConcurrentLinkedQueue<>();
    private final boolean enabled;
    private final String runId;
    private final Duration publishTimeout;
    private final Map<String, String> customAttributes;

    public NewRelicSteps(NewRelicEventPublisher publisher, RunContext runContext, IPropertyParser propertyParser,
            StoryControls storyControls, boolean enabled, String runId, Duration publishTimeout)
    {
        this.publisher = publisher;
        this.runContext = runContext;
        this.enabled = enabled && !storyControls.dryRun();
        this.runId = StringUtils.isNotBlank(runId) ? runId : UUID.randomUUID().toString();
        this.publishTimeout = publishTimeout;
        this.customAttributes = propertyParser.getPropertyValuesByPrefix(ATTRIBUTE_PREFIX).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> isCustomAttribute(entry.getKey()) && entry.getValue() != null)
                .collect(toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @BeforeStories
    public void beforeStories()
    {
        if (enabled)
        {
            runFailed.set(false);
            postRunEvent(RunPhase.STARTED, null);
        }
    }

    @AfterStory(uponGivenStory = true)
    public void afterGivenStory()
    {
        if (enabled)
        {
            evaluateStoryStatus(runContext.getRunningStory());
        }
    }

    @AfterStory
    public void afterStory()
    {
        if (!enabled)
        {
            return;
        }
        RunningStory story = runContext.getRootRunningStory();
        if (!story.isNotExcluded())
        {
            return;
        }
        Status status = evaluateStoryStatus(story);
        storyStatus.remove();
        runFailed.compareAndSet(false, status == Status.FAILED);
        int batchIndex = BatchStorage.getBatchIndex(runContext.getRunningBatchKey());
        VividusStoryEvent event = new VividusStoryEvent(runId, story.getName(), batchIndex, status,
                customAttributes);
        pendingStoryEvents.add(CompletableFuture.runAsync(() -> publisher.postEvent(event)));
    }

    @AfterStories(order = Integer.MAX_VALUE)
    public void afterStories()
    {
        if (!enabled)
        {
            return;
        }
        awaitStoryEvents();
        postRunEvent(RunPhase.FINISHED, runFailed.get() ? Status.FAILED : Status.PASSED);
    }

    private Status evaluateStoryStatus(RunningStory story)
    {
        if (story.isFailed())
        {
            storyStatus.set(Status.FAILED);
        }
        return storyStatus.get();
    }

    private void postRunEvent(RunPhase phase, Status status)
    {
        publisher.postEvent(new VividusRunEvent(runId, phase, status, customAttributes));
    }

    private void awaitStoryEvents()
    {
        List<CompletableFuture<Void>> storyEvents = new ArrayList<>();
        CompletableFuture<Void> storyEvent = pendingStoryEvents.poll();
        while (storyEvent != null)
        {
            storyEvents.add(storyEvent);
            storyEvent = pendingStoryEvents.poll();
        }
        if (storyEvents.isEmpty())
        {
            return;
        }
        try
        {
            CompletableFuture.allOf(storyEvents.toArray(CompletableFuture[]::new))
                    .get(publishTimeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException e)
        {
            LOGGER.info("Timed out after {} waiting for New Relic story events", publishTimeout, e);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            LOGGER.info("Interrupted while waiting for New Relic story events", e);
        }
        catch (ExecutionException e)
        {
            LOGGER.info("Unable to send New Relic story events", e);
        }
    }

    private static boolean isCustomAttribute(String name)
    {
        if (RESERVED_ATTRIBUTES.contains(name))
        {
            LOGGER.warn("Skipping New Relic custom attribute '{}' because its name is reserved", name);
            return false;
        }
        if (name.length() > MAX_ATTRIBUTE_NAME_LENGTH || !ATTRIBUTE_NAME.matcher(name).matches())
        {
            LOGGER.warn("Skipping New Relic custom attribute '{}' because its name violates New Relic attribute rules",
                    name);
            return false;
        }
        return true;
    }
}
