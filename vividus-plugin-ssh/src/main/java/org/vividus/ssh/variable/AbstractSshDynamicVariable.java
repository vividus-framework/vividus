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

import java.util.Optional;
import java.util.function.Function;

import org.vividus.context.SshTestContext;
import org.vividus.ssh.exec.SshOutput;
import org.vividus.variable.DynamicVariable;
import org.vividus.variable.DynamicVariableCalculationResult;

public abstract class AbstractSshDynamicVariable implements DynamicVariable
{
    private final SshTestContext sshTestContext;
    private final Function<SshOutput, String> valueMapper;

    protected AbstractSshDynamicVariable(SshTestContext sshTestContext, Function<SshOutput, String> valueMapper)
    {
        this.sshTestContext = sshTestContext;
        this.valueMapper = valueMapper;
    }

    @Override
    public DynamicVariableCalculationResult calculateValue()
    {
        return DynamicVariableCalculationResult.withValueOrError(
                Optional.ofNullable(sshTestContext.getSshOutput()).map(valueMapper),
                () -> "no SSH commands were executed"
        );
    }
}
