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

package org.vividus.email.service;

import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang3.function.FailablePredicate;
import org.vividus.email.model.EmailMessage;
import org.vividus.email.model.EmailServerConfiguration;
import org.vividus.email.service.ImapFetchService.EmailFetchServiceException;

public interface EmailFetchService
{
    List<EmailMessage> fetch(List<FailablePredicate<Message, MessagingException>> messageFilters,
            EmailServerConfiguration configuration) throws EmailFetchServiceException;
}
