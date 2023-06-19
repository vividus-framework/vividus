/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.visual.steps;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.openqa.selenium.SearchContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.screenshot.ScreenshotPrecondtionMismatchException;
import org.vividus.visual.model.AbstractVisualCheck;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheckResult;

public abstract class AbstractVisualSteps
{
    private final IUiContext uiContext;
    private final IAttachmentPublisher attachmentPublisher;
    private final ISoftAssert softAssert;

    protected AbstractVisualSteps(IUiContext uiContext, IAttachmentPublisher attachmentPublisher,
            ISoftAssert softAssert)
    {
        this.uiContext = uiContext;
        this.attachmentPublisher = attachmentPublisher;
        this.softAssert = softAssert;
    }

    protected <T extends AbstractVisualCheck, R extends VisualCheckResult> Optional<R>
            execute(Supplier<T> visualCheckFactory, Function<T, R> checkResultProvider)
    {
        Optional<SearchContext> searchContext = uiContext.getOptionalSearchContext();
        if (searchContext.isPresent())
        {
            try
            {
                return execute(visualCheckFactory, checkResultProvider, vc -> vc.setSearchContext(searchContext.get()));
            }
            catch (ScreenshotPrecondtionMismatchException e)
            {
                softAssert.recordFailedAssertion(e);
            }
        }

        return Optional.empty();
    }

    protected <T extends AbstractVisualCheck> void executeWithoutContext(Supplier<T> visualCheckFactory,
            Function<T, VisualCheckResult> checkResultProvider)
    {
        execute(visualCheckFactory, checkResultProvider, vc -> { });
    }

    private <T extends AbstractVisualCheck, R extends VisualCheckResult> Optional<R> execute(
            Supplier<T> visualCheckFactory, Function<T, R> checkResultProvider, Consumer<T> visualCheckConfigurer)
    {
        T visualCheck = visualCheckFactory.get();
        visualCheckConfigurer.accept(visualCheck);
        Optional<R> result = Optional.ofNullable(checkResultProvider.apply(visualCheck));

        result.ifPresent(r ->
        {
            String attachmentTitle = "Visual comparison: " + r.getBaselineName();
            attachmentPublisher.publishAttachment(getTemplateName(), Map.of("result", r), attachmentTitle);
            verifyResult(r);
        });

        return result;
    }

    protected void verifyResult(VisualCheckResult result)
    {
        boolean passed = result.isPassed() ^ result.getActionType() == VisualActionType.CHECK_INEQUALITY_AGAINST;
        softAssert.assertTrue("Visual check passed", passed);
    }

    protected abstract String getTemplateName();

    protected ISoftAssert getSoftAssert()
    {
        return softAssert;
    }
}
