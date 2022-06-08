/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.ui.action.search;

import java.beans.ConstructorProperties;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class LocatorPattern
{
    private static final Pattern PARAMS = Pattern.compile("%\\d+\\$s");

    private String locatorType;
    private String pattern;
    private int parametersQuantity;

    @ConstructorProperties({"locator-type", "pattern"})
    public LocatorPattern(String locatorType, String pattern)
    {
        this.locatorType = locatorType;
        this.pattern = pattern;
        calculateParametersQuantity();
    }

    public String getLocatorType()
    {
        return locatorType;
    }

    public String getPattern()
    {
        return pattern;
    }

    private void calculateParametersQuantity()
    {
        int references = PARAMS.matcher(this.pattern)
                               .results()
                               .map(MatchResult::group)
                               .map(r -> StringUtils.substringBetween(r, "%", "$s"))
                               .mapToInt(Integer::parseInt)
                               .max()
                               .orElse(0);
        int placeholders = StringUtils.countMatches(pattern, "%s");
        this.parametersQuantity = Math.max(references, placeholders);
    }

    public int getParametersQuantity()
    {
        return parametersQuantity;
    }
}
