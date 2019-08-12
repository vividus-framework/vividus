/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.selenium.element;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.TextUtils;

public class TextFormattingWebElement extends DelegatingWebElement
{
    public TextFormattingWebElement(WebElement webElement)
    {
        super(webElement);
    }

    @Override
    public String getText()
    {
        return TextUtils.normalizeText(super.getText());
    }

    @Override
    public String getAttribute(String name)
    {
        return TextUtils.normalizeText(super.getAttribute(name));
    }

    @Override
    public List<WebElement> findElements(By by)
    {
        return super.findElements(by).stream().map(TextFormattingWebElement::new).collect(Collectors.toList());
    }

    @Override
    public WebElement findElement(By by)
    {
        return new TextFormattingWebElement(super.findElement(by));
    }
}
