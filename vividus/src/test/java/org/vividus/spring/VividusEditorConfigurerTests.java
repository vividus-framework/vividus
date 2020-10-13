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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.PropertyEditor;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.propertyeditors.PathEditor;

class VividusEditorConfigurerTests
{
    @Test
    void shouldCollectPropertyEditors()
    {
        ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
        String propertyEditorsBeanName = "propertyEditors-UnitTests";
        when(beanFactory.getBeanNamesForType(Map.class)).thenReturn(new String[] { "any", propertyEditorsBeanName });
        Class<Path> propertyEditorClass = Path.class;
        PropertyEditor propertyEditor = new PathEditor();
        Map<Class<?>, ? extends PropertyEditor> propertyEditors = Map.of(propertyEditorClass, propertyEditor);
        when(beanFactory.getBean(propertyEditorsBeanName, Map.class)).thenReturn(propertyEditors);

        PropertyEditorRegistry registry = mock(PropertyEditorRegistry.class);
        doNothing().when(beanFactory).addPropertyEditorRegistrar(argThat(registrar -> {
            registrar.registerCustomEditors(registry);
            return true;
        }));

        VividusEditorConfigurer vividusEditorConfigurer = new VividusEditorConfigurer();
        vividusEditorConfigurer.postProcessBeanFactory(beanFactory);
        verify(registry).registerCustomEditor(propertyEditorClass, propertyEditor);
    }
}
