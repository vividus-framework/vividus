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

package org.vividus.ui.web.playwright.steps;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.PlaywrightCookieManager;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class PlaywrightCookieStepsTests
{
    private static final String COOKIE_WITH_NAME_TEXT = "Cookie with the name that is equal to '";
    private static final String COOKIE_NAME = "SSESSf4342sds23e3t5fs";
    private static final String VARIABLE_NAME = "var";

    @Mock private PlaywrightCookieManager cookieManager;
    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;
    @Mock private UiContext uiContext;
    @Mock private JsonUtils jsonUtils;
    @InjectMocks private PlaywrightCookieSteps cookieSteps;

    @Test
    void shouldAssertThatCookieWithNameMatchingComparisonRuleIsSet()
    {
        var cookie = new Cookie(COOKIE_NAME, StringUtils.EMPTY);
        when(cookieManager.getCookies()).thenReturn(List.of(cookie));
        cookieSteps.assertCookieIsSet(StringComparisonRule.MATCHES, "SSESS.*");
        verify(softAssert).assertTrue("Cookie with the name that matches 'SSESS.*' is set", true);
    }

    @Test
    void shouldAssertThatCookieWithNameMatchingComparisonRuleIsNotSet()
    {
        var cookie = new Cookie(COOKIE_NAME, StringUtils.EMPTY);
        when(cookieManager.getCookies()).thenReturn(List.of(cookie));
        cookieSteps.assertCookieIsNotSet(StringComparisonRule.CONTAINS, "_ga");
        verify(softAssert).assertTrue("Cookie with the name that contains '_ga' is not set", true);
    }

    @Test
    void shouldConvertCookieToJsonAndSaveItToContext()
    {
        Cookie cookie = mock();
        when(cookieManager.getCookie(COOKIE_NAME)).thenReturn(cookie);
        String cookieAsJson = "{\"path\": \"/index\"}";
        when(jsonUtils.toJson(cookie)).thenReturn(cookieAsJson);
        Set<VariableScope> scopes = Set.of(VariableScope.STEP);

        cookieSteps.saveCookieAsJson(COOKIE_NAME, scopes, VARIABLE_NAME);
        verify(variableContext).putVariable(scopes, VARIABLE_NAME, cookieAsJson);
    }

    @Test
    void shouldNotConvertNorSaveIfNoCookieReturnedByCookieHelper()
    {
        cookieSteps.saveCookieAsJson(COOKIE_NAME, Set.of(VariableScope.STEP), VARIABLE_NAME);
        verifyNoInteractions(jsonUtils, variableContext);
    }

    @Test
    void testSetAllCookies()
    {
        String testUrl = "https://www.vividus.org";
        Page page = mock();
        when(page.url()).thenReturn(testUrl);
        when(uiContext.getCurrentPage()).thenReturn(page);

        String tableAsString = "|cookieName|cookieValue|path|\n|hcpsid|1|/|\n|hcpsid|1|/|";
        ExamplesTable table = new ExamplesTable(tableAsString);
        cookieSteps.setAllCookies(table);
        verify(cookieManager, times(2)).addCookie("hcpsid", "1", "/", testUrl);
    }

    @Test
    void shouldSaveCookieValueIntoVariableContext()
    {
        String value = "value";
        Cookie newCookie = new Cookie(COOKIE_NAME, value);
        when(cookieManager.getCookies()).thenReturn(List.of(newCookie));
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);

        cookieSteps.saveCookieIntoVariable(COOKIE_NAME, scopes, VARIABLE_NAME);
        verify(variableContext).putVariable(scopes, VARIABLE_NAME, value);
        verifyCookieAssertion(true);
    }

    @Test
    void shouldRemoveAllCookies()
    {
        cookieSteps.removeAllCookies();
        var ordered = inOrder(cookieManager);
        ordered.verify(cookieManager).deleteAllCookies();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldRemoveCookie()
    {
        cookieSteps.removeCookie(COOKIE_NAME);
        var ordered = inOrder(cookieManager);
        ordered.verify(cookieManager).deleteCookie(COOKIE_NAME);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldNotSaveNotExistingCookieAndRecordAssertion()
    {
        cookieSteps.saveCookieIntoVariable(COOKIE_NAME, Set.of(VariableScope.SCENARIO), VARIABLE_NAME);
        verifyNoInteractions(variableContext);
        verifyCookieAssertion(false);
    }

    private void verifyCookieAssertion(boolean assertionPass)
    {
        verify(softAssert).assertTrue(COOKIE_WITH_NAME_TEXT + COOKIE_NAME + "' is set", assertionPass);
    }
}
