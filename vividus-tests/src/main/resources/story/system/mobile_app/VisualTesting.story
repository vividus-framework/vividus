Meta:
    @epic vividus-plugin-visual

Lifecycle:
Examples:
|action         |
|COMPARE_AGAINST|

Scenario: Verify step: 'Given I start mobile application with capabilities:$capabilities'
Given I start mobile application with capabilities:
|name|value     |
|app |${app-url}|


Scenario: Step verification: When I $actionType baseline with `$name`
When I <action> baseline with `${target-platform}-full-page`


Scenario: Step verification: When I $actionType baseline with `$name` ignoring:$checkSettings
When I <action> baseline with `${target-platform}-<cut-type>-ignore` ignoring:
|<cut-type>                                                |
|xpath(//XCUIElementTypeOther[./XCUIElementTypeImage])|

Examples:
|cut-type|
|ELEMENT |
|AREA    |


Scenario: Step verification: When I $actionType baseline with `$name` using screenshot configuration:$screenshotConfiguration
When I <action> baseline with `${target-platform}-custom-config` using screenshot configuration:
|nativeFooterToCut|
|100              |


Scenario: Step verification: When I $actionType baseline with `$name` ignoring:$checkSettings using screenshot configuration:$screenshotConfiguration
When I <action> baseline with `${target-platform}-custom-config-<cut-type>-ignore` ignoring:
|<cut-type>                                           |
|xpath(//XCUIElementTypeOther[./XCUIElementTypeImage])|
using screenshot configuration:
|nativeFooterToCut|
|100              |

Examples:
|cut-type|
|ELEMENT |
|AREA    |
