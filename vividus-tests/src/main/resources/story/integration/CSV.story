Meta:
    @epic vividus-plugin-csv

Scenario: Verify FROM_CSV transformer
Meta:
    @feature table-transformers
Then `<country>` is equal to `Belarus`
Then `<capital>` is equal to `Minsk`
Then `<data>` is equal to `{"sheet": [{"cols": 1, "name": "A", "rows": 2}], "name": "tests"}`
Examples:
{transformer=FROM_CSV, path=/data/csv.csv}

Scenario: Verify FROM_CSV transformer from variable
Meta:
    @feature table-transformers
Then `<country>` is equal to `Belarus`
Then `<capital>` is equal to `Minsk`
Examples:
{transformer=FROM_CSV, variableName=csv-transformer-test}

Scenario: Verify FROM_CSV transformer with deprecated property
Meta:
    @feature table-transformers
!-- Deprecated
Then `<country>` is equal to `Belarus`
Then `<capital>` is equal to `Minsk`
Then `<data>` is equal to `{"sheet": [{"cols": 1, "name": "A", "rows": 2}], "name": "tests"}`
Examples:
{transformer=FROM_CSV, csvPath=/data/csv.csv}

Scenario: Verify FROM_CSV transformer with custom delimiter
Meta:
    @feature table-transformers
Then `<country>` is equal to `Belarus`
Then `<capital>` is equal to `Minsk`
Then `<data>` is equal to `{"sheet": [{"cols": 1, "name": "A", "rows": 2}], "name": "tests"}`
Examples:
{transformer=FROM_CSV, path=/data/csv-with-semicolon.csv, delimiterChar=;}

Scenario: Execute SQL against CSV
Meta:
    @requirementId 1655
When I execute SQL query `
SELECT * FROM csv-with-semicolon-and-duplicates
` against `csv-data` and save result to scenario variable `csv-records`
Then `${csv-records}` matching rows using `country` from `csv-data` is equal to data from:
|country|capital|data                                                             |
|Belarus|Minsk  |{"sheet": [{"cols": 1, "name": "A", "rows": 2}], "name": "tests"}|
|Belarus|Minsk  |plain text                                                       |

Scenario: Verify step: "When I save CSV `$csv` to $scopes variable `$variableName`"
Given I initialize scenario variable `csv` with value `key1,key2,key3
val1-1,val1-2,val1-3
val2-1,val2-2,val2-3`
When I save CSV `${csv}` to scenario variable `csv-from-variable`
When I save CSV `#{loadResource(/data/simple-csv.csv)}` to scenario variable `csv-from-file`
Then `${csv-from-file}` is equal to `${csv-from-variable}`
Then `${csv-from-file[1].key2}` is equal to `val2-2`
