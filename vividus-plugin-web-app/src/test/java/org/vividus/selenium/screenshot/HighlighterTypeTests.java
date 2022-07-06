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

package org.vividus.selenium.screenshot;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pazone.ashot.cropper.indent.BlurFilter;
import pazone.ashot.cropper.indent.IndentCropper;
import pazone.ashot.cropper.indent.MonochromeFilter;

@ExtendWith(MockitoExtension.class)
class HighlighterTypeTests
{
    @Mock
    private IndentCropper indentCropper;

    @Test
    void testDefault()
    {
        HighlighterType.DEFAULT.addIndentFilter(indentCropper);
        verifyNoInteractions(indentCropper);
    }

    @Test
    void testMonochrome()
    {
        HighlighterType.MONOCHROME.addIndentFilter(indentCropper);
        verify(indentCropper).addIndentFilter(isA(MonochromeFilter.class));
    }

    @Test
    void testBlur()
    {
        HighlighterType.BLUR.addIndentFilter(indentCropper);
        verify(indentCropper).addIndentFilter(isA(BlurFilter.class));
    }
}
