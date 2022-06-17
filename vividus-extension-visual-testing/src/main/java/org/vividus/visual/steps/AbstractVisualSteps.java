/*
 * Copyright 2019-2022 the original author or authors.
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
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotPrecondtionMismatchException;
import org.vividus.visual.model.AbstractVisualCheck;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheckResult;

public abstract class AbstractVisualSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractVisualSteps.class);

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

    protected <T extends AbstractVisualCheck> void execute(Supplier<T> visualCheckFactory,
            Function<T, VisualCheckResult> checkResultProvider, String templateName)
    {
        uiContext.getOptionalSearchContext().ifPresent(searchContext ->
        {
            try
            {
                T visualCheck = visualCheckFactory.get();
                visualCheck.setSearchContext(searchContext);

                VisualCheckResult result = checkResultProvider.apply(visualCheck);

                if (null != result)
                {
                    attachmentPublisher.publishAttachment(templateName, Map.of("result", result), "Visual comparison");
                    verifyResult(result);
                }
            }
            catch (ScreenshotPrecondtionMismatchException e)
            {
                softAssert.recordFailedAssertion(e);
            }
        });
    }

    protected void verifyResult(VisualCheckResult result)
    {
        boolean passed = result.isPassed() ^ result.getActionType() == VisualActionType.CHECK_INEQUALITY_AGAINST;
        softAssert.assertTrue("Visual check passed", passed);
    }

    protected void patchIgnores(String sourceKey, ScreenshotConfiguration screenshotConfiguration,
            Map<IgnoreStrategy, Set<Locator>> ignores)
    {
        Set<Locator> elementsToIgnore = getIgnoresFromOneOf(screenshotConfiguration.getElementsToIgnore(), sourceKey,
                ignores.get(IgnoreStrategy.ELEMENT));
        screenshotConfiguration.setElementsToIgnore(elementsToIgnore);

        Set<Locator> areasToIgnore = getIgnoresFromOneOf(screenshotConfiguration.getAreasToIgnore(), sourceKey,
                ignores.get(IgnoreStrategy.AREA));
        screenshotConfiguration.setAreasToIgnore(areasToIgnore);
    }

    private Set<Locator> getIgnoresFromOneOf(Set<Locator> configIgnores, String sourceKey, Set<Locator> source)
    {
        if (!source.isEmpty())
        {
            Validate.isTrue(configIgnores.isEmpty(), "The elements and areas to ignore must be passed "
                    + "either through screenshot configuration or %s", sourceKey);
            LOGGER.atWarn().addArgument(sourceKey).log("The passing of elements and areas to ignore through {}"
                    + " is deprecated, please use screenshot configuration instead");
            return source;
        }
        return configIgnores;
    }

    protected ISoftAssert getSoftAssert()
    {
        return softAssert;
    }
}
