/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.spring;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;

public class StartContextListener implements ApplicationListener<ContextStartedEvent>
{
    private Optional<List<File>> cleanableDirectories;

    @Override
    public void onApplicationEvent(ContextStartedEvent event)
    {
        cleanableDirectories.stream().flatMap(List::stream).forEach(this::deleteDirectory);
    }

    private void deleteDirectory(File directory)
    {
        if (directory.exists())
        {
            try
            {
                FileUtils.deleteDirectory(directory);
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }

    public void setCleanableDirectories(Optional<List<File>> cleanableDirectories)
    {
        this.cleanableDirectories = cleanableDirectories;
    }
}
