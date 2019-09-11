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

package org.vividus.http.client;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

public class SocketFactoryPoolingHttpClientConnectionManager extends PoolingHttpClientConnectionManager
{
    public SocketFactoryPoolingHttpClientConnectionManager(ISslContextFactory sslContextFactory,
            boolean sslCertificateCheckEnabled, boolean sslHostnameVerificationEnabled)
    {
        super(getRegistry(sslContextFactory, sslCertificateCheckEnabled, sslHostnameVerificationEnabled));
    }

    private static Registry<ConnectionSocketFactory> getRegistry(ISslContextFactory sslContextFactory,
            boolean sslCertificateCheckEnabled, boolean sslHostnameVerificationEnabled)
    {
        SSLContext sslContext = sslContextFactory
                .getSslContext(SSLConnectionSocketFactory.SSL, !sslCertificateCheckEnabled)
                .orElse(SSLContexts.createDefault());
        HostnameVerifier hostnameVerifier = sslHostnameVerificationEnabled
                ? new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault())
                : NoopHostnameVerifier.INSTANCE;
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", new SSLConnectionSocketFactory(sslContext, hostnameVerifier)).build();
    }
}
