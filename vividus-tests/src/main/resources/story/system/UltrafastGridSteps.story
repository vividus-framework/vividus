Description: System tests for UFG Applitools Visual plugin steps

Meta:
    @epic vividus-plugin-applitools

Lifecycle:
Before:
Scope: STORY
Given I am on page with URL `${vividus-test-site-url}/stickyHeader.html`
Examples:
|action         |firstP             |batchName           |
|COMPARE_AGAINST|xpath((.//p)[1])   |Vividus System Tests|


Scenario: Validation of step: 'When I $actionType baseline `$testName` in batch `$batchName` with Applitools UFG using matrix:$matrix' for full page
When I <action> baseline `ufg-full-page` in batch `<batchName>` with Applitools UFG using matrix:
|profile                |browser|viewportSize|deviceName|
|desktop                |chrome |1920x1080   |          |
|chrome_mobile_emulation|       |            |Galaxy S10|


Scenario: Validation of step: 'When I $actionType baseline `$testName` in batch `$batchName` with Applitools UFG using matrix:$matrix' for context element
When I change context to element located by `<firstP>`
When I <action> baseline `ufg-context` in batch `<batchName>` with Applitools UFG using matrix:
|profile                |browser|viewportSize|deviceName|
|desktop                |chrome |1920x1080   |          |
|ios                    |       |            |iPhone 14 |
|chrome_mobile_emulation|       |            |Galaxy S10|


Scenario: Validation of step: 'When I run visual test with Applitools UFG using:$applitoolsConfigurations and matrix:$matrix' for full page with element cut
When I reset context
When I run visual test with Applitools UFG using:
|baselineName              |batchName  |action  |elementsToIgnore|beforeRenderScreenshotHook                                       |
|ufg-full-page-element-cut |<batchName>|<action>|tagName(img)    |document.querySelector('#myHeader').style.backgroundColor = 'red'|
 and matrix:
|profile                |browser|viewportSize|deviceName|screenOrientation|version|
|desktop                |firefox|1920x1080   |          |                 |       |
|ios                    |       |            |iPhone 14 |portrait         |latest |
|chrome_mobile_emulation|       |            |Galaxy S10|landscape        |       |
When I refresh page


Scenario: Validation of step: 'When I run visual test with Applitools UFG using:$applitoolsConfigurations and matrix:$matrix' for full page with area cut
When I run visual test with Applitools UFG using:
|baselineName           |batchName  |action  |areasToIgnore|
|ufg-full-page-area-cut |<batchName>|<action>|<firstP>     |
 and matrix:
|profile                |browser|viewportSize|deviceName|screenOrientation|version|
|desktop                |firefox|1920x1080   |          |                 |       |
|ios                    |       |            |iPhone 14 |landscape        |latest |
|chrome_mobile_emulation|       |            |Galaxy S10|portrait         |       |


Scenario: Validation of step: 'When I run visual test with Applitools UFG using:$applitoolsConfigurations and matrix:$matrix' for context element with element cut
When I change context to element located by `xpath(.//body)`
When I run visual test with Applitools UFG using:
|baselineName            |batchName  |action  |elementsToIgnore|
|ufg-context-element-cut |<batchName>|<action>|tagName(img)    |
 and matrix:
|profile                |browser|viewportSize|deviceName|
|desktop                |firefox|1920x1080   |          |
|ios                    |       |            |iPhone 14 |
|chrome_mobile_emulation|       |            |Galaxy S10|
