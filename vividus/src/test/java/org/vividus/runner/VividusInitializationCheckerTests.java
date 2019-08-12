/*
 * Copyright 2019 the original author or authors.
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Vividus.class, BeanFactory.class })
public class VividusInitializationCheckerTests
{
    private static final String BEAN_1 = "bean1";
    private static final String BEAN_2 = "bean2";

    @Before
    public void before()
    {
        PowerMockito.mockStatic(Vividus.class);
        PowerMockito.mockStatic(BeanFactory.class);
        when(BeanFactory.getBeanDefinitionNames()).thenReturn(new String[] { BEAN_1, BEAN_2 });
    }

    @Test
    public void testNoArguments() throws Exception
    {
        VividusInitializationChecker.main(new String[0]);
        verifyStatic(BeanFactory.class);
        BeanFactory.getBean(BEAN_1);
        verifyStatic(BeanFactory.class);
        BeanFactory.getBean(BEAN_2);
    }

    @Test
    public void testIgnoreBeansOptionIsPresent() throws Exception
    {
        VividusInitializationChecker.main(new String[] { "--ignoreBeans", BEAN_2 });
        verifyStatic(BeanFactory.class);
        BeanFactory.getBean(BEAN_1);
        verifyStatic(BeanFactory.class, never());
        BeanFactory.getBean(BEAN_2);
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownOptionIsPresent() throws Exception
    {
        VividusInitializationChecker.main(new String[] { "--any" });
    }
}
