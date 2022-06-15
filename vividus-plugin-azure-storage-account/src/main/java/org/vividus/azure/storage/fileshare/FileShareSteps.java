/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.azure.storage.fileshare;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.azure.storage.StorageAccountEndpointsManager;
import org.vividus.context.VariableContext;
import org.vividus.steps.DataWrapper;
import org.vividus.util.ResourceUtils;
import org.vividus.variable.VariableScope;

public class FileShareSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FileShareSteps.class);

    private final StorageAccountEndpointsManager storageAccountEndpointsManager;
    private final VariableContext variableContext;

    private final LoadingCache<String, ShareServiceClient> shareServiceClients = CacheBuilder.newBuilder().build(
            new CacheLoader<>()
            {
                @Override
                public ShareServiceClient load(String endpoint)
                {
                    return new ShareServiceClientBuilder().endpoint(endpoint).buildClient();
                }
            });

    public FileShareSteps(StorageAccountEndpointsManager storageAccountEndpointsManager,
            VariableContext variableContext)
    {
        this.storageAccountEndpointsManager = storageAccountEndpointsManager;
        this.variableContext = variableContext;
    }

    /**
     * Downloads the file from the file share and saves its content as a text to a variable
     *
     * @param filePath          The full path to the file in the file share.
     * @param shareName         The name of the file share to point to.
     * @param storageAccountKey The key of the storage account file service endpoint from the configuration.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the file content.
     * @throws IOException      In case of error on file downloading
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    @When("I download file with path `$filePath` from file share `$shareName` of storage account "
            + "`$storageAccountKey` and save its content to $scopes variable `$variableName`")
    public void downloadFile(String filePath, String shareName, String storageAccountKey, Set<VariableScope> scopes,
            String variableName) throws IOException
    {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            createShareFileClient(filePath, shareName, storageAccountKey).download(outputStream);
            variableContext.putVariable(scopes, variableName, outputStream.toString(StandardCharsets.UTF_8));
        }
    }

    /**
     * Downloads the file from the file share into a temporary file with the specified name and saves the full path to
     * the specified variable.
     *
     * @param filePath          The full path to the file in the file share.
     * @param shareName         The name of the file share to point to.
     * @param storageAccountKey The key of the storage account file service endpoint from the configuration.
     * @param baseFileName      The base file name used to generate the prefix and the suffix for the creating
     *                          temporary file.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the path to the temporary file with the file content.
     * @throws IOException      In case of error on file downloading or temporary file creation
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    @When("I download file with path `$filePath` from file share `$shareName` of storage account `$storageAccountKey` "
            + "to temporary file with name `$baseFileName` and save path to $scopes variable `$variableName`")
    public void downloadFile(String filePath, String shareName, String storageAccountKey, String baseFileName,
            Set<VariableScope> scopes, String variableName) throws IOException
    {
        Path tempFile = ResourceUtils.createTempFile(baseFileName);
        try (OutputStream outputStream = Files.newOutputStream(tempFile))
        {
            createShareFileClient(filePath, shareName, storageAccountKey).download(outputStream);
            variableContext.putVariable(scopes, variableName, tempFile.toAbsolutePath().toString());
        }
    }

    /**
     * Uploads the file to the file share.
     *
     * @param filePath          The full path to the creating file in the file share.
     * @param data              The data to store as a file.
     * @param shareName         The name of the file share to point to.
     * @param storageAccountKey The key of the storage account file service endpoint from the configuration.
     */
    @When("I upload file with path `$filePath` and data `$data` to file share `$shareName` of storage account "
            + "`$storageAccountKey`")
    public void uploadFile(String filePath, DataWrapper data, String shareName, String storageAccountKey)
    {
        ShareClient shareClient = createShareClient(shareName, storageAccountKey);
        ShareDirectoryClient currentDirectory = shareClient.getRootDirectoryClient();
        String[] fileParts = StringUtils.split(filePath, '/');
        for (int i = 0; i < fileParts.length - 1; i++)
        {
            String subdirectoryName = fileParts[i];
            currentDirectory = currentDirectory.createSubdirectoryIfNotExists(subdirectoryName);
        }

        byte[] bytes = data.getBytes();
        int fileSize = bytes.length;
        String fileName = fileParts[fileParts.length - 1];
        ShareFileClient fileClient = currentDirectory.getFileClient(fileName);
        fileClient.create(fileSize);
        ShareFileUploadInfo uploadInfo = fileClient.uploadRange(new ByteArrayInputStream(bytes), fileSize);
        LOGGER.atInfo().addArgument(uploadInfo::getETag).log("Upload of the data with eTag {} is completed");
    }

    /**
     * Deletes the specified file from the file share.
     *
     * @param filePath          The full path to the file in the file share.
     * @param shareName         The name of the file share to point to.
     * @param storageAccountKey The key of the storage account file service endpoint from the configuration.
     */
    @When("I delete file with path `$filePath` from file share `$shareName` of storage account `$storageAccountKey`")
    public void deleteFile(String filePath, String shareName, String storageAccountKey)
    {
        createShareFileClient(filePath, shareName, storageAccountKey).delete();
        LOGGER.info("The file with path '{}' is successfully deleted from the file share '{}'", filePath, shareName);
    }

    private ShareClient createShareClient(String shareName, String storageAccountKey)
    {
        String fileServiceEndpoint = storageAccountEndpointsManager.getFileServiceEndpoint(storageAccountKey);
        ShareServiceClient shareServiceClient = shareServiceClients.getUnchecked(fileServiceEndpoint);
        return shareServiceClient.getShareClient(shareName);
    }

    private ShareFileClient createShareFileClient(String filePath, String shareName, String storageAccountKey)
    {
        return createShareClient(shareName, storageAccountKey).getFileClient(StringUtils.removeStart(filePath, "/"));
    }
}
