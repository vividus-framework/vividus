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

package org.vividus.steps.ui.web;

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
import java.util.function.Consumer;

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
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.action.INavigateActions;
import org.vividus.ui.web.action.WebDriverCookieManager;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

@SuppressWarnings("PMD.UnnecessaryBooleanAssertion")
@ExtendWith(MockitoExtension.class)
class SeleniumCookieStepsTests
{
    private static final String NAME = "name";
    private static final String DYNAMIC_COOKIE_NAME = "SSESSf4342sds23e3t5fs";

    @Mock private INavigateActions navigateActions;
    @Mock private ISoftAssert softAssert;
    @Mock private WebDriverCookieManager cookieManager;
    @Mock private Cookie cookie;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private VariableContext variableContext;
    @Mock private JsonUtils jsonUtils;
    @InjectMocks private SeleniumCookieSteps cookieSteps;

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
    void shouldAssertThatCookieWithNameMatchingComparisonRuleIsSet()
    {
        when(cookie.getName()).thenReturn(DYNAMIC_COOKIE_NAME);
        when(cookieManager.getCookies()).thenReturn(Set.of(cookie));
        cookieSteps.assertCookieIsSet(StringComparisonRule.MATCHES, "SSESS.*");
        verify(softAssert).assertTrue("Cookie with the name that matches 'SSESS.*' is set", true);
    }

    @Test
    void shouldAssertThatCookieWithNameMatchingComparisonRuleIsNotSet()
    {
        when(cookie.getName()).thenReturn(DYNAMIC_COOKIE_NAME);
        when(cookieManager.getCookies()).thenReturn(Set.of(cookie));
        cookieSteps.assertCookieIsNotSet(StringComparisonRule.CONTAINS, "_ga");
        verify(softAssert).assertTrue("Cookie with the name that contains '_ga' is not set", true);
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

    @Test
    void shouldNotConvertNorSaveIfNoCookieReturnedByCookieManager()
    {
        cookieSteps.saveCookieAsJson(NAME, Set.of(VariableScope.STEP), NAME);
        verifyNoInteractions(jsonUtils, variableContext);
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
    void shouldSetAllCookies()
    {
        testSetAllCookies(cookieSteps::setAllCookies);
        verify(navigateActions).refresh();
    }

    @Test
    void shoutSetAllCookiesWithoutApplyingChanges()
    {
        testSetAllCookies(cookieSteps::setAllCookiesWithoutApply);
        verifyNoInteractions(navigateActions);
    }

    private void testSetAllCookies(Consumer<ExamplesTable> test)
    {
        var testUrl = "https://www.vividus.org";
        WebDriver webDriver = mock();
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(testUrl);
        var tableAsString = """
                |cookieName|cookieValue|path|
                |hcpsid    |1          |/   |
                |hcpsid    |1          |/   |
                """;
        var table = new ExamplesTable(tableAsString);
        test.accept(table);
        verify(cookieManager, times(2)).addCookie("hcpsid", "1", "/", testUrl);
    }

    private void verifyCookieAssertion(Cookie cookie)
    {
        verify(softAssert).assertThat(eq("Cookie with the name 'name' is set"), eq(cookie),
                argThat(m -> "not null".equals(m.toString())));
    }
}
