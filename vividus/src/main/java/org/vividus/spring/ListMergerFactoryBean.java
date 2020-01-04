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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;

public class ListMergerFactoryBean<T> implements FactoryBean<List<T>>
{
    private List<T> object;

    @Override
    public List<T> getObject()
    {
        return object;
    }

    @Override
    public Class<?> getObjectType()
    {
        return List.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    public void setLists(List<List<T>> listOfLists)
    {
        object = new ArrayList<>();
        for (List<T> list : listOfLists)
        {
            object.addAll(list);
        }
    }
}
