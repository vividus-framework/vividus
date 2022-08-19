Meta:
    @epic vividus-plugin-visual

Lifecycle:
Examples:
/data/tables/system/mobile_app/locators/${target-platform}.table


Scenario: [iOS] Verify that dpr is not cached (dpr for iPhone 7 Simulator is 2.0)
Meta:
    @targetPlatform ios
Given I start mobile application with capabilities:
|name       |value             |
|app        |${app-url}        |
|deviceName |iPhone 7 Simulator|
When I ESTABLISH baseline with name `ios-low-dpr`
When I close mobile application


Scenario: Verify step: 'Given I start mobile application with capabilities:$capabilities'
Meta:
    @targetPlatform ios
Given I start mobile application with capabilities:
|name|value     |
|app |${app-url}|


Scenario: Verify step: 'Given I start mobile application with capabilities:$capabilities'
Meta:
    @targetPlatform android
Given I start mobile application with capabilities:
|name           |value     |
|app            |${app-url}|
|platformVersion|10.0      |
!-- Platform version 10 is using here as AndroidDriver getSystemBars() returns wrong values for Android 12.
!-- https://github.com/appium/appium/issues/16390. It leads to failed visual check. Should be fixed in Appium 1.22.3,
!-- which is not supported by SauceLab yet.


Scenario: Step verification: When I $actionType baseline with name `$name`
When I <action> baseline with name `${target-platform}-full-page`


Scenario: Step verification: When I $actionType baseline with name `$name` for the context
When I change context to element located `${element-to-ignore}`
When I <action> baseline with name `${target-platform}-context`
When I reset context


Scenario: Step verification: When I $actionType baseline with `$name` ignoring:$checkSettings
When I <action> baseline with name `${target-platform}-<cut-type>-ignore` ignoring:
|<cut-type>          |
|${element-to-ignore}|

Examples:
|cut-type|
|ELEMENT |
|AREA    |


Scenario: Step verification: When I $actionType baseline with name `$name` using screenshot configuration:$screenshotConfiguration
When I <action> baseline with name `${target-platform}-custom-config` using screenshot configuration:
|nativeFooterToCut|
|100              |


Scenario: Step verification: When I $actionType baseline with `$name` ignoring:$checkSettings using screenshot configuration:$screenshotConfiguration
When I <action> baseline with `${target-platform}-custom-config-<cut-type>-ignore` ignoring:
|<cut-type>          |
|${element-to-ignore}|
using screenshot configuration:
|nativeFooterToCut|
|33               |

Examples:
|cut-type|
|ELEMENT |
|AREA    |


Scenario: Verify contextual check with ignored element
When I tap on element located `accessibilityId(menuToggler)`
When I tap on element located `xpath(<menuScrollViewXpath>)`
When I wait until element located `xpath(<scrollViewXpath>)` appears
When I change context to element located `xpath(<scrollViewXpath>)`
When I <action> baseline with name `${target-platform}-context-with-ignore` ignoring:
|ELEMENT                   |
|By.accessibilityId(header)|
