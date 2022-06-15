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

package org.vividus.azure.storage;

import java.util.function.Function;

import org.apache.commons.lang3.Validate;
import org.vividus.util.property.PropertyMappedCollection;

public class StorageAccountEndpointsManager
{
    private final PropertyMappedCollection<StorageAccountEndpoint> storageAccountEndpoints;

    public StorageAccountEndpointsManager(PropertyMappedCollection<StorageAccountEndpoint> storageAccountEndpoints)
    {
        this.storageAccountEndpoints = storageAccountEndpoints;
    }

    public String getBlobServiceEndpoint(String storageAccountKey)
    {
        return getServiceEndpoint(storageAccountKey, StorageAccountEndpoint::getBlobService, "Blob");
    }

    public String getFileServiceEndpoint(String storageAccountKey)
    {
        return getServiceEndpoint(storageAccountKey, StorageAccountEndpoint::getFileService, "File");
    }

    private String getServiceEndpoint(String storageAccountKey,
            Function<StorageAccountEndpoint, String> endpointProvider, String serviceName)
    {
        StorageAccountEndpoint endpoint = storageAccountEndpoints.get(storageAccountKey,
                "Storage account with key '%s' is not configured in properties", storageAccountKey);
        String serviceEndpoint = endpointProvider.apply(endpoint);
        Validate.isTrue(serviceEndpoint != null,
                "%s Service endpoint is not configured for storage account with key '%s' in properties", serviceName,
                storageAccountKey);
        return serviceEndpoint;
    }

    public static class StorageAccountEndpoint
    {
        private String blobService;
        private String fileService;

        public String getBlobService()
        {
            return blobService;
        }

        public void setBlobService(String blobService)
        {
            this.blobService = blobService;
        }

        public String getFileService()
        {
            return fileService;
        }

        public void setFileService(String fileService)
        {
            this.fileService = fileService;
        }
    }
}
