Meta:
    @epic vividus-plugin-cosmos-db

Scenario: Verify Cosmos DB CRUD operations
When I read item with `1` id and `personal` partition from CosmosDB container `test` and save result to scenario variable `result`
Then JSON element from `${result}` by JSON path `$` is equal to `
{
    "id": "1",
    "category": "personal",
    "name": "groceries",
    "description": "Pick up apples and strawberries.",
    "isComplete": false
}`IGNORING_EXTRA_FIELDS
When I insert `
{
    "id": "3",
    "category": "personal",
    "name": "work",
    "description": "Vacation",
    "isComplete": false
}` into CosmosDB container `test`
When I upsert `
{
    "id": "3",
    "category": "personal",
    "name": "work",
    "description": "Work till sundown",
    "isComplete": false
}` into CosmosDB container `test`
When I execute `SELECT * FROM Items` query against CosmosDB container `test` and save result to scenario variable `result`
Then JSON element from `${result}` by JSON path `$` is equal to `
[
    {
        "id": "1",
        "category": "personal",
        "name": "groceries",
        "description": "Pick up apples and strawberries.",
        "isComplete": false
    },
    {
        "id": "2",
        "category": "private",
        "name": "stores",
        "description": "Pick up apples and strawberries.",
        "isComplete": false
    },
    {
        "id": "3",
        "category": "personal",
        "name": "work",
        "description": "Work till sundown",
        "isComplete": false
    }
]`IGNORING_EXTRA_FIELDS
When I delete `
{
    "id": "3",
    "category": "personal"
}` from CosmosDB container `test`
When I execute `SELECT * FROM Items` query against CosmosDB container `test` and save result to scenario variable `result`
Then JSON element from `${result}` by JSON path `$` is equal to `
[
    {
        "id": "1",
        "category": "personal"
    },
    {
        "id": "2",
        "category": "private"
    }
]`IGNORING_EXTRA_FIELDS
