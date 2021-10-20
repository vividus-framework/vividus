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

package org.vividus.azure.util;

import com.azure.core.util.serializer.JacksonAdapter;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

public class InnersJacksonAdapter extends JacksonAdapter
{
    public InnersJacksonAdapter()
    {
        super((outer, inner) -> {
            JacksonAnnotationIntrospector annotationIntrospector = new JacksonAnnotationIntrospector()
            {
                @Override
                public Access findPropertyAccess(Annotated annotated)
                {
                    Access access = super.findPropertyAccess(annotated);
                    return access == Access.WRITE_ONLY ? Access.AUTO : access;
                }
            };
            outer.setAnnotationIntrospector(annotationIntrospector);
            inner.setAnnotationIntrospector(annotationIntrospector);
        });
    }
}
