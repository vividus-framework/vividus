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

Scenario: Verify FROM_DB transformer
Meta:
    @feature table-transformers
Then `<name>` is equal to `lipstick`
Then `<description>` is equal to `Color with the power to transform your lips`
Examples:
{transformer=FROM_DB, dbKey=testh2db, sqlQuery=SELECT name\,description FROM cosmetics}

Scenario: Verify execute sql steps without 'INIT' property and with 'DB_CLOSE_DELAY=-1' property in database connection url
Meta:
    @issueId 2273
When I execute SQL query `
CREATE SCHEMA if not exists test;
SET SCHEMA test;
CREATE TABLE if not exists test.launch_rockets (name VARCHAR(50) NOT NULL, payload_cost VARCHAR(100) NOT NULL, country VARCHAR(100) NOT NULL);
INSERT INTO launch_rockets (name, payload_cost, country) VALUES ('Falcon 9', '$2,720', 'USA');
INSERT INTO launch_rockets (name, payload_cost, country) VALUES ('Proton', '$4,320', 'RUSSIA');
` against `testh2db-modifiable`
When I execute SQL query `UPDATE test.launch_rockets SET country='USSR' WHERE name='Proton';` against `testh2db-modifiable`
When I execute SQL query `UPDATE test.launch_rockets SET payload_cost='unknown' WHERE country='USSR';` against `testh2db-modifiable`
When I execute SQL query `SELECT * FROM test.launch_rockets WHERE name='Proton'` against `testh2db-modifiable` and save result to scenario variable `test-data`
Then `${test-data}` is equal to `[{name=Proton, payload_cost=unknown, country=USSR}]`
