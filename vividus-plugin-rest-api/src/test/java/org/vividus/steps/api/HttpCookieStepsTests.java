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

package org.vividus.steps.api;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.http.CookieStoreProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.variable.VariableScope;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class HttpCookieStepsTests
{
    private static final String NAME = "name";
    private static final String ANOTHER_NAME = "newName";
    private static final String VALUE = "value";
    private static final String NEW_VALUE = "newValue";
    private static final String ROOT = "/";
    private static final String NOT_ROOT = "/anotherPath";

    private static final Set<VariableScope> SCENARIO_SCOPE = Set.of(VariableScope.SCENARIO);

    private static final String NUMBER_OF_COOKIES_WITH_NAME = String.format("Number of cookies with name '%s'", NAME);
    private static final String NUMBER_OF_COOKIES_WITH_NAME_AND_PATH = String.format(
            "Number of cookies with name '%s' and path attribute '%s'", NAME, ROOT);

    private static final String FILTERING_COOKIES_LOG_MESSAGE = "Filtering cookies by path attribute '{}'";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(HttpCookieSteps.class);

    @Mock
    private CookieStoreProvider cookieStoreProvider;

    @Mock
    private VariableContext variableContext;

    @Mock
    private ISoftAssert softAssert;

    @InjectMocks
    private HttpCookieSteps httpCookieSteps;

    private CookieStore cookieStore;

    @BeforeEach
    void beforeEach()
    {
        cookieStore = mock(CookieStore.class);
        when(cookieStoreProvider.getCookieStore()).thenReturn(cookieStore);
    }

    @Test
    void shouldSaveCookieValueIntoVariableContext()
    {
        List<Cookie> cookies = List.of(createCookie(NAME, NOT_ROOT));
        when(cookieStore.getCookies()).thenReturn(cookies);
        when(softAssert.assertThat(eq(NUMBER_OF_COOKIES_WITH_NAME), eq(1), getSizeMatcher())).thenReturn(true);
        httpCookieSteps.saveHttpCookieIntoVariable(NAME, SCENARIO_SCOPE, NAME);
        verify(softAssert).assertThat(eq(NUMBER_OF_COOKIES_WITH_NAME), eq(1), getSizeMatcher());
        verify(variableContext).putVariable(SCENARIO_SCOPE, NAME, VALUE);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldSaveCookieWithRootPathValueIntoVariableContext()
    {
        List<Cookie> cookies = List.of(createCookie(NAME, ROOT), createCookie(ANOTHER_NAME, ROOT),
                createCookie(NAME, NOT_ROOT));
        when(cookieStore.getCookies()).thenReturn(cookies);
        when(softAssert.assertThat(eq(NUMBER_OF_COOKIES_WITH_NAME), eq(2), getSizeMatcher())).thenReturn(true);
        when(softAssert.assertEquals(NUMBER_OF_COOKIES_WITH_NAME_AND_PATH, 1, 1)).thenReturn(true);
        httpCookieSteps.saveHttpCookieIntoVariable(NAME, SCENARIO_SCOPE, NAME);
        verify(softAssert).assertThat(eq(NUMBER_OF_COOKIES_WITH_NAME), eq(2), getSizeMatcher());
        verify(variableContext).putVariable(SCENARIO_SCOPE, NAME, VALUE);
        verifyNoMoreInteractions(softAssert);
        assertThat(logger.getLoggingEvents(), is(List.of(info(FILTERING_COOKIES_LOG_MESSAGE, ROOT))));
    }

    @Test
    void shouldNotSaveCookiesWithTheSameNameAndNotRootPathVariableContext()
    {
        List<Cookie> cookies = List.of(createCookie(NAME, NOT_ROOT), createCookie(NAME, NOT_ROOT));
        when(cookieStore.getCookies()).thenReturn(cookies);
        when(softAssert.assertThat(eq(NUMBER_OF_COOKIES_WITH_NAME), eq(2), getSizeMatcher())).thenReturn(true);
        when(softAssert.assertEquals(NUMBER_OF_COOKIES_WITH_NAME_AND_PATH, 1, 0)).thenReturn(false);
        httpCookieSteps.saveHttpCookieIntoVariable(NAME, SCENARIO_SCOPE, NAME);
        verify(softAssert).assertThat(eq(NUMBER_OF_COOKIES_WITH_NAME), eq(2), getSizeMatcher());
        verifyNoInteractions(variableContext);
        verifyNoMoreInteractions(softAssert);
        assertThat(logger.getLoggingEvents(), is(List.of(info(FILTERING_COOKIES_LOG_MESSAGE, ROOT))));
    }

    @Test
    void shouldNotSaveNotExistingCookieAndRecordAssertion()
    {
        when(cookieStore.getCookies()).thenReturn(List.of());
        httpCookieSteps.saveHttpCookieIntoVariable(NAME, SCENARIO_SCOPE, NAME);
        verify(softAssert).assertThat(eq(NUMBER_OF_COOKIES_WITH_NAME), eq(0), getSizeMatcher());
        verifyNoInteractions(variableContext);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldChangeCookieValue()
    {
        Cookie cookie = new BasicClientCookie(NAME, VALUE);
        List<Cookie> cookies = List.of(cookie);
        when(cookieStore.getCookies()).thenReturn(cookies);
        when(softAssert.assertThat(eq(NUMBER_OF_COOKIES_WITH_NAME), eq(1), getSizeMatcher())).thenReturn(true);
        httpCookieSteps.changeHttpCookieValue(NAME, NEW_VALUE);
        verify(softAssert).assertThat(eq(NUMBER_OF_COOKIES_WITH_NAME), eq(1), getSizeMatcher());
        assertEquals(NEW_VALUE, cookie.getValue());
    }

    @Test
    void shouldNotChangeCookieValue()
    {
        Cookie cookie = mock(Cookie.class);
        when(cookieStore.getCookies()).thenReturn(List.of(cookie));
        when(cookie.getName()).thenReturn(ANOTHER_NAME);
        when(softAssert.assertThat(eq(NUMBER_OF_COOKIES_WITH_NAME), eq(0), getSizeMatcher())).thenReturn(false);
        httpCookieSteps.changeHttpCookieValue(NAME, NEW_VALUE);
        verify(softAssert).assertThat(eq(NUMBER_OF_COOKIES_WITH_NAME), eq(0), getSizeMatcher());
        verifyNoMoreInteractions(softAssert, cookie);
    }

    @Test
    void shouldNotAbleToChangeCookieValue()
    {
        Cookie cookie = mock(Cookie.class);
        List<Cookie> cookies = List.of(cookie);
        when(cookieStore.getCookies()).thenReturn(cookies);
        when(cookie.getName()).thenReturn(NAME);
        when(softAssert.assertThat(eq(NUMBER_OF_COOKIES_WITH_NAME), eq(1), getSizeMatcher())).thenReturn(true);
        Throwable exception = assertThrows(IllegalStateException.class, () ->
                httpCookieSteps.changeHttpCookieValue(NAME, NEW_VALUE));
        assertEquals(String.format("Unable to change value of cookie with name '%s' of type '%s'", NAME,
                cookie.getClass().getName()), exception.getMessage());
    }

    private BasicClientCookie createCookie(String name, String path)
    {
        BasicClientCookie cookie = new BasicClientCookie(name, VALUE);
        cookie.setPath(path);
        return cookie;
    }

    private Matcher<? super Integer> getSizeMatcher()
    {
        return argThat(arg -> "a value greater than <0>".equals(arg.toString()));
    }
}
