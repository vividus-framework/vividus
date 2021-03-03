/*
 * Copyright 2019-2021 the original author or authors.
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

import java.util.Properties;

import org.jbehave.core.embedder.Embedder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.vividus.bdd.IBatchedPathFinder;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;
import org.vividus.report.MetadataLogger;

class GenericRunnerTests
{
    @Test
    void shouldPerformInitInInstanceInitializationBlock()
    {
        String embedderBeanName = "embedderBeanName";
        GenericRunner.setEmbedderBeanName(embedderBeanName);
        try (MockedStatic<Vividus> vividus = mockStatic(Vividus.class);
                MockedStatic<BeanFactory> beanFactory = mockStatic(BeanFactory.class);
                MockedStatic<MetadataLogger> metadataLogger = mockStatic(MetadataLogger.class))
        {
            IBatchedPathFinder batchedPathFinder = mock(IBatchedPathFinder.class);
            beanFactory.when(() -> BeanFactory.getBean(IBatchedPathFinder.class)).thenReturn(batchedPathFinder);
            Embedder embedder = mock(Embedder.class);
            beanFactory.when(() -> BeanFactory.getBean(embedderBeanName, Embedder.class)).thenReturn(embedder);
            Properties systemProperties = System.getProperties();
            Properties springProperties = mock(Properties.class);
            beanFactory.when(() -> BeanFactory.getBean("properties", Properties.class)).thenReturn(springProperties);
            GenericRunner genericRunner = new GenericRunner();
            assertEquals(embedder, genericRunner.configuredEmbedder());
            vividus.verify(Vividus::init);
            metadataLogger.verify(() -> MetadataLogger.drawBanner());
            metadataLogger.verify(() -> MetadataLogger.logPropertiesSecurely(systemProperties));
            metadataLogger.verify(() -> MetadataLogger.logPropertiesSecurely(springProperties));
        }
    }
}
