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

package org.vividus.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProxyFactoryTests
{
    @InjectMocks
    private ProxyFactory proxyFactory;

    @Test
    void testCreateProxy() throws IllegalAccessException
    {
        String proxyHost = "somehost";
        proxyFactory.setProxyHost(proxyHost);
        Proxy actualProxy = proxyFactory.createProxy();
        assertNotNull(actualProxy);
        assertEquals(proxyHost, FieldUtils.getField(Proxy.class, "proxyHost", true).get(actualProxy));
    }
}
