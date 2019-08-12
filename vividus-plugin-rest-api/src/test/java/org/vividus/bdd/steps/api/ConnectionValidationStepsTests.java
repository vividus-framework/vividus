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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.api.ConnectionDetails;
import org.vividus.api.IApiTestContext;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class ConnectionValidationStepsTests
{
    private static final String TLS_V1_2 = "TLSv1.2";
    private static final String CONNECTION_SECURE_ASSERTION = "Connection is secure";

    @Mock
    private IApiTestContext apiTestContext;

    @Mock
    private ISoftAssert softAssert;

    @InjectMocks
    private ConnectionValidationSteps steps;

    @Test
    void shouldValidateSecuredConnection()
    {
        boolean secure = true;
        String actualProtocol = "TLSv1.3";
        ConnectionDetails connectionDetails = new ConnectionDetails();
        connectionDetails.setSecure(secure);
        connectionDetails.setSecurityProtocol(actualProtocol);

        when(apiTestContext.getConnectionDetails()).thenReturn(connectionDetails);
        when(softAssert.assertTrue(CONNECTION_SECURE_ASSERTION, secure)).thenReturn(Boolean.TRUE);
        steps.isConnectionSecured(TLS_V1_2);
        verify(softAssert).assertEquals("Security protocol", TLS_V1_2, actualProtocol);
    }

    @Test
    void shouldValidateNonSecuredConnection()
    {
        boolean secure = false;
        ConnectionDetails connectionDetails = new ConnectionDetails();
        connectionDetails.setSecure(secure);

        when(apiTestContext.getConnectionDetails()).thenReturn(connectionDetails);
        when(softAssert.assertTrue(CONNECTION_SECURE_ASSERTION, secure)).thenReturn(Boolean.FALSE);
        steps.isConnectionSecured(TLS_V1_2);
        verifyNoMoreInteractions(softAssert);
    }
}
