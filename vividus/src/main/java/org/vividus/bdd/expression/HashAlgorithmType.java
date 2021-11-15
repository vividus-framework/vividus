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

import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apache.commons.codec.digest.DigestUtils;

public enum HashAlgorithmType
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
