Meta:
    @epic vividus-engine

GivenStories: /story/integration/knownissue/Known issues.story

Scenario: This scenario should never run, because GivenStory is failed and `bdd.configuration.skip-story-if-given-story-failed=true`
Then `true` is equal to `false`
