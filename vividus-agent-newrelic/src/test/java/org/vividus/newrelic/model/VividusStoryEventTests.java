/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.newrelic.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.EnumFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.vividus.util.json.JsonUtils;

class VividusStoryEventTests
{
    private static final String RUN_ID = "run-1";
    private static final String LOGIN = "Login";
    private static final int BATCH_INDEX = 1;

    private final JsonUtils jsonUtils = jsonUtils();

    @Test
    void shouldSerializeStoryEvent()
    {
        VividusStoryEvent story = new VividusStoryEvent(RUN_ID, LOGIN, BATCH_INDEX, Status.FAILED,
                Map.of("team_name", "qa"));

        assertEquals(jsonUtils.readTree("""
                {"eventType":"VividusStory","runId":"run-1","storyName":"Login","batchIndex":1,
                "status":"failed","team_name":"qa"}"""),
                jsonUtils.readTree(jsonUtils.toJson(story)));
    }

    private static JsonUtils jsonUtils()
    {
        ObjectMapper objectMapper = JsonMapper.builder().enable(EnumFeature.WRITE_ENUMS_TO_LOWERCASE).build();
        return new JsonUtils(objectMapper);
    }
}
