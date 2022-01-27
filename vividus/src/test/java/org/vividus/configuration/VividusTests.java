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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.vividus.log.TestInfoLogger;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;

class VividusTests
{
    @Test
    void shouldDrawBannerAfterLoggerConfigurations()
    {
        try (MockedStatic<BeanFactory> beanFactory = mockStatic(BeanFactory.class);
                MockedStatic<TestInfoLogger> testInfoLogger = mockStatic(TestInfoLogger.class);
                MockedConstruction<ClassGraph> classGraph = mockConstruction(ClassGraph.class, (mock, context) -> {
                    when(mock.acceptPackagesNonRecursive("")).thenReturn(mock);
                    ScanResult scanResult = Mockito.mock(ScanResult.class);
                    when(mock.scan()).thenReturn(scanResult);
                    when(scanResult.getAllResources()).thenReturn(ResourceList.emptyList());
                }))
        {
            Vividus.init();
            List<ClassGraph> constructedScanners = classGraph.constructed();
            assertThat(constructedScanners, hasSize(1));
            verify(classGraph.constructed().get(0)).scan();
            testInfoLogger.verify(TestInfoLogger::drawBanner);
            beanFactory.verify(BeanFactory::open);
        }
    }
}
