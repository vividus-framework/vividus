/*
 * Copyright 2019 the original author or authors.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.vividus.softassert.issue.IKnownIssueProvider;
import org.vividus.softassert.issue.KnownIssueIdentifier;
import org.vividus.util.json.ObjectMapperFactory;
import org.vividus.util.property.IPropertyParser;

public class KnownIssueProvider implements IKnownIssueProvider, ApplicationContextAware
{
    private static final String ADDITIONAL_PROPERTIES_PREFIX = "known-issue-provider.additional-parameters-%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(KnownIssueProvider.class);

    private Map<String, BddKnownIssueIdentifier> knownIssueIdentifiers;
    private ApplicationContext applicationContext;
    private IPropertyParser propertyParser;
    private String fileName;

    protected void init()
    {
        if (knownIssueIdentifiers.isEmpty())
        {
            try
            {
                loadKnownIssueIdentifiers();
            }
            catch (IOException e)
            {
                LOGGER.error("Unable to load known issue identifiers", e);
            }
        }
        List<String> errorMessages = new ArrayList<>();
        for (Entry<String, BddKnownIssueIdentifier> entry : knownIssueIdentifiers.entrySet())
        {
            BddKnownIssueIdentifier knownIssueIdentifier = entry.getValue();
            String knownIssueKey = entry.getKey();
            if (knownIssueIdentifier.getType() == null)
            {
                addErrorMessage(errorMessages, knownIssueKey, "type");
            }
            if (knownIssueIdentifier.getAssertionCompiledPattern() == null)
            {
                addErrorMessage(errorMessages, knownIssueKey, "assertionPattern");
            }
        }
        if (!errorMessages.isEmpty())
        {
            throw new IllegalArgumentException(Arrays.toString(errorMessages.toArray()));
        }
    }

    private void loadKnownIssueIdentifiers() throws IOException
    {
        String locationPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + fileName;
        Resource[] resources = applicationContext.getResources(locationPattern);
        if (resources.length > 0)
        {
            ObjectMapper objectMapper = ObjectMapperFactory.createWithCaseInsensitiveEnumDeserializer();
            MapType mapType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class,
                    BddKnownIssueIdentifier.class);
            for (Resource resource : resources)
            {
                LOGGER.debug("Loading known issue identifiers from {}", resource.getDescription());
                knownIssueIdentifiers.putAll(filter(objectMapper.readValue(resource.getInputStream(), mapType)));
            }
        }
        else
        {
            LOGGER.warn("Known issue functionality is not available. No resource is found by location pattern: {}",
                    locationPattern);
        }
    }

    private static void addErrorMessage(List<String> errorMessages, String knownIssueKey, String fieldName)
    {
        errorMessages.add("Field \"" + fieldName + "\" of known issue with key " + knownIssueKey + " is null");
    }

    private Map<String, BddKnownIssueIdentifier> filter(Map<String, BddKnownIssueIdentifier> identifiers)
    {
        return identifiers.entrySet().stream().filter(entry -> {
            Map<String, Pattern> additionalCompiledPatterns = entry.getValue().getAdditionalCompiledPatterns();
            return additionalCompiledPatterns.entrySet().stream().allMatch(patternEntry -> {
                String propertyKey = propertyParser.getPropertyValue(ADDITIONAL_PROPERTIES_PREFIX,
                        patternEntry.getKey());
                String propertyValue = propertyParser.getPropertyValue(propertyKey);
                Pattern additionalPattern = patternEntry.getValue();
                if (null != propertyValue && additionalPattern.matcher(propertyValue).matches())
                {
                    return true;
                }
                LOGGER.info(
                        "Issue with key {} filtered out by additional pattern '{}'. Actual property value is '{}'",
                        entry.getKey(), additionalPattern, propertyValue);
                return false;
            });
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, ? extends KnownIssueIdentifier> getKnownIssueIdentifiers()
    {
        return knownIssueIdentifiers;
    }

    public void setKnownIssueIdentifiers(Map<String, BddKnownIssueIdentifier> knownIssueIdentifiers)
    {
        this.knownIssueIdentifiers = knownIssueIdentifiers;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }

    public void setPropertyParser(IPropertyParser propertyParser)
    {
        this.propertyParser = propertyParser;
    }
}
