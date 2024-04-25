Meta:
    @epic vividus-plugin-csv

Scenario: Validate step: "When I save CSV `$csv` to $scopes variable `$variableName`" with non-default delimiter char
Given I initialize scenario variable `csv-as-string` with value `key1;key2;key3
val1-1;val1-2;val1-3
val2-1;val2-2;val2-3`
When I save CSV `${csv-as-string}` to scenario variable `csv`
Then `${csv[1].key2}` is equal to `val2-2`
