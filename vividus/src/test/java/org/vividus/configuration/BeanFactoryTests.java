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

package org.vividus.configuration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@ExtendWith(MockitoExtension.class)
class BeanFactoryTests
{
    private static final String BEAN_NAME = "beanName";
    private static final Object DUMMY = new Object();

    @Mock
    private GenericXmlApplicationContext context;

    @BeforeEach
    void init() throws ReflectiveOperationException
    {
        String fieldName = "applicationContext";
        Field contextField = ReflectionUtils
                .findFields(BeanFactory.class, f -> f.getName().equals(fieldName), HierarchyTraversalMode.TOP_DOWN)
                .stream().findFirst().orElseThrow(() -> new NoSuchFieldException(fieldName));
        ReflectionUtils.makeAccessible(contextField);
        contextField.set(null, context);
    }

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(context);
    }

    @Test
    void testGetResourcePatternResolver()
    {
        assertEquals(context, BeanFactory.getResourcePatternResolver());
    }

    static Stream<Arguments> profiles()
    {
        return Stream.of(
                arguments(Map.of("spring.profiles.active", "web,mobile"), new String[] { "web", "mobile" }),
                arguments(Map.of(), new String[] {  })
        );
    }

    @ParameterizedTest
    @MethodSource("profiles")
    void testOpenNotActiveContext(Map<String, String> properties, String... activeProfiles)
    {
        try (var configurationResolverStaticMock = mockStatic(ConfigurationResolver.class))
        {
            var configurationResolver = mock(ConfigurationResolver.class);
            when(configurationResolver.getProperties()).thenReturn(MapUtils.toProperties(properties));
            configurationResolverStaticMock.when(ConfigurationResolver::getInstance).thenReturn(configurationResolver);
            var environment = mock(ConfigurableEnvironment.class);
            String[] locations = {
                "classpath*:/org/vividus/spring.xml",
                "classpath*:/vividus-extension/spring.xml",
                "classpath*:/vividus-plugin/spring.xml",
                "classpath*:/spring.xml"
            };

            when(context.isActive()).thenReturn(false);
            when(context.getEnvironment()).thenReturn(environment);

            BeanFactory.open();

            verify(environment).setActiveProfiles(activeProfiles);
            verify(context).load(locations);
            verify(context).refresh();
            verify(context).registerShutdownHook();
            verify(context).start();
        }
    }

    @Test
    void testOpenActiveContext()
    {
        when(context.isActive()).thenReturn(true);
        BeanFactory.open(new String[] {});
    }

    @Test
    void testGetBeanByName()
    {
        when(context.getBean(BEAN_NAME)).thenReturn(DUMMY);
        assertEquals(DUMMY, BeanFactory.getBean(BEAN_NAME));
    }

    @Test
    void testGetBeanByClass()
    {
        when(context.getBean(Object.class)).thenReturn(DUMMY);
        assertEquals(DUMMY, BeanFactory.getBean(Object.class));
    }

    @Test
    void testGetBeanByNameAndClass()
    {
        when(context.getBean(BEAN_NAME, Object.class)).thenReturn(DUMMY);
        assertEquals(DUMMY, BeanFactory.getBean(BEAN_NAME, Object.class));
    }

    @Test
    void testGetBeansOfType()
    {
        when(context.getBeansOfType(Object.class)).thenReturn(Map.of(BEAN_NAME, DUMMY));
        assertEquals(Map.of(BEAN_NAME, DUMMY), BeanFactory.getBeansOfType(Object.class));
    }

    @Test
    void testBeanRegistration()
    {
        var object = new Object();
        BeanFactory.registerBean(BEAN_NAME, Object.class, () -> object);
        verify(context).registerBean(eq(BEAN_NAME), eq(Object.class), argThat(
                (ArgumentMatcher<Supplier<Object>>) objectSupplier -> objectSupplier.get() == object));
    }

    @Test
    void testIsActive()
    {
        when(context.isActive()).thenReturn(true);
        assertTrue(BeanFactory.isActive());
    }

    @Test
    void testCloseActiveContext()
    {
        when(context.isActive()).thenReturn(true);
        BeanFactory.close();
        verify(context).close();
    }

    @Test
    void testCloseNotActiveContext()
    {
        when(context.isActive()).thenReturn(false);
        BeanFactory.close();
    }

    @Test
    void testReset()
    {
        when(context.isActive()).thenReturn(true);
        BeanFactory.reset();
        verify(context).close();
        assertNotSame(context, BeanFactory.getResourcePatternResolver());
    }

    @Test
    void testGetBeanDefinitionNames()
    {
        when(context.getBeanDefinitionNames()).thenReturn(new String [] {BEAN_NAME});
        assertArrayEquals(new String [] {BEAN_NAME}, BeanFactory.getBeanDefinitionNames());
    }
}
