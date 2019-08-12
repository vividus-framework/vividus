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

package org.vividus.bdd.steps.ui.web.validation;

import java.util.List;

import javax.inject.Inject;

import org.hamcrest.Matcher;
import org.openqa.selenium.WebElement;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.ui.web.context.IWebUiContext;

public class HighlightingSoftAssert implements IHighlightingSoftAssert
{
    @Inject private IDescriptiveSoftAssert descriptiveSoftAssert;
    @Inject private IWebUiContext webUiContext;

    @Override
    public IDescriptiveSoftAssert withHighlightedElement(WebElement element)
    {
        webUiContext.putAssertingWebElements(List.of(element));
        return this;
    }

    @Override
    public IDescriptiveSoftAssert withHighlightedElements(List<WebElement> elements)
    {
        webUiContext.putAssertingWebElements(elements);
        return this;
    }

    @Override
    public boolean assertTrue(String businessDescription, String systemDescription, boolean condition)
    {
        boolean result = descriptiveSoftAssert.assertTrue(businessDescription, systemDescription, condition);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public <T> boolean assertThat(String businessDescription, String systemDescription, T actual,
            Matcher<? super T> matcher)
    {
        boolean result = descriptiveSoftAssert.assertThat(businessDescription, systemDescription, actual,
                matcher);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean assertNotNull(String businessDescription, String systemDescription, Object object)
    {
        boolean result = descriptiveSoftAssert.assertNotNull(businessDescription, systemDescription, object);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean assertTrue(String description, boolean condition)
    {
        boolean result = descriptiveSoftAssert.assertTrue(description, condition);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean assertFalse(String description, boolean condition)
    {
        boolean result = descriptiveSoftAssert.assertFalse(description, condition);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean assertEquals(String description, Object expected, Object actual)
    {
        boolean result = descriptiveSoftAssert.assertEquals(description, expected, actual);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean assertNotEquals(String description, Object expected, Object actual)
    {
        boolean result = descriptiveSoftAssert.assertNotEquals(description, expected, actual);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean assertEquals(String description, double expected, double actual, double delta)
    {
        boolean result = descriptiveSoftAssert.assertEquals(description, expected, actual, delta);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean assertNotEquals(String description, double expected, double actual, double delta)
    {
        boolean result = descriptiveSoftAssert.assertNotEquals(description, expected, actual, delta);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean assertEquals(String description, long expected, long actual)
    {
        boolean result = descriptiveSoftAssert.assertEquals(description, expected, actual);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean assertNotEquals(String description, long expected, long actual)
    {
        boolean result = descriptiveSoftAssert.assertNotEquals(description, expected, actual);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean assertEquals(String description, boolean expected, boolean actual)
    {
        boolean result = descriptiveSoftAssert.assertEquals(description, expected, actual);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean assertNotEquals(String description, boolean expected, boolean actual)
    {
        boolean result = descriptiveSoftAssert.assertNotEquals(description, expected, actual);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean assertNotNull(String description, Object object)
    {
        boolean result = descriptiveSoftAssert.assertNotNull(description, object);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean assertNull(String description, Object object)
    {
        boolean result = descriptiveSoftAssert.assertNull(description, object);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public <T> boolean assertThat(String description, T actual, Matcher<? super T> matcher)
    {
        boolean result = descriptiveSoftAssert.assertThat(description, actual, matcher);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean recordPassedAssertion(String description)
    {
        boolean result = descriptiveSoftAssert.recordPassedAssertion(description);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean recordFailedAssertion(String description)
    {
        boolean result = descriptiveSoftAssert.recordFailedAssertion(description);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean recordFailedAssertion(Exception exception)
    {
        boolean result = descriptiveSoftAssert.recordFailedAssertion(exception);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public boolean recordFailedAssertion(String description, Exception exception)
    {
        boolean result = descriptiveSoftAssert.recordFailedAssertion(description, exception);
        clearAssertingWebElements();
        return result;
    }

    @Override
    public void verify() throws VerificationError
    {
        descriptiveSoftAssert.verify();
    }

    private void clearAssertingWebElements()
    {
        webUiContext.clearAssertingWebElements();
    }
}
