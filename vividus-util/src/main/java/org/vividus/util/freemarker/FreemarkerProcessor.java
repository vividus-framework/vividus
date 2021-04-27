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

package org.vividus.util.freemarker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerProcessor
{
    private final Configuration configuration;

    public FreemarkerProcessor(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public byte[] process(String templateName, Object dataModel) throws IOException, TemplateException
    {
        Template template = getTemplate(templateName);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream,
                        StandardCharsets.UTF_8.name()))
        {
            template.process(dataModel, outputStreamWriter);
            return byteArrayOutputStream.toByteArray();
        }
    }

    public String process(String templateName, Object dataModel, Charset charset) throws IOException, TemplateException
    {
        byte[] data = process(templateName, dataModel);
        return new String(data, charset);
    }

    public Template getTemplate(String templateName) throws IOException
    {
        return configuration.getTemplate(templateName);
    }
}
