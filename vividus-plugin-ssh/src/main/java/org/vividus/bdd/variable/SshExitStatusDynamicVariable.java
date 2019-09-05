/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.variable;

import java.util.Optional;

import javax.inject.Named;

import org.vividus.bdd.context.SshTestContext;
import org.vividus.ssh.exec.SshOutput;

@Named("ssh-exit-status")
public class SshExitStatusDynamicVariable implements DynamicVariable
{
    private final SshTestContext sshTestContext;

    public SshExitStatusDynamicVariable(SshTestContext sshTestContext)
    {
        this.sshTestContext = sshTestContext;
    }

    @Override
    public String getValue()
    {
        return Optional.ofNullable(sshTestContext.getSshOutput()).map(SshOutput::getExitStatus).map(Object::toString)
                .orElse(null);
    }
}
