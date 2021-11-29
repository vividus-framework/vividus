Meta:
    @epic vividus-engine

Scenario: Precondition
Meta:
    @id scenario-to-run
Then `precondition-true` is equal to `precondition-true`

Scenario: Precondition to filter
Meta:
    @id scenario-to-filter
Then `precondition-true` is equal to `precondition-false`
