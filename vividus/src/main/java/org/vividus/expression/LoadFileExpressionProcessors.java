/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.expression;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.function.FailableFunction;
import org.jbehave.core.expressions.DelegatingExpressionProcessor;
import org.jbehave.core.expressions.SingleArgExpressionProcessor;

public final class LoadFileExpressionProcessors extends DelegatingExpressionProcessor
{
    public LoadFileExpressionProcessors()
    {
        super(List.of(
            new SingleArgExpressionProcessor<>("loadFile",
                    path -> performOnFile(path, f -> FileUtils.readFileToString(f, StandardCharsets.UTF_8))),
            new SingleArgExpressionProcessor<>("loadBinaryFile",
                    path -> performOnFile(path, FileUtils::readFileToByteArray))
        ));
    }

    private static <T> T performOnFile(String path, FailableFunction<File, T, IOException> function)
    {
        try
        {
            File file = new File(path);
            Validate.isTrue(file.isFile(), "The file by '%s' path does not exist or is a directory", path);
            return function.apply(file);
        }
        catch (IOException thrown)
        {
            throw new UncheckedIOException(thrown);
        }
    }
}
