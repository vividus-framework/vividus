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

package org.vividus.aws.s3.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.function.FailableConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.aws.auth.AwsServiceClientsContext;
import org.vividus.aws.s3.steps.S3BucketSteps.S3ObjectFilter;
import org.vividus.aws.s3.steps.S3BucketSteps.S3ObjectFilterType;
import org.vividus.context.VariableContext;
import org.vividus.steps.DataWrapper;
import org.vividus.util.DateUtils;
import org.vividus.util.ResourceUtils;
import org.vividus.variable.VariableScope;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class S3BucketStepsTests
{
    private static final String CONTENT_TYPE = "contentType";
    private static final String CSV_FILE_PATH = "/test.csv";
    private static final String S3_BUCKET_NAME = "bucketName";
    private static final String S3_OBJECT_KEY = "objectKey";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String VARIABLE_NAME = "var";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(S3BucketSteps.class);

    @Mock private S3Client amazonS3Client;
    @Mock private AwsServiceClientsContext clientsContext;
    @Mock private VariableContext variableContext;

    @Test
    void shouldUploadDataBytes() throws IOException
    {
        byte[] csvAsBytes = ResourceUtils.loadResourceAsByteArray(CSV_FILE_PATH);
        DataWrapper data = mock(DataWrapper.class);
        when(data.getBytes()).thenReturn(csvAsBytes);
        testSteps(steps -> steps.uploadData(data, S3_OBJECT_KEY, CONTENT_TYPE, S3_BUCKET_NAME));
        verifyContentUploaded(csvAsBytes, CONTENT_TYPE);
    }

    @Test
    void shouldUploadDataString() throws IOException
    {
        String text = "abc";
        byte[] stringAsBytes = text.getBytes(StandardCharsets.UTF_8);
        DataWrapper data = mock(DataWrapper.class);
        when(data.getBytes()).thenReturn(stringAsBytes);
        testSteps(steps -> steps.uploadData(data, S3_OBJECT_KEY, CONTENT_TYPE, S3_BUCKET_NAME));
        verifyContentUploaded(stringAsBytes, CONTENT_TYPE);
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
        verify(amazonS3Client).putObject(argThat((PutObjectRequest request) ->
                S3_BUCKET_NAME.equals(request.bucket()) && S3_OBJECT_KEY.equals(request.key())
                        && contentType.equals(request.contentType())), argThat((RequestBody requestBody) ->
                        {
                            try
                            {
                                return Arrays.equals(csv,
                                        IOUtils.toByteArray(requestBody.contentStreamProvider().newStream()));
                            }
                            catch (IOException e)
                            {
                                return false;
                            }
                        }));
    }

    @Test
    void shouldFetchCsvObject() throws IOException
    {
        byte[] csv = ResourceUtils.loadResourceAsByteArray(CSV_FILE_PATH);

        mockGetObject(S3_OBJECT_KEY + ".csv", csv);

        testSteps(steps -> steps.fetchCsvObject(S3_OBJECT_KEY, S3_BUCKET_NAME, SCOPES, VARIABLE_NAME));
        verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, List.of(Map.of("id", "1")));
    }

    @Test
    void shouldFetchObject() throws IOException
    {
        String objectKey = S3_OBJECT_KEY + ".json";
        String data = "data";

        mockGetObject(objectKey, data.getBytes(StandardCharsets.UTF_8));

        testSteps(steps -> steps.fetchObject(objectKey, S3_BUCKET_NAME, SCOPES, VARIABLE_NAME));
        verify(amazonS3Client).getObjectAsBytes(argThat((GetObjectRequest request) ->
                S3_BUCKET_NAME.equals(request.bucket()) && objectKey.equals(request.key())));
        verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, data);
    }

    private void mockGetObject(String objectKey, byte[] data)
    {
        when(amazonS3Client.getObjectAsBytes(argThat((GetObjectRequest request) ->
                S3_BUCKET_NAME.equals(request.bucket()) && objectKey.equals(request.key()))))
                .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), data));
    }

    @Test
    void shouldSetObjectAcl() throws IOException
    {
        testSteps(steps -> steps.setObjectAcl(ObjectCannedACL.PUBLIC_READ_WRITE, S3_OBJECT_KEY, S3_BUCKET_NAME));
        verify(amazonS3Client).putObjectAcl(argThat((PutObjectAclRequest request) ->
                S3_BUCKET_NAME.equals(request.bucket()) && S3_OBJECT_KEY.equals(request.key())
                        && ObjectCannedACL.PUBLIC_READ_WRITE.equals(request.acl())));
    }

    @Test
    void shouldDeleteObject() throws IOException
    {
        testSteps(steps -> steps.deleteObject(S3_OBJECT_KEY, S3_BUCKET_NAME));
        verify(amazonS3Client).deleteObject(argThat((DeleteObjectRequest request) ->
                S3_BUCKET_NAME.equals(request.bucket()) && S3_OBJECT_KEY.equals(request.key())));
    }

    @Test
    void shouldCollectKeysWithEmptyFilters() throws IOException
    {
        String key = "any";
        S3Object objectSummary = S3Object.builder().key(key).build();
        ListObjectsV2Response result = ListObjectsV2Response.builder()
                .isTruncated(false)
                .contents(objectSummary)
                .build();

        when(amazonS3Client.listObjectsV2(argThat((ArgumentMatcher<ListObjectsV2Request>)
                rq -> S3_BUCKET_NAME.equals(rq.bucket()) && rq.prefix() == null))).thenReturn(result);
        testSteps(steps -> steps.collectObjectKeys(List.of(), S3_BUCKET_NAME, SCOPES, VARIABLE_NAME));

        verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, List.of(key));

        assertKeysCollectorLogs(1, 1);
    }

    @Test
    void shouldCollectKeysWithFilters() throws IOException
    {
        String dateThreshold = "2021-01-15T19:00:00+00:00";
        ZonedDateTime zonedDateThreshold = ZonedDateTime.parse(dateThreshold);
        String key = "/folder/object.xml";

        S3Object objectSummary1 = S3Object.builder()
                .key(key)
                .lastModified(zonedDateThreshold.plusMinutes(1).toInstant())
                .build();

        S3Object objectSummary2 = S3Object.builder()
                .key(key)
                .lastModified(zonedDateThreshold.toInstant())
                .build();

        ListObjectsV2Response result1 = ListObjectsV2Response.builder()
                .contents(objectSummary1, objectSummary2)
                .isTruncated(true)
                .build();

        S3Object objectSummary3 = S3Object.builder()
                .key("/folder/object.txt")
                .lastModified(zonedDateThreshold.plusMinutes(1).toInstant())
                .build();

        ListObjectsV2Response result2 = ListObjectsV2Response.builder()
                .contents(objectSummary3)
                .isTruncated(false)
                .build();

        String prefix = "/folder/o";

        when(amazonS3Client.listObjectsV2(argThat((ArgumentMatcher<ListObjectsV2Request>)
                    rq -> S3_BUCKET_NAME.equals(rq.bucket()) && prefix.equals(rq.prefix()))))
                .thenReturn(result1, result2);

        List<S3ObjectFilter> filters = createFilters(prefix, ".xml", dateThreshold);

        testSteps(steps -> steps.collectObjectKeys(filters, S3_BUCKET_NAME, SCOPES, VARIABLE_NAME));

        verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, List.of(key));

        assertKeysCollectorLogs(3, 1);
    }

    private void assertKeysCollectorLogs(int totalNumberOfObjects, int numberOfObjectsAfterFiltering)
    {
        assertThat(logger.getLoggingEvents(), is(List.of(
                info("The total number of S3 objects is {}", totalNumberOfObjects),
                info("The number of S3 objects after filtering is {}", numberOfObjectsAfterFiltering)
        )));
    }

    private List<S3ObjectFilter> createFilters(String prefix, String suffix, String dateThreshold)
    {
        S3ObjectFilter keyPrefixFilter = new S3ObjectFilter();
        keyPrefixFilter.setFilterType(S3ObjectFilterType.KEY_PREFIX);
        keyPrefixFilter.setFilterValue(prefix);

        S3ObjectFilter keySuffixFilter = new S3ObjectFilter();
        keySuffixFilter.setFilterType(S3ObjectFilterType.KEY_SUFFIX);
        keySuffixFilter.setFilterValue(suffix);

        S3ObjectFilter lastModifiedDateFilter = new S3ObjectFilter();
        lastModifiedDateFilter.setFilterType(S3ObjectFilterType.OBJECT_MODIFIED_NOT_EARLIER_THAN);
        lastModifiedDateFilter.setFilterValue(dateThreshold);

        return List.of(keyPrefixFilter, keySuffixFilter, lastModifiedDateFilter);
    }

    void testSteps(FailableConsumer<S3BucketSteps, IOException> test) throws IOException
    {
        S3BucketSteps steps = new S3BucketSteps(clientsContext, variableContext, new DateUtils(ZoneId.of("Z")));
        when(clientsContext.getServiceClient(any(), any())).thenReturn(amazonS3Client);
        test.accept(steps);
    }
}
