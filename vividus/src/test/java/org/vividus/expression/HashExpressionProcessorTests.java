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

package org.vividus.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.vividus.converter.FluentTrimmedEnumConverter;
import org.vividus.util.ResourceUtils;

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

    @ParameterizedTest
    @CsvSource({
            // CHECKSTYLE:OFF
            "'calculateFileHash(SHA-1, org/vividus/expressions/resource.txt)', 1ae4969aa6712c516be2e62c652760e8abfaee07",
            "'calculateFileHash(Sha-256, org/vividus/expressions/resource.txt)', 8a0a675375c2f15e3789b63a40ffd1963bb11cd0349d8f08f081dcb0bbe489fe",
            "'calculateFileHash(md2, org/vividus/expressions/resource.txt)', 63090590b6d8e241bca791640a96e9fb",
            "'calculateFileHash(MD5, org/vividus/expressions/resource.txt)', 44d6c918d7a7157f7c0905df17b25496",
            "'calculateFileHash(SHA-384, org/vividus/expressions/resource.txt)', ed19ca22a3f70e8deb3c3ecd8e814b3663b042e3f9b70f0eea2bf1baa3b2c3ffecc4ab295cc51266173bab23eb6decc4",
            "'calculateFileHash(Sha512, org/vividus/expressions/resource.txt)', 73e68881ac4f06fe5f62fdf3cf0acc90e830c02f8ffe2d9728ceef40bc4eb62dd235ab13aa5214931141d9d721e16e7bba3e69ac4d8a5f9c12261d9941bb65e1"
            // CHECKSTYLE:ON
    })
    void checkFileHash(String expression, String expected)
    {
        assertEquals(processor.execute(expression), Optional.of(expected));
    }

    @Test
    void shouldThrowUncheckedIOException()
    {
        var resourceOrFilePath = "resourceOrFilePath";
        try (MockedStatic<ResourceUtils> resourceUtilsMockedStatic = mockStatic(ResourceUtils.class))
        {
            var ioException = new IOException();
            resourceUtilsMockedStatic.when(() -> ResourceUtils.loadResourceOrFileAsByteArray(resourceOrFilePath))
                    .thenThrow(ioException);
            var uncheckedIOException = assertThrows(UncheckedIOException.class,
                    () -> processor.execute(String.format("calculateFileHash(SHA-1, %s)", resourceOrFilePath)));
            assertEquals(ioException, uncheckedIOException.getCause());
        }
    }
}
