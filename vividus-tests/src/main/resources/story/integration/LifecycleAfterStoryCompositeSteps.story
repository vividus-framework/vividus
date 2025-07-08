Lifecycle:
After:
Scope: STORY
Given I initialize scenario variable `shouldBeInitialized` with value `check_me` inside composite step
Then `${shouldBeInitialized}` is equal to `check_me`


Scenario: Verify that composite step works correctly in "After" Lifecycle steps block after failed story
Then `invoke` is equal to `failure`
