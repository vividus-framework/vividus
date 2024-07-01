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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Set;

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
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.ui.web.playwright.action.PlaywrightCookieManager;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class CookieStepsTests
{
    private static final String NAME = "name";
    private static final String COOKIE_WITH_NAME_TEXT = "Cookie with the name that is equal to '";
    private static final String DYNAMIC_COOKIE_NAME = "SSESSf4342sds23e3t5fs";
    @Mock private Cookie cookie;
    @Mock private PlaywrightCookieManager cookieManager;
    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;
    @Mock private WebApplicationConfiguration webApplicationConfiguration;
    @Mock private JsonUtils jsonUtils;
    @InjectMocks private CookieSteps cookieSteps;

    @Test
    void shouldValidateThatCookieWithNameMatchingComparisonRuleIsSet()
    {
        Cookie pwCookie = new Cookie(DYNAMIC_COOKIE_NAME, StringUtils.EMPTY);
        when(cookieManager.getCookies()).thenReturn(List.of(pwCookie));
        cookieSteps.thenCookieWithMatchingNameIsSet(StringComparisonRule.MATCHES, "SSESS.*");
        verify(softAssert).assertTrue("Cookie with the name that matches 'SSESS.*' is set", true);
    }

    @Test
    void shouldValidateThatCookieWithNameMatchingComparisonRuleIsNotSet()
    {
        Cookie pwCookie = new Cookie(DYNAMIC_COOKIE_NAME, StringUtils.EMPTY);
        when(cookieManager.getCookies()).thenReturn(List.of(pwCookie));
        cookieSteps.thenCookieWithMatchingNameIsNotSet(StringComparisonRule.CONTAINS, "_ga");
        verify(softAssert).assertTrue("Cookie with the name that contains '_ga' is not set", true);
    }

    @Test
    void testSetAllCookies()
    {
        String testUrl = "https://www.vividus.org";
        URI uri = mock();
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(uri);
        when(uri.toString()).thenReturn(testUrl);

        String tableAsString = "|cookieName|cookieValue|path|\n|hcpsid|1|/|\n|hcpsid|1|/|";
        ExamplesTable table = new ExamplesTable(tableAsString);
        cookieSteps.setAllCookies(table);
        verify(cookieManager, times(2)).addCookie("hcpsid", "1", "/", testUrl);
    }

    @Test
    void shouldSaveCookieValueIntoVariableContext()
    {
        String value = "value";
        Cookie newCookie = new Cookie(NAME, value);
        when(cookieManager.getCookies()).thenReturn(List.of(newCookie));
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);

        cookieSteps.saveCookieIntoVariable(NAME, scopes, NAME);
        verify(variableContext).putVariable(scopes, NAME, value);
        verifyCookieAssertion(true);
    }

    @Test
    void shouldConvertCookieToJsonAndSaveItToContext()
    {
        when(cookieManager.getCookie(NAME)).thenReturn(cookie);
        String cookieAsJson = "{\"path\": \"/index\"}";
        when(jsonUtils.toJson(cookie)).thenReturn(cookieAsJson);
        Set<VariableScope> scopes = Set.of(VariableScope.STEP);

        cookieSteps.saveCookieAsJson(NAME, scopes, NAME);
        verify(variableContext).putVariable(scopes, NAME, cookieAsJson);
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
        cookieSteps.removeCookie(NAME);
        var ordered = inOrder(cookieManager);
        ordered.verify(cookieManager).deleteCookie(NAME);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldThrowAnExceptionIfCurrentURLIsNotDefined()
    {
        ExamplesTable table = mock();
        URI uri = mock();
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(uri);
        when(uri.toString()).thenReturn(null);
        IllegalArgumentException iae =
                assertThrows(IllegalArgumentException.class, () -> cookieSteps.setAllCookies(table));
        assertEquals("Unable to get current URL. Please make sure you've navigated to the right URL", iae.getMessage());
    }

    @Test
    void shouldNotSaveNotExistingCookieAndRecordAssertion()
    {
        cookieSteps.saveCookieIntoVariable(NAME, Set.of(VariableScope.SCENARIO), NAME);
        verifyNoInteractions(variableContext);
        verifyCookieAssertion(false);
    }

    @Test
    void shouldNotConvertNorSaveIfNoCookieReturnedByCookieHelper()
    {
        cookieSteps.saveCookieAsJson(NAME, Set.of(VariableScope.STEP), NAME);
        verifyNoInteractions(jsonUtils, variableContext);
    }

    private void verifyCookieAssertion(boolean assertionPass)
    {
        verify(softAssert).assertTrue(COOKIE_WITH_NAME_TEXT + NAME + "' is set", assertionPass);
    }
}
