/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.ui.web.storage;

import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.html5.WebStorage;
import org.vividus.ui.web.action.WebJavascriptActions;

class JavascriptWebStorage implements WebStorage
{
    private final LocalStorage localStorage;
    private final SessionStorage sessionStorage;

    JavascriptWebStorage(WebJavascriptActions javascriptActions)
    {
        this.localStorage = new JavascriptStorage(StorageType.LOCAL, javascriptActions);
        this.sessionStorage = new JavascriptStorage(StorageType.SESSION, javascriptActions);
    }

    @Override
    public LocalStorage getLocalStorage()
    {
        return this.localStorage;
    }

    @Override
    public SessionStorage getSessionStorage()
    {
        return this.sessionStorage;
    }
}
