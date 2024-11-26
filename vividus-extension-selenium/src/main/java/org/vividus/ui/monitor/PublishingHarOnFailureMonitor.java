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
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

import com.browserup.harreader.filter.HarLogFilter;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Suppliers;
import com.google.common.eventbus.EventBus;

import org.vividus.context.RunContext;
import org.vividus.proxy.IProxy;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.testcontext.TestContext;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.HarReaderMode;
import de.sstoehr.harreader.HarWriter;
import de.sstoehr.harreader.HarWriterException;
import de.sstoehr.harreader.jackson.MapperFactory;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarLog;

public class PublishingHarOnFailureMonitor extends AbstractPublishingAttachmentOnFailureMonitor
{
    private static final Object RECENT_HAR_ENTRY_TIMESTAMP_KEY = PublishingHarOnFailureMonitor.class;

    private final TestContext testContext;
    private final IProxy proxy;

    private final boolean publishHarOnFailure;

    private final MapperFactory mapperFactory;

    public PublishingHarOnFailureMonitor(boolean publishHarOnFailure, EventBus eventBus, RunContext runContext,
            IWebDriverProvider webDriverProvider, IProxy proxy, TestContext testContext)
    {
        super(runContext, webDriverProvider, eventBus, "noHarOnFailure", "Unable to capture HAR");
        this.publishHarOnFailure = publishHarOnFailure;
        this.proxy = proxy;
        this.testContext = testContext;
        this.mapperFactory = new MapperFactory()
        {
            /*
             * Increase the limit for allowed HAR size in string format to avoid the following exception:
             *
             * <code>
             * Caused by: com.fasterxml.jackson.core.exc.StreamConstraintsException: String value length
             * (20054016) exceeds the maximum allowed (20000000, from `StreamReadConstraints.getMaxStringLength()`)
             * </code>
             */
            private static final int ALLOWED_HAR_SIZE = 40_000_000;

            private final Supplier<ObjectMapper> objectMapper = Suppliers.memoize(() ->
            {
                return new ObjectMapper(
                        new JsonFactoryBuilder()
                                .streamReadConstraints(
                                        StreamReadConstraints.builder().maxStringLength(ALLOWED_HAR_SIZE).build())
                                .build());
            });

            @Override
            public ObjectMapper instance(HarReaderMode mode)
            {
                return instance();
            }

            @Override
            public ObjectMapper instance()
            {
                return objectMapper.get();
            }
        };
    }

    @Override
    protected Optional<Attachment> createAttachment() throws IOException
    {
        try (StringWriter writer = new StringWriter())
        {
            HarWriter harWriter = new HarWriter(mapperFactory);
            harWriter.writeTo(writer, proxy.getRecordedData());
            HarReader reader = new HarReader(mapperFactory);
            Har har = reader.readFromString(writer.toString());
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
            return Optional.of(new Attachment(harWriter.writeAsBytes(har), "har-on-failure.har"));
        }
        catch (HarWriterException | HarReaderException thrown)
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
