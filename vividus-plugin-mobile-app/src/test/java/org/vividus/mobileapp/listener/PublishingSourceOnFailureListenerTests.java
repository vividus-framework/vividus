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

package org.vividus.mobileapp.listener;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.event.AssertionFailedEvent;

@ExtendWith(MockitoExtension.class)
class PublishingSourceOnFailureListenerTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private EventBus eventBus;
    @Mock private AssertionFailedEvent failedEvent;
    @InjectMocks private PublishingSourceOnFailureListener listener;

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(webDriverProvider, eventBus, failedEvent);
    }

    @Test
    void shouldPublishApplicationSource()
    {
        WebDriver driver = mock(WebDriver.class);

        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getPageSource()).thenReturn("<beans />");

        listener.publishApplicationSource(failedEvent);

        verify(eventBus).post(argThat(arg ->
        {
            AttachmentPublishEvent publishEvent = (AttachmentPublishEvent) arg;
            Attachment attachment = publishEvent.getAttachment();
            return "ApplicationSource".equals(attachment.getTitle())
                    && Arrays.equals(new byte[] { 60, 98, 101, 97, 110, 115, 32, 47, 62 }, attachment.getContent());
        }));
    }

    @Test
    void shouldNotPublishApplicationSourceIfWebDriverIsNotInitialized()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(false);
        listener.publishApplicationSource(failedEvent);
    }
}
