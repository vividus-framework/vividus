package org.vividus.selenium;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.browserup.bup.client.ClientUtil;

import org.openqa.selenium.Proxy;
import org.vividus.proxy.IProxy;

public class ProxyStarter
{
    private final IProxy proxy;

    public ProxyStarter(IProxy proxy)
    {
        this.proxy = proxy;
    }

    public Proxy createSeleniumProxy(boolean remoteExecution)
    {
        try
        {
            return ClientUtil.createSeleniumProxy(proxy.getProxyServer(),
                    remoteExecution ? InetAddress.getLocalHost() : InetAddress.getLoopbackAddress());
        }
        catch (UnknownHostException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
