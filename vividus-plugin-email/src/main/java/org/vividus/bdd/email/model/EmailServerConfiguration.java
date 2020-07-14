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

package org.vividus.bdd.email.model;

import java.util.HashMap;
import java.util.Map;

public class EmailServerConfiguration
{
    private final String username;
    private final String password;
    private final Map<String, String> properties = new HashMap<>();

    public EmailServerConfiguration(String username, String password, Map<String, String> properties)
    {
        this.username = username;
        this.password = password;
        this.properties.putAll(properties);
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }
}
