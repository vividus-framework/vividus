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

package org.vividus.bdd.steps.ssl;

import java.io.IOException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.jbehave.core.annotations.Then;
import org.vividus.bdd.steps.CollectionComparisonRule;
import org.vividus.http.client.ISslContextFactory;
import org.vividus.softassert.ISoftAssert;

public class SslSteps
{
    private static final int SSL_PORT = 443;

    private final ISoftAssert softAssert;
    private final ISslContextFactory sslContextFactory;

    public SslSteps(ISoftAssert softAssert, ISslContextFactory sslContextFactory)
    {
        this.softAssert = softAssert;
        this.sslContextFactory = sslContextFactory;
    }

    /**
     * Checks that a server defined by the <b>hostname</b> supports secure protocols defined by the
     * <b>protocols</b> parameter.
     *
     * @param hostname server host e.g. example.com
     * @param rule one of the CONTAIN, ARE_EQUAL_TO, ARE_EQUAL_TO_ORDERED_COLLECTION rules
     * @param protocols comma separated list of protocols
     * @throws IOException If an input or output exception occurred
     */
    @Then("server `$hostname` supports secure protocols that $rule `$protocols`")
    public void checkSupportedSecureProtocols(String hostname, CollectionComparisonRule rule, String[] protocols)
            throws IOException
    {
        SSLSocketFactory socketFactory = sslContextFactory.getDefaultSslContext().getSocketFactory();
        try (SSLSocket socket = (SSLSocket) socketFactory.createSocket(hostname, SSL_PORT))
        {
            softAssert.assertThat("Enabled secure protocols", socket.getEnabledProtocols(),
                    rule.getComparisonRule(protocols));
        }
    }
}
