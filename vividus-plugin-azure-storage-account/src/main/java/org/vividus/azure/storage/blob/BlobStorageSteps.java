/*
 * Copyright 2021 the original author or authors.
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

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.BddVariableContext;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.util.ResourceUtils;
import org.vividus.util.property.PropertyMappedCollection;

public class BlobStorageSteps
{
    private final BddVariableContext bddVariableContext;
    private final PropertyMappedCollection<String> storageAccountEndpoints;
    private final TokenCredential credential;

    private final LoadingCache<String, BlobServiceClient> blobStorageClients = CacheBuilder.newBuilder()
            .build(new CacheLoader<>()
            {
                @Override
                public BlobServiceClient load(String endpoint)
                {
                    return new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();
                }
            });

    public BlobStorageSteps(PropertyMappedCollection<String> storageAccountEndpoints,
            BddVariableContext bddVariableContext)
    {
        this.bddVariableContext = bddVariableContext;
        this.storageAccountEndpoints = storageAccountEndpoints;
        this.credential = new DefaultAzureCredentialBuilder().build();
    }

    /**
     * Downloads the entire blob and saves its content as a text to a variable
     *
     * @param blobName          The full path to a blob in the container.
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
    @When("I download blob with name `$blobName` from container `$containerName` in storage account "
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
     * Downloads the entire blob into a temporary file with the specified name and saves the full path to the
     * specified variable.
     *
     * @param blobName          The full path to a blob in the container.
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
    @When("I download blob located at `$blobName` from container `$containerName` in storage account "
            + "`$storageAccountKey` to temporary file with name `$baseFileName` and save blobName to $scopes "
            + "variable `$variableName`")
    public void downloadBlobToFile(String blobName, String containerName, String storageAccountKey, String baseFileName,
            Set<VariableScope> scopes, String variableName) throws IOException
    {
        String tempFilePath = ResourceUtils.createTempFile(baseFileName).toAbsolutePath().toString();
        createBlobClient(blobName, containerName, storageAccountKey).downloadToFile(tempFilePath);
        bddVariableContext.putVariable(scopes, variableName, tempFilePath);
    }

    /**
     * Deletes the specified blob.
     *
     * @param blobName          The full path to a blob in the container.
     * @param containerName     The name of the container to point to.
     * @param storageAccountKey The key to Storage Account endpoint.
     */
    @When("I delete blob located at `$blobName` from container `$containerName` in storage account "
            + "`$storageAccountKey`")
    public void deleteABlob(String blobName, String containerName, String storageAccountKey)
    {
        createBlobClient(blobName, containerName, storageAccountKey).delete();
    }

    /**
     * Finds the blobs with names matching the specified comparison rule.
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
     * @param variableName      The variable name to store the result.
     */
    @When("I find all blobs with name which $comparisonRule `$blobNameToMatch` from container `$containerName` in "
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
