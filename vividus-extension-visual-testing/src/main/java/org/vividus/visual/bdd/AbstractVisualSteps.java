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

package org.vividus.visual.bdd;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;
import org.openqa.selenium.SearchContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.ui.context.IUiContext;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.model.VisualCheckResult;

public abstract class AbstractVisualSteps
{
    private final IUiContext uiContext;
    private final IAttachmentPublisher attachmentPublisher;

    public AbstractVisualSteps(IUiContext uiContext, IAttachmentPublisher attachmentPublisher)
    {
        this.uiContext = uiContext;
        this.attachmentPublisher = attachmentPublisher;
    }

    protected <T extends VisualCheck> VisualCheckResult
        execute(Function<T, VisualCheckResult> checkResultProvider, Supplier<T> visualCheckFactory, String templateName)
    {
        SearchContext searchContext = uiContext.getSearchContext();
        Validate.validState(searchContext != null, "Search context is null, please check is browser session started");
        T visualCheck = visualCheckFactory.get();
        visualCheck.setSearchContext(searchContext);
        VisualCheckResult result = checkResultProvider.apply(visualCheck);
        if (null != result)
        {
            attachmentPublisher.publishAttachment(templateName, Map.of("result", result), "Visual comparison");
        }
        return result;
    }
}
