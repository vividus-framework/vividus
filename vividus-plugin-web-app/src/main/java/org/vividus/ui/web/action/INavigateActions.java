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

package org.vividus.ui.web.action;

import java.net.URI;

import org.openqa.selenium.WebDriver;

public interface INavigateActions
{
    void navigateTo(String url);

    void navigateTo(URI url);

    void refresh();

    void refresh(WebDriver webDriver);

    void back();

    /**
     * Checks if the previous and current page URLs are different. Performs standard navigation back and checks if the
     * URL value has changed. If not, navigating to default URL is performed, but the failed assertion is recorded.
     * Applicable if navigation back function do not work on site.
     * @param previousPageUrl The string which defines the previous page URL
     */
    void back(String previousPageUrl);
}
