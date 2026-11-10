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

class VividusRunEventTests
{
    private static final String RUN_ID = "run-1";
    private static final String ENVIRONMENT = "environment";
    private static final String STAGING = "staging";

    private final JsonUtils jsonUtils = jsonUtils();

    @Test
    void shouldOmitStatusWhenRunStarts()
    {
        VividusRunEvent run = new VividusRunEvent(RUN_ID, RunPhase.STARTED, null, Map.of(ENVIRONMENT, STAGING));

        assertEquals(jsonUtils.readTree("""
                {"eventType":"VividusRun","runId":"run-1","phase":"started","environment":"staging"}"""),
                jsonUtils.readTree(jsonUtils.toJson(run)));
    }

    @Test
    void shouldIncludeStatusWhenRunFinishes()
    {
        VividusRunEvent run = new VividusRunEvent(RUN_ID, RunPhase.FINISHED, Status.PASSED, Map.of());

        assertEquals(jsonUtils.readTree("""
                {"eventType":"VividusRun","runId":"run-1","phase":"finished","status":"passed"}"""),
                jsonUtils.readTree(jsonUtils.toJson(run)));
    }

    private static JsonUtils jsonUtils()
    {
        ObjectMapper objectMapper = JsonMapper.builder().enable(EnumFeature.WRITE_ENUMS_TO_LOWERCASE).build();
        return new JsonUtils(objectMapper);
    }
}
