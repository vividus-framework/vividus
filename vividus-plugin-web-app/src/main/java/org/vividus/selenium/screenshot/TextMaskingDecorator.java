/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.selenium.screenshot;

import java.awt.image.BufferedImage;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.ui.web.action.WebJavascriptActions;

import pazone.ashot.ShootingStrategy;

public final class TextMaskingDecorator extends ScreenshotShootingDecorator
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TextMaskingDecorator.class);

    private static final long serialVersionUID = -6619575662547219485L;
    private static final char REPLACEMENT_CHAR = '*';

    private final String textToMask;
    private final String replacement;

    private final transient WebJavascriptActions webJavascriptActions;

    public TextMaskingDecorator(ShootingStrategy shootingStrategy, String textToMask,
            WebJavascriptActions webJavascriptActions)
    {
        super(shootingStrategy);
        this.textToMask = textToMask;
        this.replacement = StringUtils.repeat(REPLACEMENT_CHAR, textToMask.length());
        this.webJavascriptActions = webJavascriptActions;
    }

    @Override
    public BufferedImage perform(Supplier<BufferedImage> bufferedImageSupplier)
    {
        boolean masked = maskText(textToMask, replacement);
        try
        {
            return bufferedImageSupplier.get();
        }
        finally
        {
            if (masked)
            {
                maskText(replacement, textToMask);
            }
        }
    }

    private boolean maskText(String target, String replacement)
    {
        try
        {
            webJavascriptActions.executeScriptFromResource(getClass(), "mask-text.js", target, replacement);
            return true;
        }
        catch (WebDriverException exception)
        {
            LOGGER.atError().setCause(exception).log("Failed to mask '{}' text with '{}' replacement", target,
                    replacement);
            return false;
        }
    }
}
