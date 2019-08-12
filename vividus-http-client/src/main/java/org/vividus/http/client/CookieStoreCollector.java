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

package org.vividus.http.client;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

class CookieStoreCollector implements Collector<Cookie, CookieStore, CookieStore>
{
    @Override
    public BiConsumer<CookieStore, Cookie> accumulator()
    {
        return CookieStore::addCookie;
    }

    @Override
    public Set<Characteristics> characteristics()
    {
        return EnumSet.of(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH);
    }

    @Override
    public BinaryOperator<CookieStore> combiner()
    {
        return (l, r) ->
        {
            r.getCookies().forEach(l::addCookie);
            return l;
        };
    }

    @Override
    public Function<CookieStore, CookieStore> finisher()
    {
        return Function.identity();
    }

    @Override
    public Supplier<CookieStore> supplier()
    {
        return BasicCookieStore::new;
    }
}
