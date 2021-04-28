/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.accessibility.model;

public class AccessibilityViolation
{
    private String code;
    private String context;
    private String selector;
    private String message;
    private ViolationLevel type;
    private int typeCode;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getContext()
    {
        return context;
    }

    public void setContext(String context)
    {
        this.context = context;
    }

    public String getSelector()
    {
        return selector;
    }

    public void setSelector(String selector)
    {
        this.selector = selector;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public ViolationLevel getType()
    {
        return type;
    }

    public void setType(ViolationLevel type)
    {
        this.type = type;
    }

    public int getTypeCode()
    {
        return typeCode;
    }

    public void setTypeCode(int typeCode)
    {
        this.typeCode = typeCode;
    }
}
