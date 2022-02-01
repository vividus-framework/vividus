package org.vividus.reportportal.jbehave;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.epam.reportportal.jbehave.ReportPortalStoryReporter;

import org.jbehave.core.reporters.DelegatingStoryReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.springframework.cglib.proxy.Proxy;

public class LazyInitializingDelegatingStoryReporter extends DelegatingStoryReporter
{
    public LazyInitializingDelegatingStoryReporter(Supplier<ReportPortalStoryReporter> reporter)
    {
        super((Collection<StoryReporter>) Proxy.newProxyInstance(
                LazyInitializingDelegatingStoryReporter.class.getClassLoader(), new Class[] { Collection.class },
                (o, method, objects) -> {
                    return method.invoke(List.of(reporter.get()), objects);
                }));
    }
}
