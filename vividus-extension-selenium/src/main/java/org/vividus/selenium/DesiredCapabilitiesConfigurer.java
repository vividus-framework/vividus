package org.vividus.selenium;

import org.openqa.selenium.remote.DesiredCapabilities;

public interface DesiredCapabilitiesConfigurer
{
    void addCapabilities(DesiredCapabilities desiredCapabilities);
}
