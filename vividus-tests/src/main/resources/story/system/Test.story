Meta:
    @myMeta

Scenario: test should pass
Given I initialize SCENARIO variable `actualResponse` with value `[{"id":1,"name": "book1"},{"id": 2,"name": "book2"},{"id": 3,"name": "book3"}]`
And I initialize SCENARIO variable `expectedResponse` with value `[{"id":1,"name": "book1"},{"id": 3,"name": "book3"}]`
Then JSON element value from `${actualResponse}` by JSON path `$.*` contain `${expectedResponse}`

Scenario: test should fail
Given I initialize SCENARIO variable `actualResponse` with value `[{"id":1,"name": "book1"},{"id": 2,"name": "book2"},{"id": 3,"name": "book3"}]`
And I initialize SCENARIO variable `expectedResponse` with value `[{"id":1,"name": "book1"},{"id": 3,"name": "book4"}]`
Then JSON element value from `${actualResponse}` by JSON path `$.*` contain `${expectedResponse}`
