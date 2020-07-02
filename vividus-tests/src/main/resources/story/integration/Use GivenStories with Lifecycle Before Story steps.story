Meta:
    @epic vividus-bdd-engine

GivenStories: /story/integration/Precondition.story

Lifecycle:
Before:
Scope: STORY
When I initialize the STORY variable `beforeStoryVar` with value `0`


Scenario: Parent story scenario
Then `${beforeStoryVar}` is equal to `0`
