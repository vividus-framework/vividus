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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

import org.jbehave.core.expressions.ExpressionProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PkceExpressionProcessorTests
{
    private static final String GENERATE_CODE_CHALLENGE = "generateCodeChallenge(%s)";
    private static final String CODE_VERIFIER_VALUE = "71j2608810pl00bs2351909y7uptc8z6lm7g3o8yl31";
    private static final String ALGORITHM = "SHA-256";

    @InjectMocks
    private PkceExpressionProcessor expressionProcessor;

    @Test
    void testExecute() throws NoSuchAlgorithmException
    {
        byte[] bytes = CODE_VERIFIER_VALUE.getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        String expectedValue = Base64.getUrlEncoder().withoutPadding().encodeToString(md.digest(bytes));
        ExpressionProcessor<String> processor = new PkceExpressionProcessor();
        assertEquals(Optional.of(expectedValue),
                processor.execute(String.format(GENERATE_CODE_CHALLENGE, CODE_VERIFIER_VALUE)));
    }

    @Test
    void testExecuteWithException()
    {
        try (MockedStatic<MessageDigest> messageDigestStaticMock = mockStatic(MessageDigest.class))
        {
            messageDigestStaticMock.when(() -> MessageDigest.getInstance(ALGORITHM)).thenThrow(
                    new NoSuchAlgorithmException());
            assertThrows(IllegalArgumentException.class, () ->
                    expressionProcessor.execute(String.format(GENERATE_CODE_CHALLENGE, CODE_VERIFIER_VALUE)));
        }
    }
}
