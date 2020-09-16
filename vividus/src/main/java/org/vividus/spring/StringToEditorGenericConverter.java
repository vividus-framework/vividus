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

package org.vividus.spring;

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.ReflectionUtils;

public class StringToEditorGenericConverter implements GenericConverter
{
    private PropertyEditorRegistrySupport propertyEditorRegistry;
    private Map<Class<?>, PropertyEditor> defaultEditors;

    @SuppressWarnings("unchecked")
    public void init()
    {
        Field findField = ReflectionUtils.findField(PropertyEditorRegistrySupport.class, "defaultEditors");
        findField.setAccessible(true);
        defaultEditors = (Map<Class<?>, PropertyEditor>) ReflectionUtils.getField(findField, propertyEditorRegistry);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes()
    {
        return defaultEditors.keySet()
                             .stream()
                             .filter(k -> !Collection.class.isAssignableFrom(k))
                             .map(k -> new ConvertiblePair(String.class, k))
                             .collect(Collectors.toSet());
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType)
    {
        return convert(targetType.getObjectType(), (String) source);
    }

    private Object convert(Class<?> clazz, String toConvert)
    {
        PropertyEditor propertyEditor = defaultEditors.get(clazz);
        propertyEditor.setAsText(toConvert);
        return propertyEditor.getValue();
    }

    public void setPropertyEditorRegistry(PropertyEditorRegistrySupport propertyEditorRegistry)
    {
        this.propertyEditorRegistry = propertyEditorRegistry;
    }
}
