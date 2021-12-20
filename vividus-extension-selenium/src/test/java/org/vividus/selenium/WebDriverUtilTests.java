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

package org.vividus.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.virtualauthenticator.HasVirtualAuthenticator;
import org.vividus.selenium.driver.TextFormattingWebDriver;
import org.vividus.selenium.element.DelegatingWebElement;
import org.vividus.selenium.element.TextFormattingWebElement;

import io.appium.java_client.remote.SupportsContextSwitching;

class WebDriverUtilTests
{
    @Test
    void testWrappedWebDriverUnwrap()
    {
        var remoteWebDriver = mock(RemoteWebDriver.class);
        var wrappingDriver = new TextFormattingWebDriver(remoteWebDriver);
        var actual = WebDriverUtil.unwrap(wrappingDriver, HasVirtualAuthenticator.class);
        assertEquals(remoteWebDriver, actual);
    }

    @Test
    void testNonWrappedWebDriverUnwrap()
    {
        var remoteWebDriver = mock(RemoteWebDriver.class);
        var actual = WebDriverUtil.unwrap(remoteWebDriver, HasVirtualAuthenticator.class);
        assertEquals(remoteWebDriver, actual);
    }

    @Test
    void testWrappedWebDriverUnwrapButNoCast()
    {
        var webDriver = mock(WebDriver.class);
        var wrappingDriver = new TextFormattingWebDriver(webDriver);
        assertThrows(ClassCastException.class,
                () -> WebDriverUtil.unwrap(wrappingDriver, HasVirtualAuthenticator.class));
    }

    @Test
    void testWrappedWebElementUnwrap()
    {
        var remoteWebElement = mock(RemoteWebElement.class);
        var wrappingElement = new TextFormattingWebElement(remoteWebElement);
        var actual = WebDriverUtil.unwrap(wrappingElement, WrapsDriver.class);
        assertEquals(remoteWebElement, actual);
    }

    @Test
    void testWrappedWebElementWithNewInterfaceUnwrap()
    {
        var webElement = mock(SupportsContextSwitching.class, withSettings().extraInterfaces(WebElement.class));
        var actual = WebDriverUtil.unwrap(new TestWebElement((WebElement) webElement), SupportsContextSwitching.class);
        assertEquals(webElement, actual);
    }

    @Test
    void testNonWrappedWebElementUnwrap()
    {
        var remoteWebElement = mock(RemoteWebElement.class);
        var actual = WebDriverUtil.unwrap(remoteWebElement, WrapsDriver.class);
        assertEquals(remoteWebElement, actual);
    }

    @Test
    void testWrappedWebElementUnwrapButNoCast()
    {
        var webElement = mock(WebElement.class);
        var wrappingElement = new TextFormattingWebElement(webElement);
        assertThrows(ClassCastException.class, () -> WebDriverUtil.unwrap(wrappingElement, WrapsDriver.class));
    }

    @SuppressWarnings("unchecked")
    static class TestWebElement extends DelegatingWebElement implements WrapsElement
    {
        private final WebElement wrappedElement;

        TestWebElement(WebElement wrappedElement)
        {
            super(wrappedElement);
            this.wrappedElement = wrappedElement;
        }

        @Override
        public WebElement getWrappedElement()
        {
            return wrappedElement;
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
            TestWebElement that = (TestWebElement) o;
            return Objects.equals(wrappedElement, that.wrappedElement);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(super.hashCode(), wrappedElement);
        }
    }
}
