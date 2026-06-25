/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.action.CookieManager;
import org.vividus.ui.web.action.NavigateActions;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

@SuppressWarnings("PMD.UnnecessaryBooleanAssertion")
@ExtendWith(MockitoExtension.class)
class CookieStepsTests
{
    private static final String COOKIE_NAME = "SSESSf4342sds23e3t5fs";
    private static final String VARIABLE_NAME = "var";

    @Mock private CookieManager<Cookie> cookieManager;
    @Mock private NavigateActions navigateActions;
    @Mock private VariableContext variableContext;
    @Mock private JsonUtils jsonUtils;
    @Mock private ISoftAssert softAssert;
    private CookieSteps<Cookie> cookieSteps;

    @BeforeEach
    void beforeEach()
    {
        cookieSteps = new CookieSteps<>(cookieManager, Cookie::name, cookie -> cookie, navigateActions, variableContext,
                jsonUtils, softAssert);
    }

    @Test
    void shouldAssertThatCookieWithNameMatchingComparisonRuleIsSet()
    {
        var cookie = new Cookie(COOKIE_NAME);
        when(cookieManager.getCookies()).thenReturn(List.of(cookie));
        cookieSteps.assertCookieIsSet(StringComparisonRule.MATCHES, "SSESS.*");
        verify(softAssert).assertTrue("Cookie with the name that matches 'SSESS.*' is set", true);
    }

    @Test
    void shouldAssertThatCookieWithNameMatchingComparisonRuleIsNotSet()
    {
        var cookie = new Cookie(COOKIE_NAME);
        when(cookieManager.getCookies()).thenReturn(List.of(cookie));
        cookieSteps.assertCookieIsNotSet(StringComparisonRule.CONTAINS, "_ga");
        verify(softAssert).assertTrue("Cookie with the name that contains '_ga' is not set", true);
    }

    @Test
    void shouldConvertCookieToJsonAndSaveItToContext()
    {
        Cookie cookie = mock();
        when(cookieManager.getCookie(COOKIE_NAME)).thenReturn(cookie);
        var cookieAsJson = "{\"path\": \"/index\"}";
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
    void shouldThrowAnExceptionIfCurrentUrlIsNotDefined()
    {
        ExamplesTable table = mock();
        when(navigateActions.getCurrentUrl()).thenReturn(null);
        var iae = assertThrows(IllegalArgumentException.class, () -> cookieSteps.setAllCookies(table));
        assertEquals("Current page URL is not provided. Please make sure to navigate to the correct page",
                iae.getMessage());
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
        verifyNoMoreInteractions(navigateActions);
    }

    private void testSetAllCookies(Consumer<ExamplesTable> test)
    {
        var testUrl = "https://www.vividus.org";
        when(navigateActions.getCurrentUrl()).thenReturn(testUrl);
        var tableAsString = """
                |cookieName|cookieValue|path|
                |c1        |1          |/   |
                |c1        |1          |/   |
                """;
        var table = new ExamplesTable(tableAsString);
        test.accept(table);
        verify(cookieManager, times(2)).addCookie("c1", "1", "/", testUrl);
    }

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
        cookieSteps.removeCookie(COOKIE_NAME);
        var ordered = inOrder(cookieManager, navigateActions);
        ordered.verify(cookieManager).deleteCookie(COOKIE_NAME);
        ordered.verify(navigateActions).refresh();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldRemoveCookieWithoutApplyingChanges()
    {
        cookieSteps.removeCookieWithoutApply(COOKIE_NAME);
        var ordered = inOrder(cookieManager, navigateActions);
        ordered.verify(cookieManager).deleteCookie(COOKIE_NAME);
        ordered.verifyNoMoreInteractions();
    }

    record Cookie(String name)
    {
    }
}
