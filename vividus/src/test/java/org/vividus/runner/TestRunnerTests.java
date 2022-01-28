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

package org.vividus.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.util.List;
import java.util.Properties;

import org.jbehave.core.embedder.Embedder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;
import org.vividus.log.TestInfoLogger;

class TestRunnerTests
{
    @Test
    void shouldPerformInitInInstanceInitializationBlock()
    {
        try (MockedStatic<Vividus> vividus = mockStatic(Vividus.class);
                MockedStatic<BeanFactory> beanFactory = mockStatic(BeanFactory.class);
                MockedStatic<TestInfoLogger> testInfoLogger = mockStatic(TestInfoLogger.class))
        {
            Properties systemProperties = System.getProperties();
            Properties springProperties = mock(Properties.class);
            beanFactory.when(() -> BeanFactory.getBean("properties", Properties.class)).thenReturn(springProperties);
            Embedder embedder = mock(Embedder.class);
            AbstractTestRunner genericRunner = new TestRunner(embedder);
            assertEquals(embedder, genericRunner.configuredEmbedder());
            vividus.verify(Vividus::init);
            testInfoLogger.verify(() -> TestInfoLogger.logPropertiesSecurely(systemProperties));
            testInfoLogger.verify(() -> TestInfoLogger.logPropertiesSecurely(springProperties));
        }
    }

    private static final class TestRunner extends AbstractTestRunner
    {
        private final Embedder embedder;

        private TestRunner(Embedder embedder)
        {
            this.embedder = embedder;
        }

        @Override
        public Embedder configuredEmbedder()
        {
            return embedder;
        }

        @Override
        public List<String> storyPaths()
        {
            return List.of();
        }
    }
}
