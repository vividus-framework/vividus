/*
 * Copyright 2021 the original author or authors.
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

package org.vividus.bdd.model;

import java.util.concurrent.atomic.AtomicLong;

public class Statistic
{
    private final AtomicLong total = new AtomicLong();

    private final AtomicLong passed = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private final AtomicLong broken = new AtomicLong();
    private final AtomicLong skipped = new AtomicLong();
    private final AtomicLong pending = new AtomicLong();
    private final AtomicLong knownIssue = new AtomicLong();

    public long getTotal()
    {
        return total.get();
    }

    public long getPassed()
    {
        return passed.get();
    }

    public void incrementPassed()
    {
        incrementTotal();
        this.passed.incrementAndGet();
    }

    public long getFailed()
    {
        return failed.get();
    }

    public void incrementFailed()
    {
        incrementTotal();
        this.failed.incrementAndGet();
    }

    public long getBroken()
    {
        return broken.get();
    }

    public void incrementBroken()
    {
        incrementTotal();
        this.broken.incrementAndGet();
    }

    public long getSkipped()
    {
        return skipped.get();
    }

    public void incrementSkipped()
    {
        incrementTotal();
        this.skipped.incrementAndGet();
    }

    public long getPending()
    {
        return pending.get();
    }

    public void incrementPending()
    {
        incrementTotal();
        this.pending.incrementAndGet();
    }

    public long getKnownIssue()
    {
        return knownIssue.get();
    }

    public void incrementKnownIssue()
    {
        incrementTotal();
        this.knownIssue.incrementAndGet();
    }

    private void incrementTotal()
    {
        this.total.incrementAndGet();
    }
}
