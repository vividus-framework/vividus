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

package org.vividus.bdd.expression;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;
import java.util.Optional;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated use set of #{encodeUriXyz(..)} expressions instead
 */
@Named
@Deprecated(forRemoval = true, since = "0.2.7")
public class EncodeUrlExpressionProcessor extends UnaryExpressionProcessor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(EncodeUrlExpressionProcessor.class);

    public EncodeUrlExpressionProcessor()
    {
        super("encodeUrl", input -> URLEncoder.encode(input, UTF_8));
    }

    @Override
    public Optional<String> execute(String expression)
    {
        LOGGER.warn("#{encodeUrl(..)} expression is deprecated, use set of #{encodeUri<Part>(..)} expressions instead");
        return super.execute(expression);
    }
}
