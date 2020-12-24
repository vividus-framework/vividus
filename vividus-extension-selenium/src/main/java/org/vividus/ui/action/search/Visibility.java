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

package org.vividus.ui.action.search;

import java.util.stream.Stream;

import org.vividus.ui.State;

public enum Visibility
{
    VISIBLE(State.VISIBLE, "visible"),
    INVISIBLE(State.NOT_VISIBLE, "invisible"),
    ALL(null, "visible or invisible");

    private final State state;
    private final String description;

    Visibility(State state, String description)
    {
        this.state = state;
        this.description = description;
    }

    public State getState()
    {
        return state;
    }

    public String getDescription()
    {
        return description;
    }

    public static Visibility getElementType(String input)
    {
        String inputInUpperCase = input.toUpperCase().trim();
        return Stream.of(values())
                .filter(v -> v.name().startsWith(inputInUpperCase))
                .findFirst()
                .orElseThrow(() ->
                new IllegalArgumentException(
                        String.format("Illegal visibility type '%s'. Expected one of visible, invisible, all", input)));
    }
}
