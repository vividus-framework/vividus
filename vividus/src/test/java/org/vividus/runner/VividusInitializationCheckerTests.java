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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Vividus.class, BeanFactory.class })
public class VividusInitializationCheckerTests
{
    private static final String BEAN_1 = "bean1";
    private static final String BEAN_2 = "bean2";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(VividusInitializationChecker.class);

    @Before
    public void before()
    {
        PowerMockito.mockStatic(Vividus.class);
        PowerMockito.mockStatic(BeanFactory.class);
        when(BeanFactory.getBeanDefinitionNames()).thenReturn(new String[] { BEAN_1, BEAN_2 });
    }

    @After
    public void after()
    {
        TestLoggerFactory.clear();
    }

    @Test
    public void testNoArguments() throws ParseException
    {
        VividusInitializationChecker.main(new String[0]);
        verifyStatic(BeanFactory.class);
        BeanFactory.getBean(BEAN_1);
        verifyStatic(BeanFactory.class);
        BeanFactory.getBean(BEAN_2);
    }

    @Test
    public void shouldIgnoreAbstractBeans() throws ParseException
    {
        when(BeanFactory.getBean(BEAN_1)).thenThrow(new BeanIsAbstractException(BEAN_1));
        VividusInitializationChecker.main(new String[0]);
        verifyStatic(BeanFactory.class);
        BeanFactory.getBean(BEAN_1);
        verifyStatic(BeanFactory.class);
        BeanFactory.getBean(BEAN_2);
    }

    @Test
    public void shouldFailInCaseOfException()
    {
        IllegalStateException exception = new IllegalStateException();
        when(BeanFactory.getBean(BEAN_2)).thenThrow(exception);
        RuntimeException runtimeException = assertThrows(RuntimeException.class,
            () -> VividusInitializationChecker.main(new String[0]));
        assertEquals("Initialization of beans has been failed", runtimeException.getMessage());
        verifyStatic(BeanFactory.class);
        BeanFactory.getBean(BEAN_1);
        verifyStatic(BeanFactory.class);
        BeanFactory.getBean(BEAN_2);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(error(exception.toString()))));
    }

    @Test
    public void testIgnoreBeansOptionIsPresent() throws ParseException
    {
        VividusInitializationChecker.main(new String[] { "--ignoreBeans", BEAN_2 });
        verifyStatic(BeanFactory.class);
        BeanFactory.getBean(BEAN_1);
        verifyStatic(BeanFactory.class, never());
        BeanFactory.getBean(BEAN_2);
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownOptionIsPresent() throws ParseException
    {
        VividusInitializationChecker.main(new String[] { "--any" });
    }
}
