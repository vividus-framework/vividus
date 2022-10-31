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

package org.vividus.mobitru.client.model;

public class Application
{
    private String id;
    private String realName;
    private String uploadedBy;
    private long uploadedAt;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    public String getUploadedBy()
    {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy)
    {
        this.uploadedBy = uploadedBy;
    }

    public long getUploadedAt()
    {
        return uploadedAt;
    }

    public void setUploadedAt(long uploadedAt)
    {
        this.uploadedAt = uploadedAt;
    }

    @Override
    public String toString()
    {
        return "Application{" + "id='" + id + '\'' + ", realName='" + realName + '\'' + ", uploadedBy='" + uploadedBy
                + '\'' + ", uploadedAt=" + uploadedAt + '}';
    }
}
