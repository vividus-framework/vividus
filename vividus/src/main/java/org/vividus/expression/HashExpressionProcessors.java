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

package org.vividus.expression;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;
import org.jbehave.core.expressions.DelegatingExpressionProcessor;
import org.jbehave.core.expressions.RelaxedMultiArgExpressionProcessor;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.vividus.util.ResourceUtils;
import org.apache.commons.codec.binary.Hex;

public final class HashExpressionProcessors extends DelegatingExpressionProcessor
{
    public HashExpressionProcessors(FluentEnumConverter fluentEnumConverter)
    {
        super(List.of(
                new RelaxedMultiArgExpressionProcessor<>("calculateHash", 2, args -> {
                    String algorithm = args.get(0);
                    String input = args.get(1);
                    if (algorithm.toUpperCase().startsWith("HMAC")) {
                        throw new IllegalArgumentException("HMAC requires a key. Use calculateHash(key, algorithm, input)");
                    }
                    HashAlgorithmType hashAlgorithmType = convert(fluentEnumConverter, algorithm);
                    return hashAlgorithmType.getHash(input);
                }),
                new RelaxedMultiArgExpressionProcessor<>("calculateHash", 3, args -> {
                    String key = args.get(0);
                    String algorithm = args.get(1);
                    String input = args.get(2);
                    if (algorithm.toUpperCase().startsWith("HMAC")) {
                        return calculateHmac(algorithm, key, input);
                    } else {
                        // If key is provided but not needed, ignore it and fallback to normal hash
                        HashAlgorithmType hashAlgorithmType = convert(fluentEnumConverter, algorithm);
                        return hashAlgorithmType.getHash(input);
                    }
                }),
                new RelaxedMultiArgExpressionProcessor<>("calculateFileHash", 2, args -> {
                    HashAlgorithmType hashAlgorithmType = convert(fluentEnumConverter, args.get(0));
                    try
                    {
                        return hashAlgorithmType.getHash(ResourceUtils.loadResourceOrFileAsByteArray(args.get(1)));
                    }
                    catch (IOException e)
                    {
                        throw new UncheckedIOException(e);
                    }
                })
        ));
    }
    
    private static String calculateHmac(String algorithm, String key, String data) {
        try {
            String normalized = algorithm.trim().toUpperCase(Locale.ROOT);
            if (!normalized.startsWith("HMAC")) {
                throw new IllegalArgumentException("Unsupported HMAC algorithm: " + algorithm);
            }
            String digestPart = normalized.substring(4);
            if (digestPart.startsWith("-")) {
                digestPart = digestPart.substring(1);
            }
            if (digestPart.isEmpty()) {
                throw new IllegalArgumentException("Unsupported HMAC algorithm: " + algorithm);
            }
            String macAlgorithm = digestPart.startsWith("SHA3")
                    ? "Hmac" + digestPart
                    : "Hmac" + digestPart.replace("-", "");
            Mac mac = Mac.getInstance(macAlgorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), macAlgorithm);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC: " + e.getMessage(), e);
    }
    }

    private static HashAlgorithmType convert(FluentEnumConverter fluentEnumConverter, String algorithm)
    {
        return (HashAlgorithmType) fluentEnumConverter.convertValue(algorithm.replace("-", ""),
                HashAlgorithmType.class);
    }

    private enum HashAlgorithmType
    {
        MD2(DigestUtils::md2Hex, DigestUtils::md2Hex),
        MD5(DigestUtils::md5Hex, DigestUtils::md5Hex),
        SHA1(DigestUtils::sha1Hex, DigestUtils::sha1Hex),
        SHA256(DigestUtils::sha256Hex, DigestUtils::sha256Hex),
        SHA384(DigestUtils::sha384Hex, DigestUtils::sha384Hex),
        SHA512(DigestUtils::sha512Hex, DigestUtils::sha512Hex);

        private final UnaryOperator<String> hashFactory;
        private final Function<byte[], String> fileHashFactory;

        HashAlgorithmType(UnaryOperator<String> hashFactory, Function<byte[], String> fileHashFactory)
        {
            this.hashFactory = hashFactory;
            this.fileHashFactory = fileHashFactory;
        }

        public String getHash(String data)
        {
            return hashFactory.apply(data);
        }

        public String getHash(byte[] data)
        {
            return fileHashFactory.apply(data);
        }
    }
}
