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

package org.vividus.reportportal.listener;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;

import rp.com.google.common.io.BaseEncoding;

public class AttachmentListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentListener.class);

    @Subscribe
    @AllowConcurrentEvents
    public void onAttachmentPublish(AttachmentPublishEvent event)
    {
        Attachment attachment = event.getAttachment();
        LOGGER.atInfo().addArgument(() -> BaseEncoding.base64().encode(attachment.getContent()))
                       .addArgument(attachment::getTitle)
                       .log("RP_MESSAGE#BASE64#{}#{}");
    }
}
