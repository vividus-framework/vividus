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

package org.vividus.selenium.tunnel;

import java.util.Objects;

public class TunnelOptions
{
    private String proxy;

    public String getProxy()
    {
        return proxy;
    }

    /**
     * Sets the proxy &lt;host:port&gt;.
     * <br>
     * Make sure that the format is the following, if the proxy is running on a local machine: <b>127.0.0.1:port</b>
     * @param proxy Proxy <b>host:port</b>
     */
    public void setProxy(String proxy)
    {
        this.proxy = proxy;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(proxy);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof TunnelOptions))
        {
            return false;
        }
        TunnelOptions other = (TunnelOptions) obj;
        return Objects.equals(proxy, other.proxy);
    }
}
