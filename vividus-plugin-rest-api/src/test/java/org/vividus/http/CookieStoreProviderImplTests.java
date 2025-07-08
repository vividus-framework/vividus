/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class CookieStoreProviderImplTests
{
    private static final BasicClientCookie COOKIE = new BasicClientCookie("name", "value");

    @ParameterizedTest
    @CsvSource({
            "GLOBAL,   org.apache.hc.client5.http.cookie.BasicCookieStore",
            "STORY,    org.vividus.http.client.ThreadedBasicCookieStore",
            "SCENARIO, org.vividus.http.client.ThreadedBasicCookieStore",
            "STEP,     org.vividus.http.client.ThreadedBasicCookieStore"
    })
    void shouldCreateCookieStore(CookieStoreLevel cookieStoreLevel, Class<?> clazz)
    {
        var cookieStoreProvider = new CookieStoreProviderImpl(cookieStoreLevel);
        assertEquals(clazz, cookieStoreProvider.getCookieStore().getClass());
    }

    @ParameterizedTest
    @EnumSource(names = "STEP", mode = Mode.EXCLUDE)
    void shouldNotClearCookieStoreAfterStep(CookieStoreLevel cookieStoreLevel)
    {
        var cookieStoreProvider = createProviderWithCookie(cookieStoreLevel);
        cookieStoreProvider.afterPerforming(null, false, null);
        assertEquals(List.of(COOKIE), cookieStoreProvider.getCookieStore().getCookies());
    }

    @ParameterizedTest
    @EnumSource(names = "SCENARIO", mode = Mode.EXCLUDE)
    void shouldNotClearCookieStoreAfterScenario(CookieStoreLevel cookieStoreLevel)
    {
        var cookieStoreProvider = createProviderWithCookie(cookieStoreLevel);
        cookieStoreProvider.resetScenarioCookies();
        assertEquals(List.of(COOKIE), cookieStoreProvider.getCookieStore().getCookies());
    }

    @ParameterizedTest
    @EnumSource(names = "STORY", mode = Mode.EXCLUDE)
    void shouldNotClearCookieStoreAfterStory(CookieStoreLevel cookieStoreLevel)
    {
        var cookieStoreProvider = createProviderWithCookie(cookieStoreLevel);
        cookieStoreProvider.resetStoryCookies();
        assertEquals(List.of(COOKIE), cookieStoreProvider.getCookieStore().getCookies());
    }

    @Test
    void shouldClearCookieStoreAfterStep()
    {
        var cookieStoreProvider = createProviderWithCookie(CookieStoreLevel.STEP);
        cookieStoreProvider.afterPerforming(null, false, null);
        assertEquals(List.of(), cookieStoreProvider.getCookieStore().getCookies());
    }

    @Test
    void shouldNotClearCookieStoreAfterStepOnDryRun()
    {
        var cookieStoreProvider = createProviderWithCookie(CookieStoreLevel.STEP);
        cookieStoreProvider.afterPerforming(null, true, null);
        assertEquals(List.of(COOKIE), cookieStoreProvider.getCookieStore().getCookies());
    }

    @Test
    void shouldClearCookieStoreAfterScenario()
    {
        var cookieStoreProvider = createProviderWithCookie(CookieStoreLevel.SCENARIO);
        cookieStoreProvider.resetScenarioCookies();
        assertEquals(List.of(), cookieStoreProvider.getCookieStore().getCookies());
    }

    @Test
    void shouldClearCookieStoreAfterStory()
    {
        var cookieStoreProvider = createProviderWithCookie(CookieStoreLevel.STORY);
        cookieStoreProvider.resetStoryCookies();
        assertEquals(List.of(), cookieStoreProvider.getCookieStore().getCookies());
    }

    private CookieStoreProviderImpl createProviderWithCookie(CookieStoreLevel cookieStoreLevel)
    {
        var cookieStoreProvider = new CookieStoreProviderImpl(cookieStoreLevel);
        cookieStoreProvider.getCookieStore().addCookie(COOKIE);
        return cookieStoreProvider;
    }
}
