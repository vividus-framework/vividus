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


Scenario: Verify cut for full-page for FULL_SCREEN shooting strategy
When I <action> baseline with name `${target-platform}-cuts-full-page` using screenshot configuration:
|shootingStrategy|cutTop                             |cutBottom|cutLeft|cutRight|
|FULL_SCREEN     |#{eval(400 + ${status-bar-height})}|300      |200    |100     |


Scenario: Verify cuts for full-page
When I <action> baseline with name `${target-platform}-cuts-full-page` using screenshot configuration:
|shootingStrategy  |cutTop|cutBottom|cutLeft|cutRight|
|<shootingStrategy>|400   |300      |200    |100     |
Examples:
|shootingStrategy|
|VIEWPORT        |
|SIMPLE          |
!-- SIMPLE shooting strategy is deprecated


Scenario: Verify cuts for context
When I change context to element located by `${element-to-ignore}`
When I <action> baseline with name `${target-platform}-cuts-context` using screenshot configuration:
|cutTop|cutBottom|cutLeft|cutRight|
|400   |200      |100    |200     |


Scenario: Step verification: When I $actionType baseline with name `$name` for the context
When I change context to element located by `${element-to-ignore}`
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
|cutBottom|
|100      |


Scenario: Step verification: When I $actionType baseline with name `$name` ignoring:$checkSettings using screenshot configuration:$screenshotConfiguration
When I <action> baseline with name `${target-platform}-custom-config-<cut-type>-ignore` ignoring:
|<cut-type>          |
|${element-to-ignore}|
using screenshot configuration:
|cutBottom|
|33       |

Examples:
|cut-type|
|ELEMENT |
|AREA    |


Scenario: Verify contextual check with ignored element
When I tap on element located by `accessibilityId(menuToggler)`
When I tap on element located by `xpath(<menuScrollViewXpath>)`
When I wait until element located by `xpath(<scrollViewXpath>)` appears
When I change context to element located by `xpath(<scrollViewXpath>)`
When I <action> baseline with name `${target-platform}-context-with-ignore` ignoring:
|ELEMENT                   |
|By.accessibilityId(header)|
