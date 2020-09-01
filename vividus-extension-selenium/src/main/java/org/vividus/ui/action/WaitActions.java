/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.ui.action;

import java.time.Duration;
import java.util.function.Function;

import javax.inject.Inject;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.Sleeper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class WaitActions implements IWaitActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitActions.class);

    @Inject private ISoftAssert softAssert;
    @Inject private IWaitFactory waitFactory;

    @Override
    public <T, V> WaitResult<V> wait(T input, Function<T, V> isTrue)
    {
        return wait(input, isTrue, true);
    }

    @Override
    public <T, V> WaitResult<V> wait(T input, Function<T, V> isTrue, boolean recordAssertionIfTimeout)
    {
        return wait(input, waitFactory::createWait, isTrue, recordAssertionIfTimeout);
    }

    @Override
    public <T, V> WaitResult<V> wait(T input, Duration timeout, Function<T, V> isTrue)
    {
        return wait(input, timeout, isTrue, true);
    }

    @Override
    public <T, V> WaitResult<V> wait(T input, Duration timeout, Function<T, V> isTrue, boolean recordAssertionIfTimeout)
    {
        return wait(input, i -> waitFactory.createWait(i, timeout), isTrue,
                recordAssertionIfTimeout);
    }

    @Override
    public <T, V> WaitResult<V> wait(T input, Duration timeout, Duration pollingPeriod, Function<T, V> isTrue)
    {
        return wait(input, timeout, pollingPeriod, isTrue, true);
    }

    @Override
    public <T, V> WaitResult<V> wait(T input, Duration timeout, Duration pollingPeriod, Function<T, V> isTrue,
            boolean recordAssertionIfTimeout)
    {
        return wait(input, i -> waitFactory.createWait(i, timeout, pollingPeriod), isTrue, recordAssertionIfTimeout);
    }

    @SuppressFBWarnings("NP_LOAD_OF_KNOWN_NULL_VALUE")
    private <T, V> WaitResult<V> wait(T input, Function<T, Wait<T>> waitSupplier, Function<T, V> isTrue,
            boolean recordAssertionIfTimeout)
    {
        WaitResult<V> result = new WaitResult<>();
        if (input != null)
        {
            Wait<T> wait = waitSupplier.apply(input);
            try
            {
                V value = wait.until(isTrue);
                result.setWaitPassed(true);
                LOGGER.debug(wait.toString());
                result.setData(value);
                return result;
            }
            catch (TimeoutException timeoutException)
            {
                if (recordAssertionIfTimeout)
                {
                    recordFailedAssertion(wait, timeoutException);
                }
                return result;
            }
            catch (NoSuchElementException noSuchElementException)
            {
                recordFailedAssertion(wait, noSuchElementException);
                return result;
            }
        }
        result.setWaitPassed(softAssert.assertNotNull("The input value to pass to the wait condition", input));
        return result;
    }

    @Override
    public void sleepForTimeout(Duration time)
    {
        Sleeper.sleep(time);
    }

    private boolean recordFailedAssertion(Wait<?> wait, WebDriverException e)
    {
        return softAssert.recordFailedAssertion(wait + ". Error: " + e.getMessage());
    }
}
