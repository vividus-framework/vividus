Meta:
    @epic vividus-engine
    @feature aliases

Scenario: Verify aliases loaded from resources

!-- Vividus step
Then `1` matches `\d+`
!-- Step alias configured in JSON
Then `1` matches the regular expression `\d+`
