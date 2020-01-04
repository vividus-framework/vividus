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

package org.vividus.util.json;

import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public interface IJsonUtils
{
    String toJson(Object object) throws JsonProcessingException;

    <T> T toObject(String json, Class<T> clazz) throws JsonProcessingException;

    <T> List<T> toObjectList(String json, Class<T> clazz) throws JsonProcessingException;

    <T> T toObject(InputStream json, Class<T> clazz) throws JsonProcessingException;

    <T> List<T> toObjectList(InputStream json, Class<T> clazz) throws JsonProcessingException;

    JsonNode toJson(String jsonString) throws JsonProcessingException;

    JsonNode toJson(byte[] jsonBytes) throws JsonProcessingException;
}
