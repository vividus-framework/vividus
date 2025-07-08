Meta:
    @requirementId 4864

Scenario: Authorization test using the "httpbingo" basic authentication configuration
Given I am on page with URL `https://httpbingo.org`
When I click on element located by `linkUrl(/basic-auth/user/password)`
Then text `"authorized": true` exists
