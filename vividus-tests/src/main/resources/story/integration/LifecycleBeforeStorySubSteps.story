Meta:
    @epic vividus-core
    @feature sub-steps
    @issueId 1459

Lifecycle:
Before:
Scope: STORY
When the condition `true` is true I do
|step                                                      |
|Given I initialize story variable `name` with value `Ulad`|

Scenario: Should pass comparison
Then `${name}` is equal to `Ulad`
