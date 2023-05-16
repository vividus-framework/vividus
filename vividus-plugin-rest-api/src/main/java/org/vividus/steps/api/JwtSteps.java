/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.steps.api;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

public class JwtSteps
{
    private static final String HS_256 = "HmacSHA256";
    private static final String JWT_FORMAT = "%s.%s";

    private final VariableContext variableContext;

    public JwtSteps(VariableContext variableContext)
    {
        this.variableContext = variableContext;
    }

    /**
     * Generates JSON Web Token (JWT) with header and payload signed with secret key using HmacSHA256 algorithm and
     * saves the result to scope variable with the specified name.
     * @param header       The header of JWT
     * @param payload      The payload of JWT
     * @param key          Secret key used by HS256 algorithm to sign the token
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The name of variable to save the result
     * @throws NoSuchAlgorithmException If specified algorithm is not available
     * @throws InvalidKeyException In case the secret key is inappropriate for initializing MAC
     */
    @When("I generate JWT with header `$header` and payload `$payload` signed with key `$key` using HS256 algorithm "
            + "and save result to $scopes variable `$variableName`")
    public void generateJwtAndSaveResult(String header, String payload, String key, Set<VariableScope> scopes,
            String variableName) throws NoSuchAlgorithmException, InvalidKeyException
    {
        Mac sha256Hmac = Mac.getInstance(HS_256);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(UTF_8), HS_256);
        sha256Hmac.init(secretKey);
        String encodedHeader = urlEncodeToBase64(header.getBytes(UTF_8));
        String encodedPayload = urlEncodeToBase64(payload.getBytes(UTF_8));
        String jwtData = format(JWT_FORMAT, encodedHeader, encodedPayload);
        String encodedSignature = urlEncodeToBase64(sha256Hmac.doFinal(jwtData.getBytes(UTF_8)));
        variableContext.putVariable(scopes, variableName, format(JWT_FORMAT, jwtData, encodedSignature));
    }

    private String urlEncodeToBase64(byte[] input)
    {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
    }
}
