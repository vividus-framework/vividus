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

package org.vividus.aws.s3.steps;

import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.util.ResourceUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ S3BucketSteps.class, ObjectMetadata.class, IOUtils.class, ResourceUtils.class,
        ByteArrayInputStream.class })
@PowerMockIgnore("javax.management.*")
public class S3BucketStepsTests
{
    private static final String CSV_FILE_PATH = "/test.csv";
    private static final String S3_BUCKET_NAME = "bucketName";
    private static final String S3_OBJECT_KEY = "objectKey";

    @Mock
    private AmazonS3Client amazonS3Client;

    @Mock
    private IBddVariableContext bddVariableContext;

    @InjectMocks
    private S3BucketSteps steps;

    @Test
    public void uploadFileTest() throws Exception
    {
        String contentType = "contentType";
        byte[] resource = "string".getBytes(StandardCharsets.UTF_8);

        ObjectMetadata objectMetadata = Mockito.mock(ObjectMetadata.class);
        ByteArrayInputStream inputStream = Mockito.mock(ByteArrayInputStream.class);
        PowerMockito.mockStatic(ResourceUtils.class);
        PowerMockito.when(ResourceUtils.loadResourceAsByteArray(CSV_FILE_PATH)).thenReturn(resource);
        PowerMockito.whenNew(ObjectMetadata.class).withNoArguments().thenReturn(objectMetadata);
        PowerMockito.whenNew(ByteArrayInputStream.class).withArguments(resource).thenReturn(inputStream);

        steps.uploadResource(CSV_FILE_PATH, S3_OBJECT_KEY, contentType, S3_BUCKET_NAME);
        verify(objectMetadata).setContentType(contentType);
        verify(objectMetadata).setContentLength(resource.length);
        verify(amazonS3Client).putObject(S3_BUCKET_NAME, S3_OBJECT_KEY, new ByteArrayInputStream(resource),
                objectMetadata);
    }

    @Test
    public void fetchCsvFileTest() throws IOException
    {
        String objectKey = S3_OBJECT_KEY + ".csv";
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        String variableName = "varName";

        S3Object s3Object = Mockito.mock(S3Object.class);
        S3ObjectInputStream s3ObjectInputStream = Mockito.mock(S3ObjectInputStream.class);
        String csvString = ResourceUtils.loadResource(this.getClass(), CSV_FILE_PATH);

        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.when(IOUtils.toString(s3ObjectInputStream, StandardCharsets.UTF_8)).thenReturn(csvString);
        Mockito.when(amazonS3Client.getObject(S3_BUCKET_NAME, objectKey)).thenReturn(s3Object);
        Mockito.when(s3Object.getObjectContent()).thenReturn(s3ObjectInputStream);

        List<Map<String, String>> expectedCsv = List.of(Map.of("id", "1"));

        steps.fetchCsvObject(objectKey, S3_BUCKET_NAME, scopes, variableName);
        verify(amazonS3Client).getObject(S3_BUCKET_NAME, objectKey);
        verify(bddVariableContext).putVariable(scopes, variableName, expectedCsv);
    }

    @Test
    public void deleteFileTest()
    {
        steps.deleteObject(S3_OBJECT_KEY, S3_BUCKET_NAME);
        verify(amazonS3Client).deleteObject(S3_BUCKET_NAME, S3_OBJECT_KEY);
    }
}
