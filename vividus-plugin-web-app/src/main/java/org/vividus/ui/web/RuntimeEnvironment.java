/*
 * Copyright 2019 the original author or authors.
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

import org.vividus.ui.web.action.IJavascriptActions;

public class RuntimeEnvironment implements IRuntimeEnvironment
{
    private IJavascriptActions javascriptActions;

    private final ThreadLocal<String> userAgent = ThreadLocal.withInitial(() -> javascriptActions.getUserAgent());
    private final ThreadLocal<Double> devicePixelRatio = ThreadLocal.withInitial(
        () -> javascriptActions.getDevicePixelRatio());

    public RuntimeEnvironment(IJavascriptActions javascriptActions)
    {
        this.javascriptActions = javascriptActions;
    }

    @Override
    public String getUserAgent()
    {
        return userAgent.get();
    }

    @Override
    public double getDevicePixelRatio()
    {
        return devicePixelRatio.get();
    }
}
