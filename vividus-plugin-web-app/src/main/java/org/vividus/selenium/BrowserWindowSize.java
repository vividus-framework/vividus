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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Dimension;

public class BrowserWindowSize
{
    private final int width;
    private final int height;
    private final String sizeAsString;

    public BrowserWindowSize(String sizeAsString)
    {
        Matcher matcher = Pattern.compile("(\\d+)x(\\d+)").matcher(sizeAsString);
        if (matcher.matches())
        {
            width = Integer.parseInt(matcher.group(1));
            height = Integer.parseInt(matcher.group(2));
        }
        else
        {
            throw new IllegalStateException("Provided size = " + sizeAsString
                    + " has wrong format. Example of correct format: 800x600");
        }
        this.sizeAsString = sizeAsString;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public Dimension toDimension()
    {
        return new Dimension(width, height);
    }

    @Override
    public String toString()
    {
        return sizeAsString;
    }
}
