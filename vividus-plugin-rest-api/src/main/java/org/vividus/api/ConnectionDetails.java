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

package org.vividus.api;

public class ConnectionDetails
{
    private boolean secure;
    private String securityProtocol;

    public boolean isSecure()
    {
        return secure;
    }

    public String getSecurityProtocol()
    {
        return securityProtocol;
    }

    public void setSecure(boolean secure)
    {
        this.secure = secure;
    }

    public void setSecurityProtocol(String securityProtocol)
    {
        this.securityProtocol = securityProtocol;
    }
}
