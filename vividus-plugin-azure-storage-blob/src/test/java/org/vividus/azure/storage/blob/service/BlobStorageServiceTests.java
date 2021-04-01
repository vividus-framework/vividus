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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.azure.storage.blob.nio.AzureBlobFileAttributes;
import com.azure.storage.blob.nio.AzureFileSystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.storage.blob.model.StorageAccount;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class BlobStorageServiceTests
{
    private static final String PATH = "path";
    private static final String CONTAINER_PATH = "container://path";
    private static final String SAS_TOKEN = "sas_token";
    private static final String CONTAINER = "container";
    private static final Map<String, ?> CONFIG = Map.of(AzureFileSystem.AZURE_STORAGE_SAS_TOKEN, SAS_TOKEN,
              AzureFileSystem.AZURE_STORAGE_FILE_STORES, CONTAINER);
    private static final String URI_FORMAT = "azb://?account=%1$s&endpoint=https://%1$s.blob.core.windows.net/";

    @Mock private PropertyMappedCollection<StorageAccount> accounts;
    @Mock private FileSystem fileSystem;

    @InjectMocks private BlobStorageService blobStorageService;

    private void mockContainer(String accountName)
    {
        StorageAccount storageAccount = new StorageAccount();
        storageAccount.setContainers(CONTAINER);
        storageAccount.setSasToken(SAS_TOKEN);
        storageAccount.setName(accountName);
        when(accounts.getData()).thenReturn(Map.of(CONTAINER, storageAccount));
    }

    @Test
    void shouldThrowAnExceptionIfNoStorageAccountConfigurationForTheContainerAvailable()
    {
        mockContainer("notExists");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> blobStorageService.delete("noConfig", null));
        assertEquals("Unable to find storage account configuraiton for the container: noConfig",
                exception.getMessage());
    }

    @Test
    void shouldReadBytes()
    {
        String accountName = "read";
        runWithFileSystem((files, filesystem) -> {
            Path path = mock(Path.class);
            when(filesystem.getPath(CONTAINER_PATH)).thenReturn(path);
            mockContainer(accountName);
            byte[] bytes = "value".getBytes(StandardCharsets.UTF_8);
            files.when(() -> Files.readAllBytes(path)).thenReturn(bytes);
            assertSame(bytes, blobStorageService.readBlob(CONTAINER, PATH));
        }, accountName);
    }

    @Test
    void shouldDelete()
    {
        String accountName = "delete";
        runWithFileSystem((files, filesystem) -> {
            Path path = mock(Path.class);
            when(filesystem.getPath(CONTAINER_PATH)).thenReturn(path);
            mockContainer(accountName);
            blobStorageService.delete(CONTAINER, PATH);
            files.verify(() -> Files.delete(path));
        }, accountName);
    }

    @Test
    void shouldRethrowUncheckedIoException()
    {
        String accountName = "uncheckedIo";
        runWithFileSystem((files, filesystem) -> {
            Path path = mock(Path.class);
            when(filesystem.getPath(CONTAINER_PATH)).thenReturn(path);
            mockContainer(accountName);
            IOException ioException = new IOException();
            files.when(() -> Files.delete(path)).thenThrow(ioException);
            UncheckedIOException uncheckedIo = assertThrows(UncheckedIOException.class,
                    () -> blobStorageService.delete(CONTAINER, PATH));
            assertSame(ioException, uncheckedIo.getCause());
        }, accountName);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldListFiles()
    {
        String accountName = "list";
        runWithFileSystem((files, filesystem) -> {
            mockContainer(accountName);
            Path rootPath = mock(Path.class);
            when(filesystem.getPath("container://")).thenReturn(rootPath);
            DirectoryStream<Path> rootStream = mock(DirectoryStream.class);
            DirectoryStream<Path> emptyStream = mock(DirectoryStream.class);
            DirectoryStream<Path> streamWithJson = mock(DirectoryStream.class);
            Path json = mock(Path.class);
            Path nestedJson = mock(Path.class);
            Path directory = mock(Path.class);
            Path directoryContainingJson = mock(Path.class);
            Path notJson = mock(Path.class);
            List<Path> empty = List.of();
            when(rootStream.iterator()).thenReturn(List.of(json, directory, notJson,
                    directoryContainingJson).iterator());
            when(emptyStream.iterator()).thenReturn(empty.iterator());
            when(streamWithJson.iterator()).thenReturn(List.of(notJson, nestedJson).iterator());
            files.when(() -> direcrtoryStream(rootPath)).thenReturn(rootStream);
            files.when(() -> direcrtoryStream(directoryContainingJson)).thenReturn(streamWithJson);
            files.when(() -> direcrtoryStream(directory)).thenReturn(emptyStream);
            files.when(() -> Files.readAttributes(directory,
                    AzureBlobFileAttributes.class)).thenThrow(new IOException());
            files.when(() -> Files.readAttributes(directoryContainingJson, AzureBlobFileAttributes.class))
                .thenThrow(new IOException());
            when(json.toString()).thenReturn("test.json");
            when(nestedJson.toString()).thenReturn("folder/test.json");
            assertEquals(List.of(json, nestedJson),
                    blobStorageService.findFiles(StringComparisonRule.CONTAINS.createMatcher("json"), CONTAINER));
        }, accountName);
    }

    private DirectoryStream<Path> direcrtoryStream(Path directoryContainingJson)
    {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directoryContainingJson))
        {
            return directoryStream;
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private void runWithFileSystem(BiConsumer<MockedStatic<Files>, FileSystem> testToRun, String accountName)
    {
        try (MockedStatic<FileSystems> fileSystems = mockStatic(FileSystems.class);
                MockedStatic<Files> files = mockStatic(Files.class))
        {
            fileSystems.when(() -> FileSystems.newFileSystem(URI.create(String.format(URI_FORMAT, accountName)),
                    CONFIG)).thenReturn(fileSystem);
            testToRun.accept(files, fileSystem);
        }
    }
}
