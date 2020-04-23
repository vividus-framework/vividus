Meta:
    @epic vividus-bdd-engine

GivenStories: /story/integration/KnownIssues.story

Scenario: This scenario should never run, because GivenStory is failed and `bdd.configuration.skip-story-if-given-story-failed=true`
Then `true` is equal to `false`
