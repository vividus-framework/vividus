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

package org.vividus.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.services.s3.model.CannedAccessControlList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CannedAccessControlListConverterTests
{
    private final CannedAccessControlListConverter converter = new CannedAccessControlListConverter();

    @ParameterizedTest
    @CsvSource({
            "private, Private",
            "Public-Read, PublicRead",
            "aws-exec-read, AwsExecRead"
    })
    void shouldConvertSuccessfully(String input, CannedAccessControlList expected)
    {
        assertEquals(expected, converter.convertValue(input, CannedAccessControlList.class));
    }

    @Test
    void shouldThrowExceptionAtInvalidInput()
    {
        String input = "any";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> converter.convertValue(input, CannedAccessControlList.class));
        assertEquals(input + " is not a valid canned access control list", exception.getMessage());
    }
}
