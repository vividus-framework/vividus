Meta:
    @epic vividus-bdd-engine

GivenStories: /story/integration/Precondition.story

Scenario: This scenario should run after story-level GivenStories
Then `base-true` is equal to `base-true`

Scenario: This scenario should run story-level and scenario-level GivenStories
GivenStories: /story/integration/Precondition.story
Then `base-true` is equal to `base-true`
