Meta:
    @epic vividus-plugin-rest-api

Lifecycle:
Before:
Scope: STORY
Given I am on page with URL `${vividus-test-site-url}`

Scenario: Verify step Then I verify assertions matching '$assertionsPattern'
Then page title is equal to `Vividus Test Site`
Then text `Doctor Who?!` exists
Then I verify assertions matching '.*Doctor Who.*'
Then text `Extermina-a-a-a-a-te` exists
