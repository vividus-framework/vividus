Description: Integration tests for DatabaseSteps class

Meta:
    @epic vividus-plugin-db
    @requirementId 435

Scenario: Verify step: 'When I merge `$left` and `$right` and save result to $scopes variable `$variableName`'
When I execute SQL query `
SELECT * FROM vividus
WHERE id = 1;
` against `vividus` and save result to scenario variable `first`
When I execute SQL query `
SELECT * FROM vividus
WHERE id = 2;
` against `vividus` and save result to scenario variable `second`
When I execute SQL query `
SELECT * FROM vividus
WHERE id = 3;
` against `vividus` and save result to scenario variable `third`
When I merge `${first}` and `${second}` and save result to scenario variable `result`
When I merge `${result}` and `${third}` and save result to scenario variable `result`
Then `${result}` matching rows using `id` from `vividus` is equal to data from:
|id|name                     |
|1 |Valery                   |
|2 |Vlad                     |
|3 |Ivan                     |
