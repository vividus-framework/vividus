Description: Integration tests for SslSteps functionality

Meta:
    @epic vividus-plugin-rest-api

Scenario: Verify step: "Then server `$hostname` supports secure protocols that $rule `$protocols`"
Meta:
    @requirementId 681
Then server `vividus-test-site.herokuapp.com` supports secure protocols that contain `TLSv1.3,TLSv1.2`
