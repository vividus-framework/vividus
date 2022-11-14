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

package org.vividus.visual.engine;

import java.awt.Color;

import pazone.ashot.comparison.DiffMarkupPolicy;
import pazone.ashot.comparison.ImageMarkupPolicy;

public class DiffMarkupPolicyFactory
{
    private static final Color DIFF_COLOR = new Color(238, 111, 238);
    private static final int ONE_HUNDRED = 100;

    public DiffMarkupPolicy create(int imageHeight, int imageWidth, double diffPercentage)
    {
        ImageMarkupPolicy imageMarkupPolicy = new ImageMarkupPolicy();
        imageMarkupPolicy.setDiffSizeTrigger((int) (imageHeight * imageWidth * diffPercentage / ONE_HUNDRED));
        return imageMarkupPolicy.withDiffColor(DIFF_COLOR);
    }
}
