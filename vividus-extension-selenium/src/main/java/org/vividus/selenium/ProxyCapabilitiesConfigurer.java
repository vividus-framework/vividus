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

package org.vividus.selenium;

import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.proxy.IProxy;
import org.vividus.proxy.SeleniumProxyFactory;

public class ProxyCapabilitiesConfigurer implements DesiredCapabilitiesConfigurer
{
    private final IProxy proxy;
    private final SeleniumProxyFactory seleniumProxyFactory;

    public ProxyCapabilitiesConfigurer(IProxy proxy, SeleniumProxyFactory seleniumProxyFactory)
    {
        this.proxy = proxy;
        this.seleniumProxyFactory = seleniumProxyFactory;
    }

    @Override
    public void addCapabilities(DesiredCapabilities desiredCapabilities)
    {
        if (proxy.isStarted())
        {
            desiredCapabilities.setCapability(CapabilityType.PROXY, seleniumProxyFactory.createSeleniumProxy());
            desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        }
    }
}
