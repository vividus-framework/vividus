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

package org.vividus.azure.storage.blob;

import com.azure.core.credential.TokenCredential;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.vividus.azure.storage.StorageAccountEndpointsManager;

public class BlobServiceClientFactory
{
    private final StorageAccountEndpointsManager storageAccountEndpointsManager;
    private final TokenCredential credential;

    private final LoadingCache<String, BlobServiceClient> blobServiceClients = CacheBuilder.newBuilder()
            .build(new CacheLoader<>()
            {
                @Override
                public BlobServiceClient load(String endpoint)
                {
                    return new BlobServiceClientBuilder().credential(credential).endpoint(endpoint).buildClient();
                }
            });

    public BlobServiceClientFactory(StorageAccountEndpointsManager storageAccountEndpointsManager,
            TokenCredential credential)
    {
        this.storageAccountEndpointsManager = storageAccountEndpointsManager;
        this.credential = credential;
    }

    public BlobServiceClient createBlobStorageClient(String storageAccountKey)
    {
        return blobServiceClients.getUnchecked(
            storageAccountEndpointsManager.getBlobServiceEndpoint(storageAccountKey));
    }

    public BlobContainerClient createBlobContainerClient(String containerName, String storageAccountKey)
    {
        return createBlobStorageClient(storageAccountKey).getBlobContainerClient(containerName);
    }

    public BlobClient createBlobClient(String blobName, String containerName, String storageAccountKey)
    {
        return createBlobContainerClient(containerName, storageAccountKey).getBlobClient(blobName);
    }
}
