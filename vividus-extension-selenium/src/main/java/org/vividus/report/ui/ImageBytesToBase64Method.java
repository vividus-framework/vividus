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

package org.vividus.report.ui;

import java.util.Base64;
import java.util.List;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class ImageBytesToBase64Method implements TemplateMethodModelEx
{
    private final ImageCompressor imageCompressor;

    public ImageBytesToBase64Method(ImageCompressor imageCompressor)
    {
        this.imageCompressor = imageCompressor;
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException
    {
        byte[] image = (byte[]) ((freemarker.template.DefaultArrayAdapter) arguments.get(0))
                .getAdaptedObject(byte[].class);
        return Base64.getEncoder().encodeToString(imageCompressor.compress(image));
    }
}
