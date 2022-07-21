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

package org.vividus.visual.storage;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Named;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobStorageException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.vividus.azure.storage.blob.BlobServiceClientFactory;

import pazone.ashot.Screenshot;
import pazone.ashot.util.ImageTool;

@Named("azure-blob-storage")
@Conditional(BlobStorageCondition.class)
public class AzureBlobStorageBaselineStorage implements BaselineStorage
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureBlobStorageBaselineStorage.class);

    private final BlobServiceClientFactory blobServiceClientFactory;

    @Value("${ui.visual.baseline-storage.azure-blob-storage.container:}")
    private String container;
    @Value("${ui.visual.baseline-storage.azure-blob-storage.account-key:}")
    private String storageAccountKey;

    public AzureBlobStorageBaselineStorage(BlobServiceClientFactory blobServiceClientFactory)
    {
        this.blobServiceClientFactory = blobServiceClientFactory;
    }

    @Override
    public Optional<Screenshot> getBaseline(String baselineName) throws IOException
    {
        String blobName = createBlobName(baselineName);
        try
        {
            BlobClient blobClient = blobServiceClientFactory.createBlobClient(blobName, container, storageAccountKey);
            byte[] baseline = blobClient.downloadContent().toBytes();
            Screenshot screenshot = new Screenshot(ImageTool.toBufferedImage(baseline));
            return Optional.of(screenshot);
        }
        catch (BlobStorageException e)
        {
            if (e.getMessage().contains("The specified blob does not exist."))
            {
                LOGGER.atError().addArgument(blobName).addArgument(container).addArgument(storageAccountKey).setCause(e)
                        .log("Unable to get blob with name `{}` from container `{}` in storage account with the key "
                                + "`{}`");
            }
            else
            {
                throw e;
            }
        }
        return Optional.empty();
    }

    @Override
    public void saveBaseline(Screenshot screenshot, String baselineName) throws IOException
    {
        String blobName = createBlobName(baselineName);
        BlobClient blobClient = blobServiceClientFactory.createBlobClient(blobName, container, storageAccountKey);
        blobClient.upload(BinaryData.fromBytes(ImageTool.toByteArray(screenshot.getImage())), true);
    }

    private String createBlobName(String baselineName)
    {
        return StringUtils.appendIfMissing(baselineName, ".png");
    }

    public void setContainer(String container)
    {
        this.container = container;
    }

    public void setStorageAccountKey(String storageAccountKey)
    {
        this.storageAccountKey = storageAccountKey;
    }
}
