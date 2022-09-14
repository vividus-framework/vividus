Description: System tests for Visual plugin steps

Meta:
    @epic vividus-plugin-visual

Lifecycle:
Before:
Scope: STORY
Given I am on a page with the URL '${vividus-test-site-url}/stickyHeader.html'
Examples:
|action         |firstP             |
|COMPARE_AGAINST|By.xpath((.//p)[1])|


Scenario: Validation of step When I $actionType baseline with name `$name` for full page
When I <action> baseline with name `full-page`
When I <action> baseline with name `full-page` using storage `filesystem`
!-- Deprecated step test
When I <action> baseline with name `full-page` using repository `filesystem`


Scenario: Validation of step When I $actionType baseline with name `$name` for context element
When I change context to element located `<firstP>`
When I <action> baseline with name `context`
When I reset context


Scenario: Validation of CHECK_INEQUALITY_AGAINST action
When I change context to element located `<firstP>`
When I CHECK_INEQUALITY_AGAINST baseline with name `full-page`
When I reset context


Scenario: Validation of CHECK_INEQUALITY_AGAINST action with step level parameters
When I change context to element located `<firstP>`
When I CHECK_INEQUALITY_AGAINST baseline with name `full-page` ignoring:
|REQUIRED_DIFF_PERCENTAGE|
|85                      |
When I CHECK_INEQUALITY_AGAINST baseline with name `full-page` using storage `filesystem` and ignoring:
|REQUIRED_DIFF_PERCENTAGE|
|85                      |
!-- Deprecated step test
When I CHECK_INEQUALITY_AGAINST baseline with name `full-page` using repository `filesystem` and ignoring:
|REQUIRED_DIFF_PERCENTAGE|
|85                      |
When I reset context


Scenario: Validation of step When I $actionType baseline with name `$name` ignoring:$ignoredElements for full page with element cut
When I <action> baseline with name `full-page-element-cut` ignoring:
|ELEMENT |
|<firstP>|


Scenario: Validation of step When I $actionType baseline with name `$name` ignoring:$ignoredElements for full page with area cut
When I <action> baseline with name `full-page-area-cut` ignoring:
|AREA    |
|<firstP>|


Scenario: Validation of step When I $actionType baseline with name `$name` ignoring:$ignoredElements for context element with element cut
When I change context to element located `By.xpath(.//body)`
When I <action> baseline with name `context-element-cut` ignoring:
|ELEMENT |
|<firstP>|


Scenario: Validation of step When I $actionType baseline with `$name` ignoring:$ignoredElements for context element not in viewport with element cut
When I change context to element located `By.xpath(.//p[last()])`
When I <action> baseline with name `not-viewport-context-element-cut` ignoring:
|ELEMENT            |
|By.cssSelector(img)|


Scenario: Validation of step When I $actionType baseline with name `$name` for full page with element/area cut
When I reset context
When I <action> baseline with name `full-page-with-scroll-element-area-cut` ignoring:
|ELEMENT                                                |AREA                                                   |
|By.xpath(//p[position() mod 2 = 1 and position() > 10])|By.xpath(//p[position() mod 2 = 1 and position() < 10])|


Scenario: Validation of cut for whole page and context
When I <action> baseline with name `cuts-full-page` using screenshot configuration:
|cutTop  |cutBottom  |cutLeft|cutRight|webHeaderToCut|
|1200    |1300       |600     |1000   |100           |
When I change context to element located `<firstP>`
When I <action> baseline with name `cuts-context` using screenshot configuration:
|cutTop|cutBottom|cutLeft|cutRight|
|50    |60       |800    |1000    |


Scenario: Validation of contextual visual testing on a page with scrollable element
Given I am on a page with the URL '${vividus-test-site-url}/visualTestIntegration.html'
When I <action> baseline with name `scrollable-element-context` using screenshot configuration:
|scrollableElement|webHeaderToCut|webFooterToCut|scrollTimeout|
|By.id(scrollable)|10            |0             |PT1S         |
When I <action> baseline with name `scrollable-element-context` using storage `filesystem` and screenshot configuration:
|scrollableElement|webHeaderToCut|webFooterToCut|scrollTimeout|
|By.id(scrollable)|10            |0             |PT1S         |
!-- Deprecated step test
When I <action> baseline with name `scrollable-element-context` using repository `filesystem` and screenshot configuration:
|scrollableElement|webHeaderToCut|webFooterToCut|scrollTimeout|
|By.id(scrollable)|10            |0             |PT1S         |


Scenario: Validation of full-page visual testing on a page with scrollable element with ignores
When I <action> baseline with name `scrollable-element-fullpage-with-ignores` ignoring:
|ELEMENT                  |AREA                                   |
|By.xpath(//a[position() mod 2 = 1 and position() > 7])|By.xpath(//a[position() mod 2 = 1 and position() < 7])|
using screenshot configuration:
|scrollableElement|webHeaderToCut|webFooterToCut|scrollTimeout|
|By.id(scrollable)|10            |0             |PT1S         |
When I <action> baseline with name `scrollable-element-fullpage-with-ignores` using storage `filesystem` and ignoring:
|ELEMENT                  |AREA                                   |
|By.xpath(//a[position() mod 2 = 1 and position() > 7])|By.xpath(//a[position() mod 2 = 1 and position() < 7])|
and screenshot configuration:
|scrollableElement|webHeaderToCut|webFooterToCut|scrollTimeout|
|By.id(scrollable)|10            |0             |PT1S         |
!-- Deprecated step test
When I <action> baseline with name `scrollable-element-fullpage-with-ignores` using repository `filesystem` and ignoring:
|ELEMENT                  |AREA                                   |
|By.xpath(//a[position() mod 2 = 1 and position() > 7])|By.xpath(//a[position() mod 2 = 1 and position() < 7])|
and screenshot configuration:
|scrollableElement|webHeaderToCut|webFooterToCut|scrollTimeout|
|By.id(scrollable)|10            |0             |PT1S         |


Scenario: Validation of step When I $actionType baseline with name `$name` for context element with acceptable diff percentage
When I change context to element located `By.xpath(//a[@href="#home"])`
When I <action> baseline with name `context-element-with-acceptable-diff-percentage` ignoring:
|ACCEPTABLE_DIFF_PERCENTAGE|
|5                         |
