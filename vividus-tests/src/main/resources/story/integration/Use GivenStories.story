Meta:
    @epic vividus-bdd-engine

GivenStories: /story/integration/Precondition.story

Lifecycle:
Examples:
|story-left |story-right|
|story-true |story-true |
|story-false|story-false|

Scenario: This scenario should run after story-level GivenStories
Then `base-true-1` is equal to `base-true-1`

Scenario: This scenario uses story-level Examples
Then `<story-left>` is equal to `<story-right>`

Scenario: This scenario should run story-level and scenario-level GivenStories
GivenStories: /story/integration/Precondition.story
Then `base-true-2` is equal to `base-true-2`

Scenario: This scenario should run story-level and filtered scenario-level GivenStories
GivenStories: /story/integration/Precondition2.story#{id:scenario-to-run}
Then `base-true-3` is equal to `base-true-3`

Scenario: This scenario should run story-level and parametrized scenario-level GivenStories
GivenStories: /story/integration/Precondition3.story#{0},
              /story/integration/Precondition3.story#{2}
Then `base-true-4` is equal to `base-true-4`
Examples:
|left       |right     |
|true       |true      |
|this should|be skipped|
|false      |false     |

Scenario: This scenario with Examples should run story-level and scenario-level GivenStories
GivenStories: /story/integration/Precondition.story
Then `<left>` is equal to `<right>`
Examples:
|left       |right     |
|true       |true      |
