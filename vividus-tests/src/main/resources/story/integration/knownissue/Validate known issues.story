Meta:
    @epic vividus-core
    @feature known-issues

Scenario: Validate the variable initialized in the scenario of story from the previous batch
Meta:
    @issueId 2276
Then `${from-next-known-issue-story}` is equal to `scenario-executed`
