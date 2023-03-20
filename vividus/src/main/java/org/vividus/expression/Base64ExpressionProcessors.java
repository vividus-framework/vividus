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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.jbehave.core.expressions.DelegatingExpressionProcessor;
import org.jbehave.core.expressions.SingleArgExpressionProcessor;
import org.vividus.util.ResourceUtils;

public class Base64ExpressionProcessors extends DelegatingExpressionProcessor
{
    public Base64ExpressionProcessors()
    {
        // CHECKSTYLE:OFF
        super(List.of(
            new SingleArgExpressionProcessor<>("resourceToBase64",         s -> encodeToBase64(
                    ResourceUtils.loadResourceAsByteArray(s))),
            new SingleArgExpressionProcessor<>("decodeFromBase64",         s -> new String(decode(s), UTF_8)),
            new SingleArgExpressionProcessor<>("encodeToBase64",           s -> encodeToBase64(s.getBytes(UTF_8))),
            new SingleArgExpressionProcessor<>("toBase64Gzip",             Base64ExpressionProcessors::toBase64Gzip),
            new SingleArgExpressionProcessor<>("decodeFromBase64ToBinary", Base64ExpressionProcessors::decode)
        ));
        // CHECKSTYLE:ON
    }

    private static byte[] decode(String input)
    {
        return Base64.getDecoder().decode(input.getBytes(UTF_8));
    }

    private static String encodeToBase64(byte[] input)
    {
        byte[] encodedInput = Base64.getEncoder().encode(input);
        return new String(encodedInput, UTF_8);
    }

    private static String toBase64Gzip(String input)
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            try (GZIPOutputStream gzipos = new GZIPOutputStream(baos))
            {
                gzipos.write(input.getBytes(UTF_8));
            }
            return encodeToBase64(baos.toByteArray());
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
