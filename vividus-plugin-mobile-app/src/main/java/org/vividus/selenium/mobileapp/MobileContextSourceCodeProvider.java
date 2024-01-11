/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.selenium.mobileapp;

import java.util.Map;
import java.util.Optional;

import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.ContextSourceCodeProvider;

public class MobileContextSourceCodeProvider implements ContextSourceCodeProvider
{
    private final IWebDriverProvider webDriverProvider;

    public MobileContextSourceCodeProvider(IWebDriverProvider webDriverProvider)
    {
        this.webDriverProvider = webDriverProvider;
    }

    @Override
    public Map<String, String> getSourceCode()
    {
        return Optional.ofNullable(webDriverProvider.get().getPageSource())
                       .map(sc -> Map.of(APPLICATION_SOURCE_CODE, sc))
                       .orElseGet(Map::of);
    }
}
