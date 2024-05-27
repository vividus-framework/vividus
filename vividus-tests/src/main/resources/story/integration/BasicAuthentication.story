Meta:
    @requirementId 4864
    @skip

Scenario: Authorization test using the "httpbingo" basic authentication configuration
Given I am on page with URL `https://httpbingo.org`
When I click on element located by `linkUrl(/basic-auth/user/passwd)`
Then text `"authorized": true` exists
