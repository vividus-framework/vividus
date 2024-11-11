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

package org.vividus.ui.web.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;

@ExtendWith(MockitoExtension.class)
class CdpActionsTests
{
    private static final String CDP_COMMAND = "command";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWebDriverManager webDriverManager;
    @Mock private HasCdp havingCdpDriver;

    @InjectMocks private CdpActions cdpActions;

    @Test
    void shouldExecuteCdpCommand()
    {
        String param = "param";
        String value = "value";

        when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(true);
        when(webDriverProvider.getUnwrapped(HasCdp.class)).thenReturn(havingCdpDriver);

        cdpActions.executeCdpCommand(CDP_COMMAND, Map.of(param, value));

        verify(havingCdpDriver).executeCdpCommand(CDP_COMMAND, Map.of(param, value));
    }

    @Test
    void shouldFailIfBrowserIsNotChrome()
    {
        Map<String, Object> params = new HashMap<>();
        when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(false);

        var exception = assertThrows(IllegalArgumentException.class,
                () -> cdpActions.executeCdpCommand(CDP_COMMAND, params));
        assertEquals("The step is only supported by Chrome browser.", exception.getMessage());
    }
}
