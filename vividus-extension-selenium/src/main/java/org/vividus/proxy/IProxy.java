/*
 * Copyright 2019-2020 the original author or authors.
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

import java.net.InetAddress;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.filters.RequestFilter;

public interface IProxy
{
    void start();

    void start(int port, InetAddress address);

    void startRecording();

    void stopRecording();

    void stop();

    boolean isStarted();

    BrowserUpProxy getProxyServer();

    ProxyLog getLog();

    void addRequestFilter(RequestFilter requestFilter);

    void clearRequestFilters();
}
