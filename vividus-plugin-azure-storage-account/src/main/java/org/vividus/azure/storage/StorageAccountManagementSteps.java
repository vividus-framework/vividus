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

package org.vividus.azure.storage;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.models.StorageAccountInner;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;

public class StorageAccountManagementSteps
{
    private final StorageManager storageManager;
    private final IBddVariableContext bddVariableContext;
    private final JacksonAdapter jacksonAdapter;

    public StorageAccountManagementSteps(AzureProfile azureProfile, TokenCredential tokenCredential,
            IBddVariableContext bddVariableContext)
    {
        this.storageManager = StorageManager.authenticate(tokenCredential, azureProfile);
        this.bddVariableContext = bddVariableContext;

        JacksonAnnotationIntrospector annotationIntrospector = new JacksonAnnotationIntrospector()
        {
            @Override
            public Access findPropertyAccess(Annotated annotated)
            {
                Access access = super.findPropertyAccess(annotated);
                return access == Access.WRITE_ONLY ? Access.AUTO : access;
            }
        };
        this.jacksonAdapter = new JacksonAdapter()
        {
            private ObjectMapper objectMapper;

            @Override
            protected ObjectMapper simpleMapper()
            {
                if (this.objectMapper == null)
                {
                    this.objectMapper = super.simpleMapper().setAnnotationIntrospector(annotationIntrospector);
                }
                return objectMapper;
            }
        };
    }

    /**
     * Collects storage accounts info in the specified resource group and saves it as JSON to a variable
     *
     * @param resourceGroupName the name of the resource group to list the storage accounts from
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the blob properties.
     */
    @When("I collect storage accounts in resource group `$resourceGroupName` and save them as JSON to $scopes variable "
            + "`$variableName`")
    public void listStorageAccounts(String resourceGroupName, Set<VariableScope> scopes, String variableName)
            throws IOException
    {
        List<StorageAccountInner> storageAccounts = storageManager.serviceClient()
                .getStorageAccounts()
                .listByResourceGroup(resourceGroupName)
                .stream()
                .collect(toList());

        bddVariableContext.putVariable(scopes, variableName,
                jacksonAdapter.serialize(storageAccounts, SerializerEncoding.JSON));
    }
}
