Meta:
    @epic vividus-engine
    @feature variables

Scenario: Create temporary local file for next batch
When I create temporary file with name `.table` and content `
|name     |
|testValue|
` and put path to NEXT_BATCHES variable `examples-table-temporary-file`
