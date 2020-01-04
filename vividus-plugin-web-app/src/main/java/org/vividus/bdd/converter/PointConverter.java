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

package org.vividus.bdd.converter;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.openqa.selenium.Point;

@Named
public class PointConverter extends AbstractParameterConverter<Point>
{
    private static final Pattern POINT_PATTERN = Pattern.compile("\\((-?\\d+),\\s*(-?\\d+)\\)");

    @Override
    public Point convertValue(String value, Type type)
    {
        Matcher matcher = POINT_PATTERN.matcher(value);
        if (matcher.matches())
        {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            return new Point(x, y);
        }
        throw new IllegalArgumentException("Unable to parse point: " + value + ". It should match regular experession: "
                + POINT_PATTERN.pattern());
    }
}
