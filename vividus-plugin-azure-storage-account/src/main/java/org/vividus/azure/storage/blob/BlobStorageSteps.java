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

package org.vividus.azure.storage.blob;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobServiceProperties;
import com.azure.storage.blob.models.ListBlobsOptions;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.VariableContext;
import org.vividus.steps.DataWrapper;
import org.vividus.steps.StringComparisonRule;
import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

public class BlobStorageSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BlobStorageSteps.class);
    private static final int DEFAULT_MAX_RESULTS_PER_PAGE = 1000;

    private final BlobServiceClientFactory blobServiceClientFactory;
    private final VariableContext variableContext;
    private final JsonUtils jsonUtils;

    public BlobStorageSteps(BlobServiceClientFactory blobServiceClientFactory, VariableContext variableContext,
            JsonUtils jsonUtils)
    {
        this.blobServiceClientFactory = blobServiceClientFactory;
        this.variableContext = variableContext;
        this.jsonUtils = jsonUtils;
    }

    /**
     * Retrieves the properties of a storage account’s Blob service and saves them as JSON to a variable.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * @param storageAccountKey The key to Storage Account endpoint.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the blob service properties.
     */
    @When("I retrieve blob service properties of storage account `$storageAccountKey` and save them to $scopes variable"
            + " `$variableName`")
    public void retrieveBlobServiceProperties(String storageAccountKey, Set<VariableScope> scopes, String variableName)
    {
        BlobServiceProperties properties = blobServiceClientFactory.createBlobStorageClient(storageAccountKey)
                .getProperties();
        saveJsonVariable(scopes, variableName, properties);
    }

    /**
     * Downloads the entire blob from the container and saves its content as a text to a variable
     *
     * @param blobName          The full path to the blob in the container.
     * @param containerName     The name of the container to point to.
     * @param storageAccountKey The key to Storage Account endpoint.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the blob content.
     * @throws IOException      In case of error on blob downloading
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    @When("I download blob with name `$blobName` from container `$containerName` of storage account "
            + "`$storageAccountKey` and save its content to $scopes variable `$variableName`")
    public void downloadBlob(String blobName, String containerName, String storageAccountKey, Set<VariableScope> scopes,
            String variableName) throws IOException
    {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            BlobDownloadResponse blobDownloadResponse = blobServiceClientFactory.createBlobClient(blobName,
                    containerName, storageAccountKey).downloadStreamWithResponse(outputStream, null, null, null,
                    false, null, Context.NONE);
            String contentType = blobDownloadResponse.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE);
            Object blobContent = MediaTypeRegistry.getDefaultRegistry().isInstanceOf(contentType,
                    MediaType.TEXT_PLAIN) ? outputStream.toString(StandardCharsets.UTF_8) : outputStream.toByteArray();
            variableContext.putVariable(scopes, variableName, blobContent);
        }
    }

    /**
     * Downloads the entire blob from the container into a temporary file with the specified name and saves the full
     * path to the specified variable.
     *
     * @param blobName          The full path to the blob in the container.
     * @param containerName     The name of the container to point to.
     * @param baseFileName      The base file name used to generate the prefix and the suffix for the creating
     *                          temporary file.
     * @param storageAccountKey The key to Storage Account endpoint.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the path to the temporary file with the blob content.
     * @throws IOException      In case of error on blob downloading or temporary file creation
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    @When("I download blob with name `$blobName` from container `$containerName` of storage account "
            + "`$storageAccountKey` to temporary file with name `$baseFileName` and save path to $scopes "
            + "variable `$variableName`")
    public void downloadBlobToFile(String blobName, String containerName, String storageAccountKey, String baseFileName,
            Set<VariableScope> scopes, String variableName) throws IOException
    {
        String tempFilePath = ResourceUtils.createTempFile(baseFileName).toAbsolutePath().toString();
        blobServiceClientFactory.createBlobClient(blobName, containerName, storageAccountKey)
                .downloadToFile(tempFilePath, true);
        variableContext.putVariable(scopes, variableName, tempFilePath);
    }

    /**
     * Retrieves the blob properties (all user-defined metadata, standard HTTP properties, and system properties for the
     * blob) and saves them as JSON to a variable.
     *
     * @param blobName          The full path to the blob in the container.
     * @param containerName     The name of the container to point to.
     * @param storageAccountKey The key to Storage Account endpoint.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the blob properties.
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    @When("I retrieve properties of blob with name `$blobName` from container `$containerName` of storage account "
            + "`$storageAccountKey` and save them to $scopes variable `$variableName`")
    public void retrieveBlobProperties(String blobName, String containerName, String storageAccountKey,
            Set<VariableScope> scopes, String variableName)
    {
        BlobProperties properties = blobServiceClientFactory.createBlobClient(blobName, containerName,
                storageAccountKey).getProperties();
        saveJsonVariable(scopes, variableName, properties);
    }

    /**
     * Uploads the blob to the container.
     *
     * @param blobName          The full path to the creating blob in the container.
     * @param data              The data to store as blob.
     * @param containerName     The name of the container to point to.
     * @param storageAccountKey The key to Storage Account endpoint.
     */
    @When("I upload blob with name `$blobName` and data `$data` to container `$containerName` of storage account "
            + "`$storageAccountKey`")
    public void uploadBlob(String blobName, DataWrapper data, String containerName, String storageAccountKey)
    {
        blobServiceClientFactory.createBlobClient(blobName, containerName, storageAccountKey)
                .upload(BinaryData.fromBytes(data.getBytes()));
    }

    /**
     * Uploads the blob to the container. If blob already exists it will be replaced.
     *
     * @param blobName          The full path to the creating blob in the container.
     * @param data              The data to store as blob.
     * @param containerName     The name of the container to point to.
     * @param storageAccountKey The key to Storage Account endpoint.
     */
    @When("I upsert blob with name `$blobName` and data `$data` to container `$containerName` of storage account "
            + "`$storageAccountKey`")
    public void upsertBlob(String blobName, DataWrapper data, String containerName, String storageAccountKey)
    {
        blobServiceClientFactory.createBlobClient(blobName, containerName, storageAccountKey)
                .upload(BinaryData.fromBytes(data.getBytes()), true);
    }

    /**
     * Deletes the specified blob from the container.
     *
     * @param blobName          The full path to the blob in the container.
     * @param containerName     The name of the container to point to.
     * @param storageAccountKey The key to Storage Account endpoint.
     */
    @When("I delete blob with name `$blobName` from container `$containerName` of storage account `$storageAccountKey`")
    public void deleteBlob(String blobName, String containerName, String storageAccountKey)
    {
        blobServiceClientFactory.createBlobClient(blobName, containerName, storageAccountKey)
                .delete();
        LOGGER.info("The blob with name '{}' is successfully deleted from the container '{}'", blobName, containerName);
    }

    /**
     * Finds the blobs with names matching the specified comparison rule in the container
     *
     * @param rule              The blob name comparison rule: "is equal to", "contains", "does not contain" or
     *                          "matches".
     * @param blobNameToMatch   The full or partial blob name to be matched.
     * @param containerName     The name of the container to point to.
     * @param storageAccountKey The key to Storage Account endpoint.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the list of found blob names.
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    @When("I find all blobs with name which $comparisonRule `$blobNameToMatch` in container `$containerName` of "
            + "storage account `$storageAccountKey` and save result to $scopes variable `$variableName`")
    public void findBlobs(StringComparisonRule rule, String blobNameToMatch, String containerName,
            String storageAccountKey, Set<VariableScope> scopes, String variableName)
    {
        BlobContainerClient blobContainerClient =
                blobServiceClientFactory.createBlobContainerClient(containerName, storageAccountKey);
        Matcher<String> nameMatcher = rule.createMatcher(blobNameToMatch);
        List<String> blobNames = blobContainerClient.listBlobs()
                                                    .stream()
                                                    .map(BlobItem::getName)
                                                    .filter(nameMatcher::matches)
                                                    .collect(Collectors.toList());
        variableContext.putVariable(scopes, variableName, blobNames);
    }

    /**
     * Finds blobs with names filtered by the specified rules in the container.
     *
     * @param filter            Filter to apply to blob names.
     *                          <div>Example:</div>
     *                          <code>
     *                          <br>When I filter blobs by:
     *                          <br>|blobNamePrefix|blobNameFilterRule|blobNameFilterValue|resultsLimit|
     *                          <br>|data/         |contains          |file-key.txt       |10          |
     *                          <br>in container `global` of storage account `storage`
     *                          <br> and save result to story variable `blobs`
     *                          </code>
     *                          <br>
     *                          <br>where all filters are optional, but at least one rule is required.
     *                          <ul>
     *                          <li><code>blobNameFilterRule</code> The blob name comparison rule: "is equal to",
     *                           "contains", "does not contain" or "matches".
     *                           Should be specified along with <i>blobNameFilterValue</i>.</li>
     *                          <li><code>blobNameFilterValue</code> The full or partial blob name to be matched.
     *                           Should be specified along with <i>blobNameFilterRule</i>.</li>
     *                          <li><code>blobNamePrefix</code> The prefix which blob names should start with.</li>
     *                          <li><code>resultsLimit</code> Maximum number of blob names to return.</li>
     *                          </ul>
     * @param containerName     The name of the container to point to.
     * @param storageAccountKey The key to Storage Account endpoint.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the list of found blob names.
     */
    @When("I filter blobs by:$filter in container `$containerName` of storage account `$storageAccountKey`"
            + " and save result to $scopes variable `$variableName`")
    public void findBlobs(BlobFilter filter, String containerName, String storageAccountKey, Set<VariableScope> scopes,
            String variableName)
    {
        BlobContainerClient blobContainerClient =
                blobServiceClientFactory.createBlobContainerClient(containerName, storageAccountKey);

        ListBlobsOptions options = new ListBlobsOptions();
        filter.getBlobNamePrefix().ifPresent(options::setPrefix);
        options.setMaxResultsPerPage(
                filter.getResultsLimit()
                        .map(limit -> Math.min(limit, DEFAULT_MAX_RESULTS_PER_PAGE))
                        .orElse(DEFAULT_MAX_RESULTS_PER_PAGE)
        );

        PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobs(options, null);
        Stream<String> blobNames = StreamSupport.stream(blobItems.iterableByPage().spliterator(), false)
                .map(PagedResponse::getValue)
                .flatMap(List::stream)
                .map(BlobItem::getName);
        Stream<String> filteredBlobNames = filter.getBlobNameMatcher()
                .map(matcher -> blobNames.filter(matcher::matches))
                .orElse(blobNames);

        List<String> result = filter.getResultsLimit()
                .map(filteredBlobNames::limit)
                .orElse(filteredBlobNames)
                .collect(Collectors.toList());

        variableContext.putVariable(scopes, variableName, result);
    }

    private void saveJsonVariable(Set<VariableScope> scopes, String variableName, Object data)
    {
        variableContext.putVariable(scopes, variableName, jsonUtils.toJson(data));
    }
}
