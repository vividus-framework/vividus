/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.mcp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.vividus.configuration.BeanFactory;
import org.vividus.util.Sleeper;

class McpServerTests
{
    @Test
    void shouldRunMcpServer()
    {
        VividusMcpServer vividusMcpServer = mock();
        try (MockedStatic<BeanFactory> beanFactoryMock = mockStatic(BeanFactory.class);
                MockedStatic<Sleeper> sleeperMock = mockStatic(Sleeper.class))
        {
            beanFactoryMock.when(BeanFactory::open).thenAnswer(invocation -> null);
            beanFactoryMock.when(() -> BeanFactory.getBean(VividusMcpServer.class)).thenReturn(vividusMcpServer);
            sleeperMock.when(() -> Sleeper.sleep(any())).thenAnswer(inv ->
            {
                throw new InterruptedException();
            });

            try
            {
                McpServer.main(new String[] { });
            }
            catch (InterruptedException e)
            {
                // empty
            }

            beanFactoryMock.verify(BeanFactory::open);
            beanFactoryMock.verify(() -> BeanFactory.getBean(VividusMcpServer.class));
            verify(vividusMcpServer).startSyncServer();
            sleeperMock.verify(() -> Sleeper.sleep(any()), atLeastOnce());
        }
    }
}
