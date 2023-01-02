Description: Story validates dynamic variables

Meta:
    @epic vividus-plugin-web-app
    @feature dynamic-variables

Lifecycle:
Before:
Scope: STORY
Given I am on page with URL `${vividus-test-site-url}`


Scenario: Verify context' rectangle dynamic variables
Meta:
    @requirementId 802
When I change context to element located by `tagName(img)`
Then `${context-height}`            is > `0`
Then `${context-width}`             is > `0`
Then `${context-x-coordinate}`      is > `0`
Then `${context-y-coordinate}`      is > `0`


Scenario: Verify `source-code` dynamic variable
Then `${source-code}` matches `.+Vividus Logo.+`

Scenario: Verify browser windows size dynamic variables
When I change window size to `600x500`
Then `${browser-window-height}` is = `500`
Then `${browser-window-width}`  is = `600`
