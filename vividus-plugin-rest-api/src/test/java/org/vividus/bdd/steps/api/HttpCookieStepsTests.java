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

package org.vividus.bdd.steps.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.http.CookieStoreProvider;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class HttpCookieStepsTests
{
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String ANOTHER_PATH = "/anotherPath";
    private static final String NUMBER_OF_COOKIES_PATTERN = "Number of cookies with name '%s'";
    private static final Set<VariableScope> SCENARIO_SCOPE = Set.of(VariableScope.SCENARIO);
    private static final String NUMBER_OF_COOKIES_WITH_NAME_AND_PATH =
            "Number of cookies with name '%s' and path attribute '%s'";
    private static final String PATH = "/";
    private static final String NEW_VALUE = "newValue";
    private static final String NEW_NAME = "newName";

    @Mock
    private Cookie cookie;

    @Mock
    private CookieStoreProvider cookieStoreProvider;

    @Mock
    private IBddVariableContext bddVariableContext;

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
        List<Cookie> cookies = List.of(cookie);
        when(cookieStore.getCookies()).thenReturn(cookies);
        when(cookie.getName()).thenReturn(NAME);
        when(cookie.getValue()).thenReturn(VALUE);
        when(softAssert.assertThat(eq(String.format(NUMBER_OF_COOKIES_PATTERN, NAME)), eq(cookies.size()),
                any(Matcher.class))).thenReturn(true);
        httpCookieSteps.saveHttpCookieIntoVariable(NAME, SCENARIO_SCOPE, NAME);
        verifyCookieAssertion(cookies);
        verify(bddVariableContext).putVariable(SCENARIO_SCOPE, NAME, VALUE);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldSaveCookieWithRootPathValueIntoVariableContext()
    {
        Cookie cookieWithAnotherPath = mock(Cookie.class);
        List<Cookie> cookies = List.of(cookie, cookieWithAnotherPath);
        when(cookieStore.getCookies()).thenReturn(cookies);
        when(cookie.getName()).thenReturn(NAME);
        when(cookieWithAnotherPath.getName()).thenReturn(NAME);
        when(cookie.getPath()).thenReturn(PATH);
        when(cookieWithAnotherPath.getPath()).thenReturn(ANOTHER_PATH);
        when(cookie.getValue()).thenReturn(VALUE);
        when(softAssert.assertThat(eq(String.format(NUMBER_OF_COOKIES_PATTERN, NAME)), eq(cookies.size()),
                any(Matcher.class))).thenReturn(true);
        when(softAssert.assertEquals(String.format(NUMBER_OF_COOKIES_WITH_NAME_AND_PATH, NAME, PATH), 1, 1))
                .thenReturn(true);
        httpCookieSteps.saveHttpCookieIntoVariable(NAME, SCENARIO_SCOPE, NAME);
        verifyCookieAssertion(cookies);
        verify(bddVariableContext).putVariable(SCENARIO_SCOPE, NAME, VALUE);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldNotSaveCookiesWithTheSameNameAndNotRootPathVariableContext()
    {
        Cookie cookieWithAnotherPath = mock(Cookie.class);
        List<Cookie> cookies = List.of(cookie, cookieWithAnotherPath);
        when(cookieStore.getCookies()).thenReturn(cookies);
        when(cookie.getName()).thenReturn(NAME);
        when(cookieWithAnotherPath.getName()).thenReturn(NAME);
        when(cookie.getPath()).thenReturn(ANOTHER_PATH);
        when(cookieWithAnotherPath.getPath()).thenReturn(ANOTHER_PATH + "/1");
        when(softAssert.assertThat(eq(String.format(NUMBER_OF_COOKIES_PATTERN, NAME)), eq(cookies.size()),
                any(Matcher.class))).thenReturn(true);
        when(softAssert.assertEquals(String.format(NUMBER_OF_COOKIES_WITH_NAME_AND_PATH, NAME, PATH), 1, 0))
                .thenReturn(false);
        httpCookieSteps.saveHttpCookieIntoVariable(NAME, SCENARIO_SCOPE, NAME);
        verifyCookieAssertion(cookies);
        verifyNoInteractions(bddVariableContext);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldNotSaveNotExistingCookieAndRecordAssertion()
    {
        List<Cookie> cookies = Collections.emptyList();
        when(cookieStore.getCookies()).thenReturn(cookies);
        httpCookieSteps.saveHttpCookieIntoVariable(NAME, SCENARIO_SCOPE, NAME);
        verifyCookieAssertion(cookies);
        verifyNoInteractions(bddVariableContext);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldChangeCookieValue()
    {
        Cookie cookie = new BasicClientCookie(NAME, VALUE);
        List<Cookie> cookies = List.of(cookie);
        when(cookieStore.getCookies()).thenReturn(cookies);
        when(softAssert.assertThat(eq(String.format(NUMBER_OF_COOKIES_PATTERN, NAME)), eq(cookies.size()),
                any(Matcher.class))).thenReturn(true);
        httpCookieSteps.changeHttpCookieValue(NAME, NEW_VALUE);
        verifyCookieAssertion(cookies);
        assertEquals(NEW_VALUE, cookie.getValue());
    }

    @Test
    void shouldNotChangeCookieValue()
    {
        Cookie cookie = mock(Cookie.class);
        List<Cookie> cookies = List.of(cookie);
        when(cookieStore.getCookies()).thenReturn(cookies);
        when(cookie.getName()).thenReturn(NEW_NAME);
        when(softAssert.assertThat(eq(String.format(NUMBER_OF_COOKIES_PATTERN, NAME)), eq(0),
                any(Matcher.class))).thenReturn(false);
        httpCookieSteps.changeHttpCookieValue(NAME, NEW_VALUE);
        verifyCookieAssertion(Collections.emptyList());
        verifyNoMoreInteractions(softAssert);
        verifyNoMoreInteractions(cookie);
    }

    @Test
    void shouldNotAbleToChangeCookieValue()
    {
        Cookie cookie = mock(Cookie.class);
        List<Cookie> cookies = List.of(cookie);
        when(cookieStore.getCookies()).thenReturn(cookies);
        when(cookie.getName()).thenReturn(NAME);
        when(softAssert.assertThat(eq(String.format(NUMBER_OF_COOKIES_PATTERN, NAME)), eq(cookies.size()),
                any(Matcher.class))).thenReturn(true);
        Throwable exception = assertThrows(IllegalStateException.class, () ->
                httpCookieSteps.changeHttpCookieValue(NAME, NEW_VALUE));
        assertEquals(String.format("Unable to change value of cookie with name '%s' of type '%s'", NAME,
                cookie.getClass().getName()), exception.getMessage());
    }

    private void verifyCookieAssertion(List<Cookie> cookies)
    {
        verify(softAssert).assertThat(eq(String.format(NUMBER_OF_COOKIES_PATTERN, NAME)), eq(cookies.size()),
                        argThat(arg -> "a value greater than <0>".equals(arg.toString())));
    }
}
