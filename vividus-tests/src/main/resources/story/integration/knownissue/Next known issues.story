Meta:
    @epic vividus-core
    @feature known-issues
    @issueId 2276

Scenario: Known issue should should not stop the rest of the story if it's not marked as fail test suite fast
Then `vividus` matches `[A-Z]+`

Scenario: Should be performed
!-- Make sure the previous story "Known issues.story" doesn't affect the execution of the next story in the same thread
!-- Here we rely on the assumption that stories are executed in alphabetical order
When I initialize the next batches variable `from-next-known-issue-story` with value `scenario-executed`
