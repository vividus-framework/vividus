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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.storage.StorageAccountEndpointsManager.StorageAccountEndpoint;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class StorageAccountEndpointsManagerTests
{
    private static final String KEY = "mystorageaccount";
    private static final String ERROR_ON_MISSING_STORAGE_ACCOUNT =
            "Storage account with key '%s' is not configured " + "in properties";

    @Mock private PropertyMappedCollection<StorageAccountEndpoint> storageAccountEndpoints;
    @InjectMocks private StorageAccountEndpointsManager storageAccountEndpointsManager;

    @Test
    void shouldReturnBlobServiceEndpoint()
    {
        var blobServiceEndpoint = "https://mystorageaccount.blob.core.windows.net";
        var storageAccountEndpoint = new StorageAccountEndpoint();
        storageAccountEndpoint.setBlobService(blobServiceEndpoint);
        when(storageAccountEndpoints.get(KEY, ERROR_ON_MISSING_STORAGE_ACCOUNT, KEY)).thenReturn(
                storageAccountEndpoint);
        assertEquals(blobServiceEndpoint, storageAccountEndpointsManager.getBlobServiceEndpoint(KEY));
    }

    @Test
    void shouldFailWhenBlobServiceEndpointIsNotConfiguredInProperties()
    {
        var storageAccountEndpoint = new StorageAccountEndpoint();
        storageAccountEndpoint.setBlobService(null);
        when(storageAccountEndpoints.get(KEY, ERROR_ON_MISSING_STORAGE_ACCOUNT, KEY)).thenReturn(
                storageAccountEndpoint);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> storageAccountEndpointsManager.getBlobServiceEndpoint(KEY));
        assertEquals(
                "Blob Service endpoint is not configured for storage account with key 'mystorageaccount' in properties",
                exception.getMessage());
    }

    @Test
    void shouldReturnFileServiceEndpoint()
    {
        var fileServiceEndpoint = "https://mystorageaccount.file.core.windows.net";
        var storageAccountEndpoint = new StorageAccountEndpoint();
        storageAccountEndpoint.setFileService(fileServiceEndpoint);
        when(storageAccountEndpoints.get(KEY, ERROR_ON_MISSING_STORAGE_ACCOUNT, KEY)).thenReturn(
                storageAccountEndpoint);
        assertEquals(fileServiceEndpoint, storageAccountEndpointsManager.getFileServiceEndpoint(KEY));
    }

    @Test
    void shouldFailWhenFileServiceEndpointIsNotConfiguredInProperties()
    {
        var storageAccountEndpoint = new StorageAccountEndpoint();
        storageAccountEndpoint.setFileService(null);
        when(storageAccountEndpoints.get(KEY, ERROR_ON_MISSING_STORAGE_ACCOUNT, KEY)).thenReturn(
                storageAccountEndpoint);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> storageAccountEndpointsManager.getFileServiceEndpoint(KEY));
        assertEquals(
                "File Service endpoint is not configured for storage account with key 'mystorageaccount' in properties",
                exception.getMessage());
    }
}
