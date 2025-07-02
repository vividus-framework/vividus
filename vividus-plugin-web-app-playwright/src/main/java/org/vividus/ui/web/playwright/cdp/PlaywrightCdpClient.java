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

package org.vividus.ui.web.playwright.cdp;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Page;

import org.vividus.ui.web.cdp.CdpClient;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;

public class PlaywrightCdpClient implements CdpClient
{
    private static final Gson GSON = new Gson();

    private final BrowserContextProvider browserContextProvider;
    private final UiContext uiContext;

    public PlaywrightCdpClient(BrowserContextProvider browserContextProvider, UiContext uiContext)
    {
        this.browserContextProvider = browserContextProvider;
        this.uiContext = uiContext;
    }

    @Override
    public void executeCdpCommand(String command)
    {
        Page page = uiContext.getCurrentPage();
        browserContextProvider.getCdpSession(page).send(command);
    }

    @Override
    public void executeCdpCommand(String command, String argsAsJsonString)
    {
        JsonObject args = GSON.fromJson(argsAsJsonString, JsonObject.class);
        Page page = uiContext.getCurrentPage();
        browserContextProvider.getCdpSession(page).send(command, args);
    }

    @Override
    public void executeCdpCommand(String command, Map<String, Object> parameters)
    {
        JsonObject args = GSON.toJsonTree(parameters).getAsJsonObject();
        Page page = uiContext.getCurrentPage();
        browserContextProvider.getCdpSession(page).send(command, args);
    }
}
