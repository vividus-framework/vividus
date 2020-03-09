Description: System tests for Applitools Visual plugin steps

Meta:
    @group vividus-plugin-applitools

Lifecycle:
Examples:
|action         |firstP             |batchName           |
|COMPARE_AGAINST|By.xpath((.//p)[1])|Vividus System Tests|


Scenario: Set-Up
Given I am on a page with the URL 'https://vividus-test-site.herokuapp.com/stickyHeader.html'


Scenario: Validation of step: 'When I $actionType baseline `$testName` in batch `$batchName` with Applitools' for full page
When I <action> baseline `full-page` in batch `<batchName>` with Applitools


Scenario: Validation of step: 'When I $actionType baseline `$testName` in batch `$batchName` with Applitools' for context element
When I change context to an element by <firstP>
When I <action> baseline `context` in batch `<batchName>` with Applitools

Scenario: Validation of step: 'When I run visual test with Applitools using:$applitoolsConfigurations' for full page with element cut
When I change context to the page
When I run visual test with Applitools using:
|baselineName          |batchName  |action  |elementsToIgnore|
|full-page-element-cut |<batchName>|<action>|<firstP>        |


Scenario: Validation of step: 'When I run visual test with Applitools using:$applitoolsConfigurations' for full page with area cut
When I run visual test with Applitools using:
|baselineName       |batchName  |action  |areasToIgnore|
|full-page-area-cut |<batchName>|<action>|<firstP>     |


Scenario: Validation of step: 'When I run visual test with Applitools using:$applitoolsConfigurations' for context element with element cut
When I change context to an element by By.xpath(.//body)
When I run visual test with Applitools using:
|baselineName        |batchName  |action  |elementsToIgnore|
|context-element-cut |<batchName>|<action>|<firstP>        |


Scenario: Validation of step: 'When I run visual test with Applitools using:$applitoolsConfigurations' for context element not in viewport with element cut
When I change context to an element by By.xpath(.//p[last()])
When I run visual test with Applitools using:
|baselineName                     |batchName  |action  |elementsToIgnore   |
|not-viewport-context-element-cut |<batchName>|<action>|By.cssSelector(img)|


Scenario: Validation of step: 'When I run visual test with Applitools using:$applitoolsConfigurations' for full page with element/area cut
When I change context to the page
When I run visual test with Applitools using:
|baselineName                           |batchName  |action  |elementsToIgnore                                       |areasToIgnore                                          |
|full-page-with-scroll-element-area-cut |<batchName>|<action>|By.xpath(//p[position() mod 2 = 1 and position() > 10])|By.xpath(//p[position() mod 2 = 1 and position() < 10])|

Scenario: Validation of contextual visual testing on a page with scrollable element
Given I am on a page with the URL 'https://vividus-test-site.herokuapp.com/visualTestIntegration.html'
When I run visual test with Applitools using:
|baselineName                          |batchName  |action  |
|scrollable-element-context            |<batchName>|<action>|
 and screenshot config:
|scrollableElement|webHeaderToCut|webFooterToCut|scrollTimeout|
|By.id(scrollable)|10            |0             |PT1S         |


Scenario: Validation of full-page visual testing on a page with scrollable element with ignores
When I run visual test with Applitools using:
|baselineName                            |batchName  |action  |elementsToIgnore                                      |areasToIgnore                                         |
|scrollable-element-fullpage-with-ignores|<batchName>|<action>|By.xpath(//a[position() mod 2 = 1 and position() > 7])|By.xpath(//a[position() mod 2 = 1 and position() < 7])|
 and screenshot config:
|scrollableElement|webHeaderToCut|webFooterToCut|scrollTimeout|
|By.id(scrollable)|10            |0             |PT1S         |
