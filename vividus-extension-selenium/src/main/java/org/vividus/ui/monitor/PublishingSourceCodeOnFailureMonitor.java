/*
 * Copyright 2019-2025 the original author or authors.
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

import java.lang.reflect.Method;
import java.util.Map;

import org.vividus.context.RunContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.ContextSourceCodeProvider;

public class PublishingSourceCodeOnFailureMonitor extends AbstractPublishingAttachmentOnFailureMonitor
{
    private final boolean publishSourceOnFailure;
    private final String sourceCodeAttachmentFormat;
    private final ContextSourceCodeProvider contextSourceCodeProvider;

    public PublishingSourceCodeOnFailureMonitor(boolean publishSourceOnFailure, String sourceCodeAttachmentFormat,
            ContextSourceCodeProvider contextSourceCodeProvider, RunContext runContext,
            IWebDriverProvider webDriverProvider, IAttachmentPublisher attachmentPublisher)
    {
        super(runContext, webDriverProvider, attachmentPublisher, "noSourceCodeOnFailure",
                "Unable to capture Source Code");
        this.publishSourceOnFailure = publishSourceOnFailure;
        this.sourceCodeAttachmentFormat = sourceCodeAttachmentFormat;
        this.contextSourceCodeProvider = contextSourceCodeProvider;
    }

    @Override
    protected boolean isPublishingEnabled(Method method)
    {
        return publishSourceOnFailure;
    }

    @Override
    protected void publishAttachment()
    {
        contextSourceCodeProvider.getSourceCode()
                .ifPresent(source -> getAttachmentPublisher().publishAttachment("/templates/source-code.ftl",
                        Map.of("sourceCode", source, "format", sourceCodeAttachmentFormat),
                        "Application source code." + sourceCodeAttachmentFormat));
    }
}
