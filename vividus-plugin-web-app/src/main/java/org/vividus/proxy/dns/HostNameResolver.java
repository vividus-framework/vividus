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

package org.vividus.proxy.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lightbody.bmp.proxy.dns.NativeResolver;

public class HostNameResolver extends NativeResolver
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HostNameResolver.class);

    private Map<String, String> dnsMappingStorage;

    @Override
    public Collection<InetAddress> resolve(String host)
    {
        try
        {
            String ipAddress = dnsMappingStorage.get(host);
            if (null != ipAddress)
            {
                return List.of(InetAddress.getByName(ipAddress));
            }
        }
        catch (UnknownHostException e)
        {
            LOGGER.warn("Unable to determine hostname by ip address", e);
        }
        return super.resolve(host);
    }

    public void setDnsMappingStorage(Map<String, String> dnsMappingStorage)
    {
        this.dnsMappingStorage = dnsMappingStorage;
    }
}
