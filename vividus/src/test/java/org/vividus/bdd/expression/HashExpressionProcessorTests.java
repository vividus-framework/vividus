/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.bdd.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.bdd.converter.FluentTrimmedEnumConverter;

public class HashExpressionProcessorTests
{
    private final HashExpressionProcessor processor = new HashExpressionProcessor(new FluentTrimmedEnumConverter());

    @ParameterizedTest
    @CsvSource({
            // CHECKSTYLE:OFF
            "'calculateHash(SHA-1, a_tylmarande@gmail.com)', 025623becec96e8be6b88a7db6e0dc97fa0033ba",
            "'calculateHash(Sha-256, a_tylmarande@gmail.com)', b4e73a7c815bb0eff534ba8ef5f7dbfe9d4f51f449cd9b2ba87fc57ee9de5fc7",
            "'calculateHash(md2, a_tylmarande@gmail.com)', 69ccf551ef37e2cf0181c97fcb7db030",
            "'calculateHash(MD5, a_tylmarande@gmail.com)', 7b2378863e837c51c83bb04e66a5876d",
            "'calculateHash(SHA-384, a_tylmarande@gmail.com)', 397bf1d877dead00a0e91cf27a2b561eff0736fbc05640580c2e541a42f99a61cc7bdd4f3fdc6c1ef7a614e6cac06415",
            "'calculateHash(Sha512, a_tylmarande@gmail.com)', b9b06852f7f209e03d0704ce6fa95283f65f7e4558b043b951bb503f23b4521afb21c1a51576dc11965f6bca91e5ed25dc30da765d803b3d4ff811ddfc4e0b5d"
            // CHECKSTYLE:ON
    })
    void checkHash(String expression, String expected)
    {
        assertEquals(processor.execute(expression), Optional.of(expected));
    }
}
