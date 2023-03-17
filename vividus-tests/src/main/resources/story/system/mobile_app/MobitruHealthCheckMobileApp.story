Scenario: [Android] Start the application on device searched by device search capabilities
Meta:
    @targetPlatform android
Given I start mobile application with capabilities:
|name                              |value     |
|mobitru-device-search:type        |phone     |
|mobitru-device-search:manufacturer|SAMSUNG   |
When I close mobile application


Scenario: [iOS] Start the application on device searched by device search capabilities
Meta:
    @targetPlatform ios
Given I start mobile application with capabilities:
|name                              |value |
|mobitru-device-search:type        |tablet|
|mobitru-device-search:manufacturer|IPAD  |
|mobitru-device-search:model       |iPad  |
When I close mobile application


Scenario: Start the application
Given I start mobile application

Scenario: Decline pop-up
When I save number of elements located `<buttonLocator>` to scenario variable `button`
When I execute steps at most 5 times while variable `button` is = `1`:
|step                                                                                  |
|When I tap on element located by `<buttonLocator>`                                    |
|When I save number of elements located `<buttonLocator>` to scenario variable `button`|

Examples:
|buttonLocator                                                                                                          |
|xpath(.//*[@text = 'CANCEL' or @resource-id='android:id/button2' or @content-desc='Close tips' or @name="Don’t Allow"])|


Scenario: Validate application start
When I wait until element located by `accessibilityId(test-Username)` appears
When I take screenshot
