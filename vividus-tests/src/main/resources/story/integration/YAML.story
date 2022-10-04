Meta:
    @epic vividus-plugin-yaml

Lifecycle:
Before:
Scope: STORY
Given I initialize story variable `yaml` with value `
---
store:
  book:
  - category: reference
    author: Nigel Rees
    title: Sayings of the Century
    isbn: ~
    price: 8.95
  - category: fiction
    author: Evelyn Waugh
    title: Sword of Honour
    price: 12.99
    hardcover: false
  - category: fiction
    author: Herman Melville
    title: Moby Dick
    isbn: 0-553-21311-3
    price: 9
    attributes: '{"used": false}'
  - category: fiction
    author: J. R. R. Tolkien
    title: The Lord of the Rings
    isbn: 0-395-19395-8
    price: 22.99
    hardcover: true
  bicycle:
    color: red
    price: 19.95
expensive: 10
`

Scenario: Step verification 'When I save YAML element value from `$yaml` by YAML path `$yamlPath` to $scopes variable `$variableName`'
When I save YAML element value from `${yaml}` by YAML path `<yamlPath>` to scenario variable `yamlElementValue`
Then `${yamlElementValue}` is equal to `<expected>`
Examples:
|yamlPath                |expected       |
|store.book[0].category  |reference      |
|store.book[1].price     |12.99          |
|store.book[1].hardcover |false          |
|store.book[2].attributes|{"used": false}|
|store.book[3].hardcover |true           |
|expensive               |10             |
