Meta:
    @layout desktop tablet phone

Scenario: Health check
Given I am on page with URL `${vividus-test-site-url}`
When I wait until element located by `name(vividus-logo)` appears
