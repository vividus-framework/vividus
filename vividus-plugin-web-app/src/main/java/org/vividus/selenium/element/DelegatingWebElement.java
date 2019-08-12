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
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Coordinates;
import org.openqa.selenium.interactions.Locatable;

public class DelegatingWebElement implements WebElement, WrapsElement, Locatable
{
    private final WebElement wrappedElement;

    public DelegatingWebElement(WebElement webElement)
    {
        wrappedElement = webElement;
    }

    @Override
    public void click()
    {
        wrappedElement.click();
    }

    @Override
    public void submit()
    {
        wrappedElement.submit();
    }

    @Override
    public void sendKeys(CharSequence... keysToSend)
    {
        wrappedElement.sendKeys(keysToSend);
    }

    @Override
    public void clear()
    {
        wrappedElement.clear();
    }

    @Override
    public String getTagName()
    {
        return wrappedElement.getTagName();
    }

    @Override
    public String getAttribute(String name)
    {
        return wrappedElement.getAttribute(name);
    }

    @Override
    public boolean isSelected()
    {
        return wrappedElement.isSelected();
    }

    @Override
    public boolean isEnabled()
    {
        return wrappedElement.isEnabled();
    }

    @Override
    public String getText()
    {
        return wrappedElement.getText();
    }

    @Override
    public List<WebElement> findElements(By by)
    {
        return wrappedElement.findElements(by);
    }

    @Override
    public WebElement findElement(By by)
    {
        return wrappedElement.findElement(by);
    }

    @Override
    public boolean isDisplayed()
    {
        return wrappedElement.isDisplayed();
    }

    @Override
    public Point getLocation()
    {
        return wrappedElement.getLocation();
    }

    @Override
    public Dimension getSize()
    {
        return wrappedElement.getSize();
    }

    @Override
    public String getCssValue(String propertyName)
    {
        return wrappedElement.getCssValue(propertyName);
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException
    {
        return wrappedElement.getScreenshotAs(target);
    }

    @Override
    public Coordinates getCoordinates()
    {
        return ((Locatable) wrappedElement).getCoordinates();
    }

    @Override
    public WebElement getWrappedElement()
    {
        return wrappedElement;
    }

    @Override
    public Rectangle getRect()
    {
        return wrappedElement.getRect();
    }

    @Override
    public String toString()
    {
        return String.valueOf(wrappedElement);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof WebElement))
        {
            return false;
        }

        WebElement that = (WebElement) obj;
        if (that instanceof WrapsElement)
        {
            that = ((WrapsElement) that).getWrappedElement();
        }

        return wrappedElement.equals(that);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(wrappedElement);
    }
}
