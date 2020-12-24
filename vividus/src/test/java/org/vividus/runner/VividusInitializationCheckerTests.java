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

package org.vividus.runner;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.vividus.SystemStreamTests;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

@ExtendWith(TestLoggerFactoryExtension.class)
class VividusInitializationCheckerTests extends SystemStreamTests
{
    private static final String BEAN_1 = "bean1";
    private static final String BEAN_2 = "bean2";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(VividusInitializationChecker.class);

    @Test
    void testPrintHelp() throws ParseException
    {
        try (MockedStatic<Vividus> vividus = mockStatic(Vividus.class))
        {
            VividusInitializationChecker.main(new String[] {"-h"});
            assertOutput(List.of("usage: VividusInitializationChecker",
                    " -h,--help                print this message.",
                    " -i,--ignoreBeans <arg>   comma separated list of beans that are not",
                    "                          instantiated during check (e.g. bean1,bean2)"
            ));
            vividus.verify(Vividus::init);
        }
    }

    @Test
    void testNoArguments() throws ParseException
    {
        try (MockedStatic<Vividus> vividus = mockStatic(Vividus.class);
                MockedStatic<BeanFactory> beanFactory = mockStatic(BeanFactory.class))
        {
            beanFactory.when(BeanFactory::getBeanDefinitionNames).thenReturn(new String[] { BEAN_1, BEAN_2 });
            VividusInitializationChecker.main(new String[0]);
            vividus.verify(Vividus::init);
            beanFactory.verify(() -> {
                BeanFactory.getBean(BEAN_1);
                BeanFactory.getBean(BEAN_2);
            });
        }
    }

    @Test
    void shouldIgnoreAbstractBeans() throws ParseException
    {
        try (MockedStatic<Vividus> vividus = mockStatic(Vividus.class);
                MockedStatic<BeanFactory> beanFactory = mockStatic(BeanFactory.class))
        {
            beanFactory.when(BeanFactory::getBeanDefinitionNames).thenReturn(new String[] { BEAN_1, BEAN_2 });
            beanFactory.when(() -> BeanFactory.getBean(BEAN_1)).thenThrow(new BeanIsAbstractException(BEAN_1));
            VividusInitializationChecker.main(new String[0]);
            vividus.verify(Vividus::init);
            beanFactory.verify(() -> {
                BeanFactory.getBean(BEAN_1);
                BeanFactory.getBean(BEAN_2);
            });
        }
    }

    @Test
    void shouldFailInCaseOfException()
    {
        try (MockedStatic<Vividus> vividus = mockStatic(Vividus.class);
                MockedStatic<BeanFactory> beanFactory = mockStatic(BeanFactory.class))
        {
            beanFactory.when(BeanFactory::getBeanDefinitionNames).thenReturn(new String[] { BEAN_1, BEAN_2 });
            IllegalStateException exception = new IllegalStateException();
            beanFactory.when(() -> BeanFactory.getBean(BEAN_2)).thenThrow(exception);
            RuntimeException runtimeException = assertThrows(RuntimeException.class,
                    () -> VividusInitializationChecker.main(new String[0]));
            assertEquals("Initialization of beans has been failed", runtimeException.getMessage());
            vividus.verify(Vividus::init);
            beanFactory.verify(() -> BeanFactory.getBean(BEAN_1));
            assertThat(logger.getLoggingEvents(), equalTo(List.of(error(exception.toString()))));
        }
    }

    @Test
    void testIgnoreBeansOptionIsPresent() throws ParseException
    {
        try (MockedStatic<Vividus> vividus = mockStatic(Vividus.class);
                MockedStatic<BeanFactory> beanFactory = mockStatic(BeanFactory.class))
        {
            beanFactory.when(BeanFactory::getBeanDefinitionNames).thenReturn(new String[] { BEAN_1, BEAN_2 });
            VividusInitializationChecker.main(new String[] { "--ignoreBeans", BEAN_2 });
            vividus.verify(Vividus::init);
            beanFactory.verify(() -> BeanFactory.getBean(BEAN_1));
            beanFactory.verify(never(), () -> BeanFactory.getBean(BEAN_2));
        }
    }

    @Test
    void testUnknownOptionIsPresent()
    {
        try (MockedStatic<Vividus> vividus = mockStatic(Vividus.class))
        {
            assertThrows(UnrecognizedOptionException.class,
                    () -> VividusInitializationChecker.main(new String[] { "--any" }));
            vividus.verify(Vividus::init);
        }
    }
}
