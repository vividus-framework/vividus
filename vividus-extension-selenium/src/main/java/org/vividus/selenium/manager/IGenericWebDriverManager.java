/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.selenium.manager;

import java.util.function.Consumer;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

public interface IGenericWebDriverManager
{
    String NATIVE_APP_CONTEXT = "NATIVE_APP";

    Dimension getSize();

    /**
     * Runs the given action in the mobile native context. If the current context is not the mobile native context, it
     * switches to the mobile native context, runs the action, and then switches back to the original context.
     *
     * @param consumer the consumer to be executed, accepts the {@link WebDriver} instance as an argument.
     */
    void performActionInNativeContext(Consumer<WebDriver> consumer);

    boolean isContextSwitchedToMobileNative();

    boolean isMobile();

    boolean isIOS();

    boolean isTvOS();

    boolean isAndroid();

    boolean isMacOs();

    Capabilities getCapabilities();
}
