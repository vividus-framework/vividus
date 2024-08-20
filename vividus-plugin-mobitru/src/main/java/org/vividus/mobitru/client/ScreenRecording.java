/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.mobitru.client;

import java.util.Arrays;
import java.util.Objects;

public record ScreenRecording(String recordingId, byte[] content)
{
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ScreenRecording that))
        {
            return false;
        }
        return Objects.equals(recordingId, that.recordingId) && Arrays.equals(content, that.content);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(recordingId, Arrays.hashCode(content));
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder("ScreenRecording{recordingId=").append(recordingId).append(", ");
        if (content == null)
        {
            result.append("content=null");
        }
        else
        {
            result.append("contentLength=").append(content.length).append(" bytes");
        }
        return result.append('}').toString();
    }
}
