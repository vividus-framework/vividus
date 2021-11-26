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

package org.vividus.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.vividus.context.VariableContext;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.CookieManager;
import org.vividus.ui.web.action.INavigateActions;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class CookieStepsTests
{
    private static final String NAME = "name";

    @Mock private INavigateActions navigateActions;
    @Mock private ISoftAssert softAssert;
    @Mock private CookieManager cookieManager;
    @Mock private Cookie cookie;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private VariableContext variableContext;
    @Mock private JsonUtils jsonUtils;
    @InjectMocks private CookieSteps cookieSteps;

    @Test
    void shouldRemoveAllCookies()
    {
        cookieSteps.removeAllCookies();
        var ordered = inOrder(cookieManager, navigateActions);
        ordered.verify(cookieManager).deleteAllCookies();
        ordered.verify(navigateActions).refresh();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldRemoveAllCookiesWithoutApplyingChanges()
    {
        cookieSteps.removeAllCookiesWithoutApply();
        var ordered = inOrder(cookieManager, navigateActions);
        ordered.verify(cookieManager).deleteAllCookies();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldRemoveCookie()
    {
        cookieSteps.removeCookie(NAME);
        var ordered = inOrder(cookieManager, navigateActions);
        ordered.verify(cookieManager).deleteCookie(NAME);
        ordered.verify(navigateActions).refresh();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldRemoveCookieWithoutApplyingChanges()
    {
        cookieSteps.removeCookieWithoutApply(NAME);
        var ordered = inOrder(cookieManager, navigateActions);
        ordered.verify(cookieManager).deleteCookie(NAME);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldSaveCookieValueIntoVariableContext()
    {
        when(cookieManager.getCookie(NAME)).thenReturn(cookie);
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        String value = "value";
        when(cookie.getValue()).thenReturn(value);
        cookieSteps.saveCookieIntoVariable(NAME, scopes, NAME);
        verify(variableContext).putVariable(scopes, NAME, value);
        verifyCookieAssertion(cookie);
    }

    @Test
    void shouldNotSaveNotExistingCookieAndRecordAssertion()
    {
        cookieSteps.saveCookieIntoVariable(NAME, Set.of(VariableScope.SCENARIO), NAME);
        verifyNoInteractions(variableContext);
        verifyCookieAssertion(null);
    }

    @Test
    void testThenCookieWithNameIsSet()
    {
        when(cookieManager.getCookie(NAME)).thenReturn(cookie);
        cookieSteps.thenCookieWithNameIsSet(NAME);
        verifyCookieAssertion(cookie);
    }

    @Test
    void testThenCookieWithNameIsNotSet()
    {
        when(cookieManager.getCookie(NAME)).thenReturn(cookie);
        cookieSteps.thenCookieWithNameIsNotSet(NAME);
        verify(softAssert).assertThat(eq("Cookie with the name '" + NAME + "' is not set"), eq(cookie),
                argThat(matcher -> "null".equals(matcher.toString())));
    }

    @Test
    void testSetAllCookies()
    {
        String testUrl = "https://www.vividus.org";
        mockGetCurrentPageUrl(testUrl);
        String tableAsString = "|cookieName|cookieValue|path|\n|hcpsid|1|/|\n|hcpsid|1|/|";
        ExamplesTable table = new ExamplesTable(tableAsString);
        cookieSteps.setAllCookies(table);
        verify(cookieManager, times(2)).addCookie("hcpsid", "1", "/", testUrl);
        verify(navigateActions).refresh();
    }

    @Test
    void shouldThrowAnExceptionIfCurrentURLIsNotDefined()
    {
        ExamplesTable table = mock(ExamplesTable.class);
        when(webDriverProvider.get()).thenReturn(mock(WebDriver.class));
        IllegalArgumentException iae =
                assertThrows(IllegalArgumentException.class, () -> cookieSteps.setAllCookies(table));
        assertEquals("Unable to get current URL. Please make sure you've navigated to the right URL", iae.getMessage());
    }

    @Test
    void shouldNotConvertNorSaveIfNoCookieReturnedByCookieManager()
    {
        cookieSteps.saveCookieAsJson(NAME, Set.of(VariableScope.STEP), NAME);
        verifyNoInteractions(jsonUtils, variableContext);
    }

    @Test
    void shouldConvertCookieToJsonAndSaveItToContext()
    {
        when(cookieManager.getCookie(NAME)).thenReturn(cookie);
        Map<String, Object> cookieAsMap = Map.of("path", "/index");
        when(cookie.toJson()).thenReturn(cookieAsMap);
        String cookieAsJson = "{\"path\": \"/index\"}";
        when(jsonUtils.toJson(cookieAsMap)).thenReturn(cookieAsJson);
        Set<VariableScope> scopes = Set.of(VariableScope.STEP);

        cookieSteps.saveCookieAsJson(NAME, scopes, NAME);

        verify(variableContext).putVariable(scopes, NAME, cookieAsJson);
    }

    private void mockGetCurrentPageUrl(String url)
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(url);
    }

    private void verifyCookieAssertion(Cookie cookie)
    {
        verify(softAssert).assertThat(eq("Cookie with the name 'name' is set"), eq(cookie),
                argThat(m -> "not null".equals(m.toString())));
    }
}
