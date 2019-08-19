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

package org.vividus.bdd.steps.api;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.vividus.http.ConnectionDetails;
import org.vividus.http.HttpTestContext;
import org.vividus.softassert.ISoftAssert;

public class ConnectionValidationSteps
{
    @Inject private HttpTestContext httpTestContext;
    @Inject private ISoftAssert softAssert;

    /**
     * This step should be preceded with any step executing HTTP request
     * The step validates that HTTP connection was secured with the expected protocol
     * @param securityProtocol expected security protocol, e.g. TLSv1.2
     */
    @Then("the connection is secured using $securityProtocol protocol")
    public void isConnectionSecured(String securityProtocol)
    {
        ConnectionDetails connectionDetails = httpTestContext.getConnectionDetails();
        if (softAssert.assertTrue("Connection is secure", connectionDetails.isSecure()))
        {
            softAssert.assertEquals("Security protocol", securityProtocol,
                    connectionDetails.getSecurityProtocol());
        }
    }
}
