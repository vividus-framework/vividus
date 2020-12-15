/*
 * Copyright 2019-2020 the original author or authors.
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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.function.FailableConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class S3BucketStepsTests
{
    private static final String CONTENT_TYPE = "contentType";
    private static final String CSV_FILE_PATH = "/test.csv";
    private static final String S3_BUCKET_NAME = "bucketName";
    private static final String S3_OBJECT_KEY = "objectKey";

    @Mock private AmazonS3Client amazonS3Client;
    @Mock private IBddVariableContext bddVariableContext;

    @Test
    void shouldUploadData() throws IOException
    {
        String csv = ResourceUtils.loadResource(CSV_FILE_PATH);
        testSteps(steps -> steps.uploadData(csv, S3_OBJECT_KEY, CONTENT_TYPE, S3_BUCKET_NAME));
        verifyContentUploaded(csv.getBytes(StandardCharsets.UTF_8), CONTENT_TYPE);
    }

    @Test
    void shouldUploadResource() throws IOException
    {
        byte[] csv = ResourceUtils.loadResourceAsByteArray(CSV_FILE_PATH);
        testSteps(steps -> steps.uploadResource(CSV_FILE_PATH, S3_OBJECT_KEY, CONTENT_TYPE, S3_BUCKET_NAME));
        verifyContentUploaded(csv, CONTENT_TYPE);
    }

    @Test
    void shouldUploadFile() throws IOException
    {
        byte[] csv = ResourceUtils.loadResourceAsByteArray(CSV_FILE_PATH);
        testSteps(steps -> steps.uploadFile(ResourceUtils.loadFile(getClass(), CSV_FILE_PATH),
                S3_OBJECT_KEY, CONTENT_TYPE, S3_BUCKET_NAME));
        verifyContentUploaded(csv, CONTENT_TYPE);
    }

    private void verifyContentUploaded(byte[] csv, String contentType)
    {
        verify(amazonS3Client).putObject(eq(S3_BUCKET_NAME), eq(S3_OBJECT_KEY), argThat(bais -> {
            try
            {
                byte[] actual = IOUtils.toByteArray(bais);
                bais.reset();
                return Arrays.equals(csv, actual);
            }
            catch (IOException e)
            {
                return false;
            }
        }), argThat(metadata -> metadata.getContentLength() == csv.length && contentType
                .equals(metadata.getContentType())));
    }

    @Test
    void shouldFetchCsvObject() throws IOException
    {
        byte[] csv = ResourceUtils.loadResourceAsByteArray(CSV_FILE_PATH);

        mockGetObject(S3_OBJECT_KEY + ".csv", csv);

        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        String variableName = "csvVar";
        testSteps(steps -> steps.fetchCsvObject(S3_OBJECT_KEY, S3_BUCKET_NAME, scopes, variableName));
        verify(bddVariableContext).putVariable(scopes, variableName, List.of(Map.of("id", "1")));
    }

    @Test
    void shouldFetchObject() throws IOException
    {
        String objectKey = S3_OBJECT_KEY + ".json";
        String data = "data";

        mockGetObject(objectKey, data.getBytes(StandardCharsets.UTF_8));

        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        String variableName = "jsonVar";
        testSteps(steps -> steps.fetchObject(objectKey, S3_BUCKET_NAME, scopes, variableName));
        verify(amazonS3Client).getObject(S3_BUCKET_NAME, objectKey);
        verify(bddVariableContext).putVariable(scopes, variableName, data);
    }

    private void mockGetObject(String objectKey, byte[] data)
    {
        S3ObjectInputStream s3ObjectInputStream = new S3ObjectInputStream(new ByteArrayInputStream(data), null);
        S3Object s3Object = mock(S3Object.class);
        when(s3Object.getObjectContent()).thenReturn(s3ObjectInputStream);
        when(amazonS3Client.getObject(S3_BUCKET_NAME, objectKey)).thenReturn(s3Object);
    }

    @Test
    void shouldSetObjectAcl() throws IOException
    {
        testSteps(steps -> steps.setObjectAcl(CannedAccessControlList.PublicReadWrite, S3_OBJECT_KEY, S3_BUCKET_NAME));
        verify(amazonS3Client).setObjectAcl(S3_BUCKET_NAME, S3_OBJECT_KEY, CannedAccessControlList.PublicReadWrite);
    }

    @Test
    void shouldDeleteObject() throws IOException
    {
        testSteps(steps -> steps.deleteObject(S3_OBJECT_KEY, S3_BUCKET_NAME));
        verify(amazonS3Client).deleteObject(S3_BUCKET_NAME, S3_OBJECT_KEY);
    }

    void testSteps(FailableConsumer<S3BucketSteps, IOException> test) throws IOException
    {
        try (MockedStatic<AmazonS3ClientBuilder> clientBuilder = mockStatic(AmazonS3ClientBuilder.class))
        {
            clientBuilder.when(AmazonS3ClientBuilder::defaultClient).thenReturn(amazonS3Client);
            S3BucketSteps steps = new S3BucketSteps(bddVariableContext);
            test.accept(steps);
        }
    }
}
