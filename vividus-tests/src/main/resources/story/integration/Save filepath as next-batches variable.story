Meta:
    @epic vividus-bdd-engine
    @feature variables

Scenario: Create temporary local file for next batch
When I create temporary file with name '.table' and content '
|name     |
|testValue|
' and put path to 'NEXT_BATCHES' variable with name 'examples-table-temporary-file'
