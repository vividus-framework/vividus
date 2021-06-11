Meta:
    @epic vividus-plugin-db

Scenario: Wait for data from the data source to contain rows from the table
Meta:
    @issueId 1658
When I wait for 'PT10s' duration retrying 3 times while data from `
SELECT * FROM csv-with-semicolon-and-duplicates
` executed against `csv-data` contains data from:
|country|capital|data                                                             |
|Belarus|Minsk  |{"sheet": [{"cols": 1, "name": "A", "rows": 2}], "name": "tests"}|
