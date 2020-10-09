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

package org.vividus.ssh;

public class ServerConfiguration
{
    private String username;
    private String password;
    private String host;
    private int port;
    private boolean agentForwarding;
    private boolean pseudoTerminalEnabled;
    private String privateKey;
    private String publicKey;
    private String passphrase;

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

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public boolean isAgentForwarding()
    {
        return agentForwarding;
    }

    public void setAgentForwarding(boolean agentForwarding)
    {
        this.agentForwarding = agentForwarding;
    }

    public boolean isPseudoTerminalEnabled()
    {
        return pseudoTerminalEnabled;
    }

    public void setPseudoTerminalEnabled(boolean pseudoTerminalEnabled)
    {
        this.pseudoTerminalEnabled = pseudoTerminalEnabled;
    }

    public String getPrivateKey()
    {
        return privateKey;
    }

    public void setPrivateKey(String privateKey)
    {
        this.privateKey = privateKey;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public void setPublicKey(String publicKey)
    {
        this.publicKey = publicKey;
    }

    public String getPassphrase()
    {
        return passphrase;
    }

    public void setPassphrase(String passphrase)
    {
        this.passphrase = passphrase;
    }
}
