/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.util.zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

public final class ZipUtils
{
    private static final byte[] EMPTY = new byte[0];

    private ZipUtils()
    {
    }

    /**
    * Read entries from ZIP
    * @param bytes bytes of ZIP file
    * @return map contains archived file path and file body
    */
    public static Map<String, byte[]> readZipEntriesFromBytes(byte[] bytes)
    {
        return readZipEntriesFromBytes(bytes, name -> true);
    }

    /**
     * Read names of entries from ZIP
     * @param bytes bytes of ZIP file
     * @return set contains names of entries
     */
    public static Set<String> readZipEntryNamesFromBytes(byte[] bytes)
    {
        return readZipEntriesFromBytes(bytes, name -> true, false).keySet();
    }

    /**
     * Filters entries by their name and reads them from ZIP
     * @param bytes bytes of ZIP file
     * @param entryNameFilter name predicate
     * @return map contains archived file path and file body
     */
    public static Map<String, byte[]> readZipEntriesFromBytes(byte[] bytes, Predicate<String> entryNameFilter)
    {
        return readZipEntriesFromBytes(bytes, entryNameFilter, true);
    }

    private static Map<String, byte[]> readZipEntriesFromBytes(byte[] bytes, Predicate<String> entryNameFilter,
            boolean readContent)
    {
        Map<String, byte[]> zipEntries = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes)))
        {
            ZipEntry entry = zip.getNextEntry();

            while (null != entry)
            {
                String entryName = entry.getName();
                if (entryNameFilter.test(entryName))
                {
                    zipEntries.put(entryName, readContent ? IOUtils.toByteArray(zip) : EMPTY);
                }
                entry = zip.getNextEntry();
            }
            return zipEntries;
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
