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

package org.vividus.transformer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable;

public final class LeftJoinTableTransformer extends AbstractJoinTableTransformer
{
    @Override
    protected List<Map<String, String>> join(ExamplesTable leftTable, ExamplesTable rightTable,
            String leftTableJoinColumn, String rightTableJoinColumn)
    {
        Map<String, List<Map<String, String>>> rightByColumn = rightTable.getRows().stream()
                .collect(Collectors.groupingBy(m -> m.get(rightTableJoinColumn)));

        Map<String, String> emptyRightRow = rightTable.getHeaders().stream()
                .filter(h -> !leftTable.getHeaders().contains(h))
                .collect(Collectors.toMap(Function.identity(), k -> StringUtils.EMPTY));

        return leftTable.getRows().stream()
                .map(leftRow -> Optional.ofNullable(rightByColumn.get(leftRow.get(leftTableJoinColumn)))
                        .map(rows -> rows.stream().map(rightRow -> joinMaps(leftRow, rightRow)))
                        .orElseGet(() -> Stream.of(joinMaps(leftRow, emptyRightRow))))
                .flatMap(Function.identity()).toList();
    }
}
