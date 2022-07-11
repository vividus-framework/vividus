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

package org.vividus.spring;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jbehave.core.reporters.Format;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.ReflectionUtils;

public class StringToFormatConverter implements Converter<String, Format>
{
    @Override
    public Format convert(String source)
    {
        Field field = ReflectionUtils.findField(Format.class, source);
        if (null != field)
        {
            return (Format) ReflectionUtils.getField(field, null);
        }
        throw new IllegalArgumentException("Unsupported format: " + source + ". List of supported formats: "
                + getSupportedFormats());
    }

    private List<String> getSupportedFormats()
    {
        return FieldUtils.getAllFieldsList(Format.class).stream().filter(ReflectionUtils::isPublicStaticFinal)
                .map(Field::getName).collect(Collectors.toList());
    }
}
