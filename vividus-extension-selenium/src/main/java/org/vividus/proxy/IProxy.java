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

package org.vividus.proxy;

import org.openqa.selenium.Proxy;

import de.sstoehr.harreader.model.Har;

public interface IProxy
{
    void start();

    void startRecording();

    /**
     * Retrieves the current HAR.
     *
     * @return current HAR, or null if proxy recording was not enabled
     */
    Har getRecordedData();

    void clearRecordedData();

    void stopRecording();

    void stop();

    boolean isStarted();

    void addMock(ProxyMock proxyMock);

    void clearMocks();

    Proxy createSeleniumProxy();
}
