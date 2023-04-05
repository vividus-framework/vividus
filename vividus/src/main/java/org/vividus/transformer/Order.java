/*
 * Copyright 2019-2023 the original author or authors.
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
import java.util.function.IntUnaryOperator;

enum Order
{
    ASCENDING
    {
        @Override
        IntUnaryOperator getIndexer(List<?> rows)
        {
            return i -> i;
        }

        @Override
        int getDirection()
        {
            return 1;
        }
    },
    DESCENDING
    {
        @Override
        IntUnaryOperator getIndexer(List<?> rows)
        {
            int size = rows.size();
            return i -> size - i - 1;
        }

        @Override
        int getDirection()
        {
            return -1;
        }
    };

    abstract IntUnaryOperator getIndexer(List<?> rows);

    abstract int getDirection();
}
