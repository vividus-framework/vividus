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

package org.vividus.ssh.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.vividus.ssh.SshConnectionParameters;
import org.vividus.ssh.exec.SshOutput;
import org.vividus.testcontext.TestContext;

public class SshTestContext
{
    private static final Object KEY = SshTestContextData.class;

    private final TestContext testContext;

    public SshTestContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    public void addDynamicConnectionParameters(String connectionKey, SshConnectionParameters sshConnectionParameters)
    {
        getData().dynamicConnectionParameters.put(connectionKey, sshConnectionParameters);
    }

    public Optional<SshConnectionParameters> getDynamicConnectionParameters(String connectionKey)
    {
        return Optional.ofNullable(getData().dynamicConnectionParameters.get(connectionKey));
    }

    public SshOutput getSshOutput()
    {
        return getData().sshOutput;
    }

    public void putSshOutput(SshOutput sshOutput)
    {
        getData().sshOutput = sshOutput;
    }

    private SshTestContextData getData()
    {
        return testContext.get(KEY, SshTestContextData::new);
    }

    private static class SshTestContextData
    {
        private final Map<String, SshConnectionParameters> dynamicConnectionParameters = new HashMap<>();

        private SshOutput sshOutput;
    }
}
