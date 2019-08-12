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

import java.util.Objects;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;

public class WebElementWrapper implements WrapsElement
{
    private final WebElement wrappedElement;

    public WebElementWrapper(WebElement wrappedElement)
    {
        this.wrappedElement = wrappedElement;
    }

    @Override
    public WebElement getWrappedElement()
    {
        return wrappedElement;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(wrappedElement);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof WebElementWrapper))
        {
            return false;
        }
        WebElementWrapper other = (WebElementWrapper) obj;
        return Objects.equals(wrappedElement, other.wrappedElement);
    }

    @Override
    public String toString()
    {
        return String.valueOf(wrappedElement);
    }
}
