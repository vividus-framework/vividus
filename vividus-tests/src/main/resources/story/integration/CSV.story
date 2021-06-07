Meta:
    @epic vividus-plugin-csv

Scenario: Verify FROM_CSV transformer
Meta:
    @feature table-transformers
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
{transformer=FROM_CSV, csvPath=/data/csv-with-semicolon.csv, delimiterChar=;}

Scenario: Execute SQL against CSV
When I execute SQL query `
SELECT * FROM csv-with-semicolon
` against `csv-data` and save result to scenario variable `csv-records`
Then `${csv-records}` matching rows using `country` from `csv-data` is equal to data from:
|country|capital|data                                                             |
|Belarus|Minsk  |{"sheet": [{"cols": 1, "name": "A", "rows": 2}], "name": "tests"}|
