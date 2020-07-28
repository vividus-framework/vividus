Description: Integration tests for composite steps.

Meta:
    @epic vividus-bdd-engine
    @feature composite-steps

Scenario: Verify composite step with comment
Meta:
    @issueId 649
When I run composite step with comment
Then `${before-comment}` is equal to `before`
Then `${after-comment}` is equal to `after`
