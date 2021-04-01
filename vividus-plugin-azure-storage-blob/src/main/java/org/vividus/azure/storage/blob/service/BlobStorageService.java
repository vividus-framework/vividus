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

package org.vividus.azure.storage.blob.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azure.storage.blob.nio.AzureBlobFileAttributes;
import com.azure.storage.blob.nio.AzureFileSystem;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.commons.lang3.function.FailableSupplier;
import org.hamcrest.Matcher;
import org.vividus.azure.storage.blob.model.StorageAccount;
import org.vividus.util.UriUtils;
import org.vividus.util.property.PropertyMappedCollection;

public class BlobStorageService
{
    private final PropertyMappedCollection<StorageAccount> accounts;

    private final LoadingCache<StorageAccount, FileSystem> fileSystems = CacheBuilder.newBuilder()
            .build(
                new CacheLoader<StorageAccount, FileSystem>()
                {
                    @Override
                    public FileSystem load(StorageAccount storageAccount)
                    {
                        Map<String, Object> config = new HashMap<>();
                        config.put(AzureFileSystem.AZURE_STORAGE_SAS_TOKEN, storageAccount.getSasToken());
                        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, storageAccount.getContainers());
                        return getUnsafely(() -> {
                            String format = String.format(
                                "azb://?account=%1$s&endpoint=https://%1$s.blob.core.windows.net/",
                                storageAccount.getName());
                            return FileSystems.newFileSystem(UriUtils.createUri(format), config);
                        });
                    }
                });

    public BlobStorageService(PropertyMappedCollection<StorageAccount> accounts)
    {
        this.accounts = accounts;
    }

    public byte[] readBlob(String containerId, String path)
    {
        Path toRead = pathOf(containerId, path);
        return getUnsafely(() -> Files.readAllBytes(toRead));
    }

    private Path pathOf(String containerId, String path)
    {
        FileSystem blobStorage = fileSystemFor(containerId);
        return blobStorage.getPath(containerId + "://" + path);
    }

    public void delete(String containerId, String path)
    {
        Path toDelete = pathOf(containerId, path);
        getUnsafely(() -> {
            Files.delete(toDelete);
            return null;
        });
    }

    public List<Path> findFiles(Matcher<String> pathMatcher, String containerId)
    {
        Path root = pathOf(containerId, "");
        return getUnsafely(() -> walkDirectory(new ArrayList<>(), root, pathMatcher));
    }

    private List<Path> walkDirectory(List<Path> result, Path path, Matcher<String> pathMatcher) throws IOException
    {
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(path))
        {
            for (Path p : directory)
            {
                if (isDirectory(p))
                {
                    walkDirectory(result, p, pathMatcher);
                }
                else if (pathMatcher.matches(p.toString()))
                {
                    result.add(p);
                }
            }
            return result;
        }
    }

    private boolean isDirectory(Path path)
    {
        try
        {
            Files.readAttributes(path, AzureBlobFileAttributes.class);
            return false;
        }
        catch (IOException e)
        {
            // not a file
            return true;
        }
    }

    private FileSystem fileSystemFor(String containerKey)
    {
        return fileSystems.getUnchecked(accounts.getData()
                                .values()
                                .stream()
                                .filter(v -> v.getContainers().contains(containerKey))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Unable to find storage account"
                                        + " configuraiton for the container: " + containerKey)));
    }

    private static <T> T getUnsafely(FailableSupplier<T, IOException> toRun)
    {
        try
        {
            return toRun.get();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
