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

package org.vividus.ui.web.listener;

import java.util.Optional;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.listener.AbstractSourceCodePublishingOnFailureListener;

public class WebSourceCodePublishingOnFailureListener extends AbstractSourceCodePublishingOnFailureListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSourceCodePublishingOnFailureListener.class);

    private final IUiContext uiContext;

    protected WebSourceCodePublishingOnFailureListener(IAttachmentPublisher attachmentPublisher, IWebDriverProvider
             webDriverProvider, IUiContext uiContext)
    {
        super(attachmentPublisher, webDriverProvider, "HTML");
        this.uiContext = uiContext;
    }

    @Override
    protected Optional<String> getSourceCode()
    {
        SearchContext searchContext = uiContext.getSearchContext();
        String sourceCode = null;
        if (searchContext instanceof WebElement)
        {
            sourceCode = getElementSource(searchContext);
        }
        else if (searchContext instanceof WebDriver)
        {
            sourceCode = ((WebDriver) searchContext).getPageSource();
        }
        return Optional.ofNullable(sourceCode);
    }

    private String getElementSource(SearchContext searchContext)
    {
        try
        {
            return ((WebElement) searchContext).getAttribute("innerHTML");
        }
        catch (StaleElementReferenceException exception)
        {
            LOGGER.debug("Unable to get sources of the stale element");
            return null;
        }
    }
}
