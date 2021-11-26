/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.mobileapp.configuration;

public class MobileEnvironment
{
    private final boolean realDevice;
    private final String webDriverAgentBundleId;

    public MobileEnvironment(boolean realDevice, String webDriverAgentBundleId)
    {
        this.realDevice = realDevice;
        this.webDriverAgentBundleId = webDriverAgentBundleId;
    }

    public boolean isRealDevice()
    {
        return realDevice;
    }

    public String getWebDriverAgentBundleId()
    {
        return webDriverAgentBundleId;
    }
}
