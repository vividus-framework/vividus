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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.csv.CsvReader;
import org.vividus.util.ResourceUtils;

public class S3BucketSteps
{
    private final AmazonS3 amazonS3Client;
    private final IBddVariableContext bddVariableContext;

    public S3BucketSteps(AmazonS3 amazonS3Client, IBddVariableContext bddVariableContext)
    {
        this.amazonS3Client = amazonS3Client;
        this.bddVariableContext = bddVariableContext;
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
        bddVariableContext.putVariable(scopes, variableName, csv);
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
        bddVariableContext.putVariable(scopes, variableName, content);
    }

    private String fetchObject(String bucketName, String key) throws IOException
    {
        try (S3ObjectInputStream objectContent = amazonS3Client.getObject(bucketName, key).getObjectContent())
        {
            return IOUtils.toString(objectContent, StandardCharsets.UTF_8);
        }
    }

    /**
     * Delete <b>file</b> from S3 bucket by the <b>objectKey</b>
     * <br>
     * Usage example:
     * <code><br>I delete object with key `test.csv` from S3 bucket `testBucket`</code>
     * @param objectKey Key on which the content is placed in S3 bucket
     * @param bucketName S3 bucket with file
     */
    @When("I delete object with key `$objectKey` from S3 bucket `$bucketName`")
    public void deleteObject(String objectKey, String bucketName)
    {
        amazonS3Client.deleteObject(bucketName, objectKey);
    }
}
