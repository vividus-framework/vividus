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

import java.util.Optional;

import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.listener.AbstractSourceCodePublishingOnFailureListener;

public class MobileSourceCodePublishingOnFailureListener extends AbstractSourceCodePublishingOnFailureListener
{
    public MobileSourceCodePublishingOnFailureListener(IAttachmentPublisher attachmentPublisher,
            IWebDriverProvider webDriverProvider)
    {
        super(attachmentPublisher, webDriverProvider, "XML");
    }

    @Override
    protected Optional<String> getSourceCode()
    {
        return Optional.ofNullable(getWebDriverProvider().get().getPageSource());
    }
}
