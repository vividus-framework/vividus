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

package org.vividus.azure.storage.blob;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.azure.storage.blob.model.BlobFilter;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.DataWrapper;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.PropertyMappedCollection;

public class BlobStorageSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BlobStorageSteps.class);

    private final PropertyMappedCollection<String> storageAccountEndpoints;
    private final TokenCredential credential;
    private final IBddVariableContext bddVariableContext;
    private final JsonUtils jsonUtils;

    private final LoadingCache<String, BlobServiceClient> blobStorageClients = CacheBuilder.newBuilder()
            .build(new CacheLoader<>()
            {
                @Override
                public BlobServiceClient load(String endpoint)
                {
                    return new BlobServiceClientBuilder().credential(credential).endpoint(endpoint).buildClient();
                }
            });

    public BlobStorageSteps(PropertyMappedCollection<String> storageAccountEndpoints, TokenCredential credential,
            IBddVariableContext bddVariableContext, JsonUtils jsonUtils)
    {
        this.storageAccountEndpoints = storageAccountEndpoints;
        this.credential = credential;
        this.bddVariableContext = bddVariableContext;
        this.jsonUtils = jsonUtils;
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
            createBlobClient(blobName, containerName, storageAccountKey).download(outputStream);
            bddVariableContext.putVariable(scopes, variableName, outputStream.toString(StandardCharsets.UTF_8));
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
        createBlobClient(blobName, containerName, storageAccountKey).downloadToFile(tempFilePath, true);
        bddVariableContext.putVariable(scopes, variableName, tempFilePath);
    }

    /**
     * Retrieves the blob properties (all user-defined metadata, standard HTTP properties, and system properties for the
     * blob) and saves them as JSON to a variable
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
        BlobProperties blobProperties = createBlobClient(blobName, containerName, storageAccountKey).getProperties();
        bddVariableContext.putVariable(scopes, variableName, jsonUtils.toJson(blobProperties));
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
        createBlobClient(blobName, containerName, storageAccountKey).upload(BinaryData.fromBytes(data.getBytes()));
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
        createBlobClient(blobName, containerName, storageAccountKey).delete();
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
        BlobContainerClient blobContainerClient = createBlobContainerClient(containerName, storageAccountKey);
        Matcher<String> nameMatcher = rule.createMatcher(blobNameToMatch);
        List<String> blobNames = blobContainerClient.listBlobs()
                                                    .stream()
                                                    .map(BlobItem::getName)
                                                    .filter(nameMatcher::matches)
                                                    .collect(Collectors.toList());
        bddVariableContext.putVariable(scopes, variableName, blobNames);
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
    public void findBlobsByFilter(BlobFilter filter, String containerName,
                                      String storageAccountKey, Set<VariableScope> scopes, String variableName)
    {
        filter.validate();
        BlobContainerClient blobContainerClient = createBlobContainerClient(containerName, storageAccountKey);
        bddVariableContext.putVariable(scopes, variableName, getLimitedBlobNames(blobContainerClient, filter));
    }

    private static List<String> getLimitedBlobNames(BlobContainerClient blobContainerClient, BlobFilter filter)
    {
        return filter.getResultsLimit()
                .map(limit -> {
                    ListBlobsOptions options = new ListBlobsOptions();
                    options.setMaxResultsPerPage(limit.intValue());
                    return filter.getBlobNamePrefix()
                            .map(options::setPrefix)
                            .map(opts -> getLimitPerPage(filter, limit,
                                    blobContainerClient.listBlobsByHierarchy("/", opts, null)))
                            .orElseGet(
                                () -> getLimitPerPage(filter, limit,
                                        blobContainerClient.listBlobs(options, null)));
                })
                .orElseGet(
                    () -> filter.getBlobNamePrefix()
                        .map(blobContainerClient::listBlobsByHierarchy)
                        .orElseGet(blobContainerClient::listBlobs)
                        .stream()
                        .map(BlobItem::getName)
                        .filter(filterMatched(filter))
                        .collect(Collectors.toList())
                );
    }

    private static List<String> getLimitPerPage(BlobFilter filter, Long limit, PagedIterable<BlobItem> pages)
    {
        List<String> blobNames = new ArrayList<>();
        for (PagedResponse<BlobItem> page : pages.iterableByPage())
        {
            if (blobNames.size() < limit)
            {
                LOGGER.info("Blob Items on page: {}", page.getContinuationToken());
                page.getValue()
                        .stream()
                        .map(BlobItem::getName)
                        .filter(filterMatched(filter))
                        .forEach(blobNames::add);
            }
            else
            {
                break;
            }
        }
        return blobNames.stream().limit(limit).collect(Collectors.toList());
    }

    private static Predicate<String> filterMatched(BlobFilter filter)
    {
        return name -> filter.getBlobNameFilterValue()
                    .map(value -> filter.getBlobNameFilterRule().get().createMatcher(value).matches(name))
                    .orElse(true);
    }

    private BlobContainerClient createBlobContainerClient(String containerName, String storageAccountKey)
    {
        String endpoint = storageAccountEndpoints.get(storageAccountKey,
                "Storage account with key '%s' is not configured in properties", storageAccountKey);
        BlobServiceClient blobStorageClient = blobStorageClients.getUnchecked(endpoint);
        return blobStorageClient.getBlobContainerClient(containerName);
    }

    private BlobClient createBlobClient(String blobName, String containerName, String storageAccountKey)
    {
        BlobContainerClient blobContainerClient = createBlobContainerClient(containerName, storageAccountKey);
        return blobContainerClient.getBlobClient(blobName);
    }
}
