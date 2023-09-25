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

package org.vividus.ssllabs;

import java.util.stream.Stream;

public enum Grade
{
    A_PLUS("A+", 8),
    A("A", 7),
    A_MINUS("A-", 6),
    B("B", 5),
    C("C", 4),
    D("D", 3),
    E("E", 2),
    F("F", 1),
    T("T", 0),
    M("M", -1);

    private final String gradeName;
    private final int gradeValue;

    Grade(String gradeName, int gradeValue)
    {
        this.gradeName = gradeName;
        this.gradeValue = gradeValue;
    }

    public static Grade fromString(String name)
    {
        return Stream.of(Grade.values()).filter(g -> g.gradeName.equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown grade '" + name + "'"));
    }

    public int getGradeValue()
    {
        return gradeValue;
    }

    public String getGradeName()
    {
        return gradeName;
    }
}
