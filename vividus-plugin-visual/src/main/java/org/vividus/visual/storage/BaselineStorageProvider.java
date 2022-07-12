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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class BaselineStorageProvider implements ApplicationContextAware
{
    private List<String> storages;

    private ApplicationContext applicationContext;

    public void init()
    {
        this.storages = Stream.of(applicationContext.getBeanNamesForType(BaselineStorage.class))
                              .filter(s -> {
                                  try
                                  {
                                      getStorage(s);
                                      return true;
                                  }
                                  catch (BeanCreationException e)
                                  {
                                      if (e.getRootCause() instanceof ClassNotFoundException)
                                      {
                                          return false;
                                      }
                                      throw e;
                                  }
                              })
                             .collect(Collectors.toList());
    }

    private BaselineStorage getStorage(String s)
    {
        return applicationContext.getBean(s, BaselineStorage.class);
    }

    public BaselineStorage getBaselineStorage(String storageName)
    {
        Validate.isTrue(storages.contains(storageName),
            "Unable to find baseline storage with name: %s. Available baseline storages: %s", storageName, storages);
        return getStorage(storageName);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
}
