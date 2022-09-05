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

package org.vividus.ssh.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ssh.context.SshTestContext;
import org.vividus.ssh.exec.SshOutput;
import org.vividus.variable.DynamicVariableCalculationResult;

@ExtendWith(MockitoExtension.class)
class SshStdoutDynamicVariableTests
{
    @Mock private SshTestContext sshTestContext;
    @InjectMocks private SshStdoutDynamicVariable dynamicVariable;

    @Test
    void shouldReturnOutputStream()
    {
        var sshOutput = new SshOutput();
        sshOutput.setOutputStream("output");
        when(sshTestContext.getSshOutput()).thenReturn(sshOutput);
        assertEquals(DynamicVariableCalculationResult.withValue(sshOutput.getOutputStream()),
                dynamicVariable.calculateValue());
    }

    @Test
    void shouldReturnErrorWhenNoDataIsAvailable()
    {
        when(sshTestContext.getSshOutput()).thenReturn(null);
        assertEquals(DynamicVariableCalculationResult.withError("no SSH commands were executed"),
                dynamicVariable.calculateValue());
    }
}
