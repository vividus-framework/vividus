Meta:
    @feature fail-fast

Scenario: Empty scenario

Scenario: Should not execute second step if scenario level fail-fast enabled
Then `20` is = `77`
Then `1` is = `3`


Scenario: Should not execute this scenario if story-level fail-fast enabled
Then `1` is = `9`
