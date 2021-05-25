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
