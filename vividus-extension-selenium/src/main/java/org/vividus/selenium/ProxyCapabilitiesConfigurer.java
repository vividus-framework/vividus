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
