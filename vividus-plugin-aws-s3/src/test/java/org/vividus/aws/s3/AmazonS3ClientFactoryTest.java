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

package org.vividus.aws.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.s3.AmazonS3;

import org.junit.jupiter.api.Test;

public class AmazonS3ClientFactoryTest
{
    @Test
    void shouldCreateNewClientInstance()
    {
        String s3AccessKey = "s3AccessKey";
        String s3SecretKey = "s3SecretKey";
        String region = "us-east-1";
        AmazonS3 client = AmazonS3ClientFactory.create(s3AccessKey, s3SecretKey, region);
        assertEquals(region, client.getRegionName());
    }
}
