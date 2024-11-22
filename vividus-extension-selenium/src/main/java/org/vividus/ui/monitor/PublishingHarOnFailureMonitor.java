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

package org.vividus.ui.monitor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Optional;

import com.browserup.harreader.filter.HarLogFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;

import org.vividus.context.RunContext;
import org.vividus.proxy.IProxy;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.testcontext.TestContext;

import de.sstoehr.harreader.HarWriter;
import de.sstoehr.harreader.HarWriterException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarLog;

public class PublishingHarOnFailureMonitor extends AbstractPublishingAttachmentOnFailureMonitor
{
    private static final Object RECENT_HAR_ENTRY_TIMESTAMP_KEY = PublishingHarOnFailureMonitor.class;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final TestContext testContext;
    private final IProxy proxy;

    private final boolean publishHarOnFailure;

    public PublishingHarOnFailureMonitor(boolean publishHarOnFailure, EventBus eventBus, RunContext runContext,
            IWebDriverProvider webDriverProvider, IProxy proxy, TestContext testContext)
    {
        super(runContext, webDriverProvider, eventBus, "noHarOnFailure", "Unable to capture HAR");
        this.publishHarOnFailure = publishHarOnFailure;
        this.proxy = proxy;
        this.testContext = testContext;
    }

    @Override
    protected Optional<Attachment> createAttachment() throws IOException
    {
        try
        {
            HarWriter writer = new HarWriter();
            byte [] harBytes = writer.writeAsBytes(proxy.getRecordedData());
            Har har = OBJECT_MAPPER.readValue(harBytes, Har.class);
            HarLog harLog = har.getLog();
            Date savedDateTime = testContext.get(RECENT_HAR_ENTRY_TIMESTAMP_KEY);
            if (savedDateTime != null)
            {
                harLog.getEntries().removeIf(entry -> savedDateTime.after(entry.getStartedDateTime()));
                harLog.getPages().removeIf(page -> savedDateTime.after(page.getStartedDateTime()));
            }
            HarLogFilter.findMostRecentEntry(harLog)
                    .map(HarEntry::getStartedDateTime)
                    .ifPresent(date -> testContext.put(RECENT_HAR_ENTRY_TIMESTAMP_KEY, date));
            return Optional.of(new Attachment(writer.writeAsBytes(har), "har-on-failure.har"));
        }
        catch (HarWriterException thrown)
        {
            throw new IllegalStateException(thrown);
        }
    }

    @Override
    protected boolean isPublishingEnabled(Method method)
    {
        return publishHarOnFailure || getAnnotation(method, CaptureHarOnFailure.class).isPresent();
    }
}
