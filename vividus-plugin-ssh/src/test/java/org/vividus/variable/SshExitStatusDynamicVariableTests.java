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

package org.vividus.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.SshTestContext;
import org.vividus.ssh.exec.SshOutput;

@ExtendWith(MockitoExtension.class)
class SshExitStatusDynamicVariableTests
{
    @Mock
    private SshTestContext sshTestContext;

    @InjectMocks
    private SshExitStatusDynamicVariable variable;

    @Test
    void shouldReturnExitStatus()
    {
        SshOutput sshOutput = new SshOutput();
        sshOutput.setExitStatus(1);
        when(sshTestContext.getSshOutput()).thenReturn(sshOutput);
        assertEquals(Integer.toString(sshOutput.getExitStatus()), variable.getValue());
    }

    @Test
    void shouldReturnNullWhenNoDataIsAvailable()
    {
        when(sshTestContext.getSshOutput()).thenReturn(null);
        assertNull(variable.getValue());
    }
}
