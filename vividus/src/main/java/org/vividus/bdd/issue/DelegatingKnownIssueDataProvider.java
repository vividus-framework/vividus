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

package org.vividus.bdd.issue;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.softassert.issue.IKnownIssueDataProvider;
import org.vividus.softassert.issue.KnownIssueDataProvider;

public class DelegatingKnownIssueDataProvider implements KnownIssueDataProvider
{
    @Inject
    private IBddVariableContext bddVariableContext;
    private Map<String, IKnownIssueDataProvider> knownIssueDataProviders;

    @Override
    public Optional<String> getData(String key)
    {
        return Optional.ofNullable(knownIssueDataProviders.get(key))
                .map(IKnownIssueDataProvider::getData)
                .orElseGet(() -> Optional.ofNullable(bddVariableContext.getVariable(key)));
    }

    public void setKnownIssueDataProviders(Map<String, IKnownIssueDataProvider> knownIssueDataProviders)
    {
        this.knownIssueDataProviders = knownIssueDataProviders;
    }
}
