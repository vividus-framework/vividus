Description: Integration tests for Visual plugin steps

Meta:
    @group vividus-plugin-visual

Lifecycle:
Examples:
|action         |firstP             |
|COMPARE_AGAINST|By.xpath((.//p)[1])|


Scenario: Set-Up
Given I am on a page with the URL 'https://vividus-test-site.herokuapp.com/stickyHeader.html'


Scenario: Validation of step When I $actionType baseline with `$name` for full page
When I <action> baseline with `full-page`


Scenario: Validation of step When I $actionType baseline with `$name` for context element
When I change context to an element by <firstP>
When I <action> baseline with `context`
When I change context to the page


Scenario: Validation of step When I $actionType baseline with `$name` ignoring:$ignoredElements for full page with element cut
When I <action> baseline with `full-page-element-cut` ignoring:
|ELEMENT         |
|<firstP>  |


Scenario: Validation of step When I $actionType baseline with `$name` ignoring:$ignoredElements for full page with area cut
When I <action> baseline with `full-page-area-cut` ignoring:
|AREA            |
|<firstP>  |


Scenario: Validation of step When I $actionType baseline with `$name` ignoring:$ignoredElements for context element with element cut
When I change context to an element by By.xpath(.//body)
When I <action> baseline with `context-element-cut` ignoring:
|ELEMENT         |
|<firstP>  |


Scenario: Validation of step When I $actionType baseline with `$name` ignoring:$ignoredElements for context element not in viewport with element cut
When I change context to an element by By.xpath(.//p[last()])
When I perform javascript 'return document.querySelector('p:last-child').getBoundingClientRect();' and save result to the 'scenario' variable 'rect'
When I <action> baseline with `not-viewport-context-element-cut` ignoring:
|ELEMENT                                                                                     |
|By.cssSelector(img)|


Scenario: Validation of step When I $actionType baseline with `$name` for full page with element/area cut
When I change context to the page
When I <action> baseline with `full-page-with-scroll-element-area-cut` ignoring:
|ELEMENT                                                |AREA                                                   |
|By.xpath(//p[position() mod 2 = 1 and position() > 10])|By.xpath(//p[position() mod 2 = 1 and position() < 10])|


Scenario: Validation of contextual visual testing on a page with scrollable element
Given I am on a page with the URL 'https://vividus-test-site.herokuapp.com//visualTestIntegration.html'
When I <action> baseline with `scrollable-element-context` using screenshot configuration:
|scrollableElement|webHeaderToCut|webFooterToCut|scrollTimeout|
|By.id(scrollable)|10            |0             |PT1S         |


Scenario: Validation of full-page visual testing on a page with scrollable element with ignores
When I <action> baseline with `scrollable-element-fullpage-with-ignores` ignoring:
|ELEMENT                  |AREA                                   |
|By.xpath(//a[position() mod 2 = 1 and position() > 7])|By.xpath(//a[position() mod 2 = 1 and position() < 7])|
using screenshot configuration:
|scrollableElement|webHeaderToCut|webFooterToCut|scrollTimeout|
|By.id(scrollable)|10            |0             |PT1S         |
