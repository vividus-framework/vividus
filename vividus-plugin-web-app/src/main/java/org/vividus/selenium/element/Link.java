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

import org.openqa.selenium.WebElement;

public class Link extends WebElementWrapper implements IHasUrl
{
    public Link(WebElement wrappedElement)
    {
        super(wrappedElement);
    }

    @Override
    public String getUrl()
    {
        return getWrappedElement().getAttribute("href");
    }

    public String getTarget()
    {
        return getWrappedElement().getAttribute("target");
    }

    public String getText()
    {
        return getWrappedElement().getText();
    }
}
