/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.winrm;

import org.jbehave.core.annotations.AsParameters;
import org.jbehave.core.annotations.Parameter;

@AsParameters
public class WinRmConnectionParameters
{
    private String address;
    private String username;
    private String password;

    @Parameter(name = "authentication-scheme")
    private String authenticationScheme;

    @Parameter(name = "disable-certificate-checks")
    private boolean disableCertificateChecks;

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getAuthenticationScheme()
    {
        return authenticationScheme;
    }

    public void setAuthenticationScheme(String authenticationScheme)
    {
        this.authenticationScheme = authenticationScheme;
    }

    public boolean isDisableCertificateChecks()
    {
        return disableCertificateChecks;
    }

    public void setDisableCertificateChecks(boolean disableCertificateChecks)
    {
        this.disableCertificateChecks = disableCertificateChecks;
    }
}
