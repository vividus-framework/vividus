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

package org.vividus.azure.servicebus;

import java.io.Serial;

public class ServiceBusSendMessageException extends RuntimeException
{
    @Serial
    private static final long serialVersionUID = 4430995732032922294L;

    public ServiceBusSendMessageException(String message, Throwable throwable)
    {
        super(message, throwable);
    }
}
