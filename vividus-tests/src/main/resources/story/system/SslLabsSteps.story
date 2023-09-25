Description: System tests for SslLabsSteps class

Meta:
    @epic vividus-plugin-rest-to-web-api

Scenario: Step verification "Then SSL rating for URL `$url` is $comparisonRule `$gradeName`"
Then SSL rating for URL `${vividus-test-site-url}` is equal to `A`
