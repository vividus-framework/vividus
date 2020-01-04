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

package org.vividus.selenium.element;

import java.util.Objects;

import org.openqa.selenium.WebElement;

public class Checkbox extends DelegatingWebElement
{
    private WebElement labelElement;

    public Checkbox(WebElement wrappedElement)
    {
        super(wrappedElement);
    }

    public Checkbox(WebElement wrappedElement, WebElement labelElement)
    {
        this(wrappedElement);
        this.labelElement = labelElement;
    }

    public WebElement getLabelElement()
    {
        return labelElement;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }
        Checkbox checkbox = (Checkbox) o;
        return Objects.equals(labelElement, checkbox.labelElement);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), labelElement);
    }
}
