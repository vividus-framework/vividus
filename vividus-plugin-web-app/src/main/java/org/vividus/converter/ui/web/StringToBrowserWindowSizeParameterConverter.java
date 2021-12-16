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

package org.vividus.converter.ui.web;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.jbehave.core.steps.ParameterConverters.FunctionalParameterConverter;
import org.vividus.selenium.BrowserWindowSize;

@Named
public class StringToBrowserWindowSizeParameterConverter extends FunctionalParameterConverter<String, BrowserWindowSize>
{
    private static final Pattern WINDOW_SIZE_PATTERN = Pattern.compile("(\\d+)x(\\d+)");

    protected StringToBrowserWindowSizeParameterConverter()
    {
        super(size -> StringToBrowserWindowSizeParameterConverter.convert(size).get());
    }

    public static Optional<BrowserWindowSize> convert(String sizeAsString)
    {
        if (sizeAsString != null)
        {
            Matcher matcher = WINDOW_SIZE_PATTERN.matcher(sizeAsString);
            if (matcher.matches())
            {
                int width = Integer.parseInt(matcher.group(1));
                int height = Integer.parseInt(matcher.group(2));
                return Optional.of(new BrowserWindowSize(width, height));
            }
            else
            {
                throw new IllegalStateException("Provided size = " + sizeAsString
                        + " has wrong format. Example of correct format: 800x600");
            }
        }
        return Optional.empty();
    }
}
