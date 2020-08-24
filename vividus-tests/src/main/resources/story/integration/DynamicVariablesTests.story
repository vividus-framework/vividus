Description: Story validates dynamic variables

Meta:
    @epic vividus-plugin-web-app
    @feature dynamic-variables

Scenario: Set-Up
Given I am on a page with the URL '${vividus-test-site-url}'


Scenario: Verify context' rectangle dynamic variables
Meta:
    @requirementId 802
When I change context to element located `tagName(img)`
Then `${context-height}`            is > `0`
Then `${context-width}`             is > `0`
Then `${context-x-coordinate}`      is > `0`
Then `${context-y-coordinate}`      is > `0`
