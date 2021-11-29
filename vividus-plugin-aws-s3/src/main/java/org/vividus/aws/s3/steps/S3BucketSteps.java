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

package org.vividus.aws.s3.steps;

import static java.util.stream.Collectors.toMap;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.AsParameters;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.VariableContext;
import org.vividus.csv.CsvReader;
import org.vividus.util.DateUtils;
import org.vividus.util.ResourceUtils;
import org.vividus.variable.VariableScope;

public class S3BucketSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(S3BucketSteps.class);

    private final AmazonS3 amazonS3Client;
    private final VariableContext variableContext;
    private final DateUtils dateUtils;

    public S3BucketSteps(VariableContext variableContext, DateUtils dateUtils)
    {
        this.dateUtils = dateUtils;
        this.amazonS3Client = AmazonS3ClientBuilder.defaultClient();
        this.variableContext = variableContext;
    }

    /**
     * Uploads <b>data</b> into S3 given bucket by the <b>objectKey</b>
     * <br>
     * Usage example:
     * <code><br>When I upload data `{"my":"json"}` with key `folder/name.json` and content type `application/json`
     * to S3 bucket `testBucket`</code>
     * @param data The data to be uploaded to Amazon S3
     * @param objectKey Key on which the content is added to S3 bucket
     * @param contentType Mime type of object for upload (see <a href="https://en.wikipedia.org/wiki/MIME">MIME</a>)
     * @param bucketName S3 bucket to upload
     */
    @When("I upload data `$data` with key `$objectKey` and content type `$contentType` to S3 bucket `$bucketName`")
    public void uploadData(String data, String objectKey, String contentType, String bucketName)
    {
        uploadContent(bucketName, objectKey, data.getBytes(StandardCharsets.UTF_8), contentType);
    }

    /**
     * Uploads <b>resource</b> into S3 given bucket by the <b>objectKey</b>
     * <br>
     * Usage example:
     * <code><br>When I upload `/story/test.csv` with key `folder/name.csv`
     *  and content type `text/csv` to S3 bucket `testBucket`</code>
     * @param resourcePath Path to resource for upload
     * @param objectKey Key on which the content is added to S3 bucket
     * @param contentType Mime type of object for upload (see <a href="https://en.wikipedia.org/wiki/MIME">MIME</a>)
     * @param bucketName S3 bucket to upload
     */
    @When("I upload resource `$resourcePath` with key `$objectKey` and content type `$contentType`"
            + " to S3 bucket `$bucketName`")
    public void uploadResource(String resourcePath, String objectKey, String contentType, String bucketName)
    {
        byte[] resource = ResourceUtils.loadResourceAsByteArray(resourcePath);
        uploadContent(bucketName, objectKey, resource, contentType);
    }

    /**
     * Uploads <b>file</b> into S3 given bucket by the <b>objectKey</b>
     * <br>
     * Usage example:
     * <code><br>When I upload file`C:/Users/user/Temp/test.csv` with key `folder/name.csv`
     *  and content type `text/csv` to S3 bucket `testBucket`</code>
     * @param file File for upload
     * @param objectKey Key on which the content is added to S3 bucket
     * @param contentType Mime type of object for upload (see <a href="https://en.wikipedia.org/wiki/MIME">MIME</a>)
     * @param bucketName S3 bucket to upload
     * @throws IOException in case of error on file reading
     */
    @When("I upload `$file` with key `$objectKey` and content type `$contentType` to S3 bucket `$bucketName`")
    public void uploadFile(File file, String objectKey, String contentType, String bucketName) throws IOException
    {
        byte[] content = FileUtils.readFileToByteArray(file);
        uploadContent(bucketName, objectKey, content, contentType);
    }

    private void uploadContent(String bucketName, String objectKey, byte[] content, String contentType)
    {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);
        objectMetadata.setContentLength(content.length);
        InputStream inputStream = new ByteArrayInputStream(content);
        amazonS3Client.putObject(bucketName, objectKey, inputStream, objectMetadata);
    }

    /**
     * Retrieve the CSV object by key from the provided S3 bucket and save it to <b>scopes</b> variable with name
     * <b>variableName</b>.<br>
     * Usage example:
     * <code><br>When I fetch CSV object with key `file.csv` from S3 bucket `myTestBucket` and save result to scenario
     *  variable `csv-from-s3`</code>
     * @param objectKey The key under which the desired object is stored
     * @param bucketName The name of the bucket containing the desired object
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables scopes<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName the variable name
     * @throws IOException in case of IO error during returned file processing
     */
    @When("I fetch CSV object with key `$objectKey` from S3 bucket `$bucketName` "
            + "and save result to $scopes variable `$variableName`")
    public void fetchCsvObject(String objectKey, String bucketName, Set<VariableScope> scopes, String variableName)
            throws IOException
    {
        String csvString = fetchObject(bucketName, StringUtils.appendIfMissing(objectKey, ".csv"));
        List<Map<String, String>> csv = new CsvReader().readCsvString(csvString);
        variableContext.putVariable(scopes, variableName, csv);
    }

    /**
     * Retrieve the object by key from the provided S3 bucket and save its content to <b>scopes</b> variables with name
     * <b>variableName</b>.<br>
     * Usage example:
     * <code><br>When I fetch object with key `file.json` from S3 bucket `myTestBucket` and save result to scenario
     *  variable `json-from-s3`</code>
     * @param objectKey The key under which the desired object is stored
     * @param bucketName The name of the bucket containing the desired object
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables scopes<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName the variable name
     * @throws IOException in case of IO error during returned file processing
     */
    @When("I fetch object with key `$objectKey` from S3 bucket `$bucketName` and save result to $scopes variable"
            + " `$variableName`")
    public void fetchObject(String objectKey, String bucketName, Set<VariableScope> scopes, String variableName)
            throws IOException
    {
        String content = fetchObject(bucketName, objectKey);
        variableContext.putVariable(scopes, variableName, content);
    }

    private String fetchObject(String bucketName, String key) throws IOException
    {
        try (S3ObjectInputStream objectContent = amazonS3Client.getObject(bucketName, key).getObjectContent())
        {
            return IOUtils.toString(objectContent, StandardCharsets.UTF_8);
        }
    }

    /**
     * Sets the canned access control list (ACL) for the specified object in Amazon S3. Each bucket and object in
     * Amazon S3 has an ACL that defines its access control policy.  When a request is made, Amazon S3 authenticates the
     * request using its standard authentication procedure and then checks the ACL to verify the sender was granted
     * access to the bucket or object. If the sender is approved, the request proceeds. Otherwise, Amazon S3 returns
     * an error.
     *
     * @param cannedAcl  The new pre-configured canned ACL for the specified object. See
     *                   <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/acl-overview.html#canned-acl">
     *                   the official documentation
     *                   </a>
     *                   for a complete list of the available ACLs.
     * @param objectKey  The key of the object within the specified bucket whose ACL is being set.
     * @param bucketName The name of the bucket containing the object whose ACL is being set
     */
    @When("I set ACL `$cannedAcl` for object with key `$objectKey` from S3 bucket `$bucketName`")
    public void setObjectAcl(CannedAccessControlList cannedAcl, String objectKey, String bucketName)
    {
        amazonS3Client.setObjectAcl(bucketName, objectKey, cannedAcl);
    }

    /**
     * Deletes the specified object in the specified bucket. Once deleted, the object can only be restored if
     * versioning was enabled when the object was deleted. If attempting to delete an object that does not exist,
     * Amazon S3 returns a success message instead of an error message.
     * <br>
     * Usage example:
     * <code><br>I delete object with key `test.csv` from S3 bucket `testBucket`</code>
     *
     * @param objectKey  The key of the object to delete
     * @param bucketName The name of the Amazon S3 bucket containing the object to delete
     */
    @When("I delete object with key `$objectKey` from S3 bucket `$bucketName`")
    public void deleteObject(String objectKey, String bucketName)
    {
        amazonS3Client.deleteObject(bucketName, objectKey);
    }

    /**
     * <p>
     * Collects a list of the S3 objects keys in the specified bucket and saves its content to <b>scopes</b> variables
     * with name <b>variableName</b>.
     * </p>
     * <p>
     * Because buckets can contain a virtually unlimited number of keys, the complete results can be extremely large,
     * thus it's recommended to use filters to retrieve the filtered dataset.
     * </p>
     *
     * @param filters      The ExamplesTable with filters to be applied to the objects to limit the resulting set.
     *                     The supported filter types are:
     *                     <ul>
     *                     <li><code>KEY_PREFIX</code> - the prefix parameter, restricting to keys that begin with
     *                     the specified value.</li>
     *                     <li><code>KEY_SUFFIX</code> - the suffix parameter, restricting to keys that end with the
     *                     specified value.</li>
     *                     <li><code>OBJECT_MODIFIED_NOT_EARLIER_THAN</code> - the ISO-8601 date, restricting to objects
     *                     with last modified date after the specified value.</li>
     *                     </ul>
     *                     The filters can be combined in any order and in any composition, e.g.<br>
     *                     <code>
     *                     |filterType                      |filterValue               |<br>
     *                     |key suffix                      |.txt                      |<br>
     *                     |object modified not earlier than|2021-01-15T19:00:00+00:00 |<br>
     *                     </code>
     *
     * @param bucketName   The name of the S3 bucket which objects keys are to be collected
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables scopes<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>@param scopes
     * @param variableName the variable name to store the S3 objects keys. The keys are accessible via zero-based index,
     *                     e.g. <code>${my-keys[0]}</code> will return the first found key.
     */
    @When("I collect objects keys filtered by:$filters in S3 bucket `$bucketName` and save result to $scopes variable "
            + "`$variableName`")
    public void collectObjectKeys(List<S3ObjectFilter> filters, String bucketName, Set<VariableScope> scopes,
            String variableName)
    {
        Map<S3ObjectFilterType, String> filterParameters = filters.stream().collect(
                toMap(S3ObjectFilter::getFilterType, S3ObjectFilter::getFilterValue));

        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName);
        Optional.ofNullable(filterParameters.get(S3ObjectFilterType.KEY_PREFIX)).ifPresent(request::setPrefix);

        Predicate<S3ObjectSummary> filter = buildFilter(filterParameters);

        List<String> keys = collectS3ObjectsKeys(request, filter);
        variableContext.putVariable(scopes, variableName, keys);
    }

    private Predicate<S3ObjectSummary> buildFilter(Map<S3ObjectFilterType, String> filterParameters)
    {
        Predicate<S3ObjectSummary> keySuffixPredicate = Optional.ofNullable(
                filterParameters.get(S3ObjectFilterType.KEY_SUFFIX))
                .map(keySuffix -> (Predicate<S3ObjectSummary>) summary -> summary.getKey().endsWith(keySuffix))
                .orElseGet(() -> summary -> true);

        Predicate<S3ObjectSummary> lowestModifiedPredicate = Optional.ofNullable(
                filterParameters.get(S3ObjectFilterType.OBJECT_MODIFIED_NOT_EARLIER_THAN))
                .map(date -> dateUtils.parseDateTime(date, DateTimeFormatter.ISO_DATE_TIME))
                .map(ZonedDateTime::toInstant)
                .map(Date::from)
                .map(date -> (Predicate<S3ObjectSummary>) summary -> summary.getLastModified().after(date))
                .orElseGet(() -> summary -> true);

        return keySuffixPredicate.and(lowestModifiedPredicate);
    }

    private List<String> collectS3ObjectsKeys(ListObjectsV2Request request, Predicate<S3ObjectSummary> filter)
    {
        ListObjectsV2Result result;
        List<String> keys = new ArrayList<>();
        int totalNumberOfObjects = 0;

        do
        {
            result = amazonS3Client.listObjectsV2(request);

            List<S3ObjectSummary> objectSummaries = result.getObjectSummaries();
            totalNumberOfObjects += objectSummaries.size();

            objectSummaries.stream()
                    .filter(filter)
                    .map(S3ObjectSummary::getKey)
                    .forEach(keys::add);

            request.setContinuationToken(result.getNextContinuationToken());
        }
        while (result.isTruncated());

        LOGGER.info("The total number of S3 objects is {}", totalNumberOfObjects);
        LOGGER.atInfo().addArgument(keys::size).log("The number of S3 objects after filtering is {}");

        return keys;
    }

    @AsParameters
    public static class S3ObjectFilter
    {
        private S3ObjectFilterType filterType;
        private String filterValue;

        public S3ObjectFilterType getFilterType()
        {
            return filterType;
        }

        public void setFilterType(S3ObjectFilterType filterType)
        {
            this.filterType = filterType;
        }

        public String getFilterValue()
        {
            return filterValue;
        }

        public void setFilterValue(String filterValue)
        {
            this.filterValue = filterValue;
        }
    }

    public enum S3ObjectFilterType
    {
        KEY_PREFIX,
        KEY_SUFFIX,
        OBJECT_MODIFIED_NOT_EARLIER_THAN
    }
}
