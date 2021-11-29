Meta:
    @epic vividus-engine
    @feature variables

Scenario: Should use Examples Table from temporary local file
Then `<name>` is equal to `testValue`
Examples:
file:///${examples-table-temporary-file}
