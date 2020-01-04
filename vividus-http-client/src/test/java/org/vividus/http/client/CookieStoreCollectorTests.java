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

package org.vividus.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Stream;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.jupiter.api.Test;

class CookieStoreCollectorTests
{
    private final CookieStoreCollector cookieStoreCollector = new CookieStoreCollector();

    @Test
    void testFinisher()
    {
        assertEquals(Function.identity(), cookieStoreCollector.finisher());
    }

    @Test
    void testCharacteristics()
    {
        Set<Characteristics> characteristics = cookieStoreCollector.characteristics();
        assertEquals(characteristics, EnumSet.of(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH));
    }

    @Test
    void testIntegrationMethodsFromCollector()
    {
        Cookie cookie1 = new BasicClientCookie("name", "value");
        Cookie cookie2 = new BasicClientCookie("name2", "value2");
        CookieStore cookieStore = Stream.of(cookie1, cookie2).parallel().collect(ClientBuilderUtils.toCookieStore());
        assertEquals(cookieStore.getCookies(), List.of(cookie1, cookie2));
    }
}
