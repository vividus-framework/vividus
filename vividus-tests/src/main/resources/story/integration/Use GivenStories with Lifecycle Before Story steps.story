Meta:
    @epic vividus-engine

GivenStories: /story/integration/Precondition.story

Lifecycle:
Before:
Scope: STORY
Given I initialize story variable `beforeStoryVar` with value `0`


Scenario: Parent story scenario
Then `${beforeStoryVar}` is equal to `0`
