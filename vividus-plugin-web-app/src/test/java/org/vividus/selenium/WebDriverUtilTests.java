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

package org.vividus.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.selenium.driver.TextFormattingWebDriver;
import org.vividus.selenium.element.TextFormattingWebElement;

import io.appium.java_client.FindsByAndroidUIAutomator;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidElement;

class WebDriverUtilTests
{
    @Test
    void testWrappedWebDriverUnwrap()
    {
        RemoteWebDriver remoteWebDriver = mock(RemoteWebDriver.class);
        FindsById actual = WebDriverUtil.unwrap(new TextFormattingWebDriver(remoteWebDriver), FindsById.class);
        assertEquals(remoteWebDriver, actual);
    }

    @Test
    void testNonWrappedWebDriverUnwrap()
    {
        RemoteWebDriver remoteWebDriver = mock(RemoteWebDriver.class);
        FindsById actual = WebDriverUtil.unwrap(remoteWebDriver, FindsById.class);
        assertEquals(remoteWebDriver, actual);
    }

    @Test
    void testWrappedWebDriverUnwrapButNoCast()
    {
        assertThrows(ClassCastException.class,
            () -> WebDriverUtil.unwrap(new TextFormattingWebDriver(mock(WebDriver.class)), FindsById.class));
    }

    @Test
    void testWrappedWebElementUnwrap()
    {
        RemoteWebElement remoteWebElement = mock(RemoteWebElement.class);
        WrapsDriver actual = WebDriverUtil.unwrap(new TextFormattingWebElement(remoteWebElement), WrapsDriver.class);
        assertEquals(remoteWebElement, actual);
    }

    @Test
    void testWrappedWebElementWithNewInterfaceUnwrap()
    {
        AndroidElement androidElement = mock(AndroidElement.class);
        FindsByAndroidUIAutomator<?> actual = WebDriverUtil.unwrap(new TestWebElement(androidElement),
                FindsByAndroidUIAutomator.class);
        assertEquals(androidElement, actual);
    }

    @Test
    void testNonWrappedWebElementUnwrap()
    {
        RemoteWebElement remoteWebElement = mock(RemoteWebElement.class);
        WrapsDriver actual = WebDriverUtil.unwrap(remoteWebElement, WrapsDriver.class);
        assertEquals(remoteWebElement, actual);
    }

    @Test
    void testWrappedWebElementUnwrapButNoCast()
    {
        assertThrows(ClassCastException.class,
            () -> WebDriverUtil.unwrap(new TextFormattingWebElement(mock(WebElement.class)), WrapsDriver.class));
    }

    @SuppressWarnings("unchecked")
    static class TestWebElement extends MobileElement implements WrapsElement
    {
        private final WebElement wrappedElement;

        TestWebElement(WebElement wrappedElement)
        {
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
