/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.http.expression;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.jbehave.core.expressions.SingleArgExpressionProcessor;

class PkceExpressionProcessor extends SingleArgExpressionProcessor<String>
{
    PkceExpressionProcessor()
    {
        super("generateCodeChallenge", PkceExpressionProcessor::generateCodeChallenge);
    }

    /**
     * Generates code challenge by provided code verifier for OAuth with PKCE.
     * @param codeVerifier value to generate code challenge
     * @return code challenge generated value
     */
    private static String generateCodeChallenge(String codeVerifier)
    {
        byte[] bytes = codeVerifier.getBytes(StandardCharsets.UTF_8);
        MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalArgumentException(e);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest(bytes));
    }
}
