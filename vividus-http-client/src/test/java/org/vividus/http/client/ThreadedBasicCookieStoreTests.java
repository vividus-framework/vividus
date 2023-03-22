/*
 * Copyright 2019-2023 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.junit.jupiter.api.Test;

class ThreadedBasicCookieStoreTests
{
    private static final String NAME = "name";
    private static final String VALUE = "value";

    private static final BasicClientCookie COOKIE = new BasicClientCookie(NAME, VALUE);
    private final ThreadedBasicCookieStore threadedBasicCookieStore = new ThreadedBasicCookieStore();

    @Test
    void testAddCookie()
    {
        threadedBasicCookieStore.addCookie(COOKIE);
        assertTrue(threadedBasicCookieStore.getCookies().contains(COOKIE));
    }

    @Test
    void testClearCookie()
    {
        threadedBasicCookieStore.addCookie(COOKIE);
        threadedBasicCookieStore.clear();
        assertTrue(threadedBasicCookieStore.getCookies().isEmpty());
    }

    @Test
    void testClearExpired()
    {
        var expired = new BasicClientCookie(NAME, VALUE);
        expired.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS));
        var notExpired = new BasicClientCookie(NAME, VALUE);
        notExpired.setExpiryDate(Instant.now().plus(2, ChronoUnit.DAYS));
        threadedBasicCookieStore.addCookie(expired);
        threadedBasicCookieStore.addCookie(notExpired);
        threadedBasicCookieStore.clearExpired(new Date());
        assertTrue(threadedBasicCookieStore.getCookies().contains(notExpired));
        assertFalse(threadedBasicCookieStore.getCookies().contains(expired));
    }

    @Test
    void testToString()
    {
        threadedBasicCookieStore.addCookie(COOKIE);
        assertTrue(threadedBasicCookieStore.toString().contains(NAME));
        assertTrue(threadedBasicCookieStore.toString().contains(VALUE));
    }
}
