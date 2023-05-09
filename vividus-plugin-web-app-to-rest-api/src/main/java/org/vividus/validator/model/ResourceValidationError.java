/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.validator.model;

import java.util.function.Consumer;

import org.vividus.http.validation.model.CheckStatus;

public enum ResourceValidationError
{
    RESPONSE_IS_EMPTY("Unable to get page with URL: %s; Response is received without body;"),
    UNREACHABLE_PAGE("Unable to get page with URL: %s"),
    MAIN_PAGE_IS_NOT_SET("Unable to resolve %s page since the main application page URL is not set"),
    INVALID_PAGE_URL("Invalid page URL: %s"),
    EMPTY_HREF_SRC("Element doesn't contain href/src attributes",
        "Element by selector %s doesn't contain href/src attributes"),
    INVALID_HREF_SRC("Element has href/src attribute with invalid URL: %s",
        "Element by selector %s has href/src attribute with invalid URL: %s");

    private final String errorFormat;
    private final String assertionFormat;

    ResourceValidationError(String errorFormat)
    {
        this(errorFormat, errorFormat);
    }

    ResourceValidationError(String errorFormat, String assertionFormat)
    {
        this.errorFormat = errorFormat;
        this.assertionFormat = assertionFormat;
    }

    public WebPageResourceValidation createValidation(String pageUrl, String cssSelector, Object... params)
    {
        WebPageResourceValidation resourceValidation = new WebPageResourceValidation(
                String.format(errorFormat, params));
        resourceValidation.setCheckStatus(CheckStatus.BROKEN);
        if (pageUrl != null)
        {
            resourceValidation.setPageURL(pageUrl);
        }
        if (cssSelector != null)
        {
            resourceValidation.setCssSelector(cssSelector);
        }
        return resourceValidation;
    }

    public ResourceValidationError onAssertion(Consumer<String> asserConsumer, Object... params)
    {
        String assertionMessage = String.format(assertionFormat, params);
        asserConsumer.accept(assertionMessage);
        return this;
    }
}
