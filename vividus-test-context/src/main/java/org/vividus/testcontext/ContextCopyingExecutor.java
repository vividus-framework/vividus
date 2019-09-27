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

package org.vividus.testcontext;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;

public class ContextCopyingExecutor
{
    private final TestContext testContext;

    public ContextCopyingExecutor(TestContext testContext)
    {
        this.testContext = testContext;
    }

    public void execute(Runnable toRun, UncaughtExceptionHandler handler)
            throws InterruptedException, ExecutionException
    {
        Map<Object, Object> runContextData = new HashMap<>();
        testContext.copyAllTo(runContextData);
        new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
                new ContextAwawreForkJoinThreadsFactory(runContextData), handler, false).submit(toRun).get();
    }

    private final class ContextAwawreForkJoinThreadsFactory implements ForkJoinWorkerThreadFactory
    {
        private final Map<Object, Object> runContextData;

        private ContextAwawreForkJoinThreadsFactory(Map<Object, Object> runContextData)
        {
            this.runContextData = runContextData;
        }

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool)
        {
            return new ContextAwareThread(pool, runContextData);
        }
    }

    private final class ContextAwareThread extends ForkJoinWorkerThread
    {
        private final Map<Object, Object> runContextData;

        protected ContextAwareThread(ForkJoinPool pool, Map<Object, Object> runContextData)
        {
            super(pool);
            this.runContextData = runContextData;
        }

        @Override
        protected void onStart()
        {
            testContext.putAll(runContextData);
        }
    }
}
