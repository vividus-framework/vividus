Meta:
    @epic vividus-plugin-json

Lifecycle:
Before:
Scope: STORY
Given I initialize story variable `json` with value `
{
    "store": {
        "book": [
            {
                "category": "reference",
                "author": "Nigel Rees",
                "title": "Sayings of the Century",
                "isbn": null,
                "price": 8.95
            },
            {
                "category": "fiction",
                "author": "Evelyn Waugh",
                "title": "Sword of Honour",
                "price": 12.99,
                "hardcover": false
            },
            {
                "category": "fiction",
                "author": "Herman Melville",
                "title": "Moby Dick",
                "isbn": "0-553-21311-3",
                "price": 9,
                "attributes": "{\"used\": false}"
            },
            {
                "category": "fiction",
                "author": "J. R. R. Tolkien",
                "title": "The Lord of the Rings",
                "isbn": "0-395-19395-8",
                "price": 22.99,
                "hardcover": true
            }
        ],
        "bicycle": {
            "color": "red",
            "price": 19.95
        }
    },
    "expensive": 10
}
`

Scenario: Step verification 'When I patch JSON `$sourceJson` using `$jsonPatch` and save result to $scopes variable `$variableName`'
When I patch JSON `${json}` using `[{ "op": "replace", "path": "/store", "value": "unknown" }]` and save result to scenario variable `patchedJson`
Then JSON element from `${patchedJson}` by JSON path `$` is equal to `
{
    "store": "unknown",
    "expensive": 10
}
`

Scenario: Step verification 'When I save JSON element value from `$json` by JSON path `$jsonPath` to $scopes variable `$variableName`'
Meta:
    @requirementId 2114
When I save JSON element value from `${json}` by JSON path `<jsonPath>` to scenario variable `jsonElementValue`
Then `${jsonElementValue}` is equal to `<expected>`
Examples:
|jsonPath                  |expected       |
|$.store.book[0].category  |reference      |
|$.store.book[0].isbn      |null           |
|$.store.book[1].price     |12.99          |
|$.store.book[1].hardcover |false          |
|$.store.book[2].attributes|{"used": false}|
|$.store.book[3].hardcover |true           |
|$.expensive               |10             |

Scenario: Step verification 'Then JSON element value from `$json` by JSON path `$jsonPath` $comparisonRule `$expectedValue`'
Meta:
    @requirementId 2114
Then JSON element value from `${json}` by JSON path `<jsonPath>` <rule> `<expected>`
Examples:
|jsonPath                  |rule           |expected       |
|$.store.book[0].category  |contains       |feren          |
|$.store.book[0].isbn      |is equal to    |#{null}        |
|$.store.book[1].price     |is greater than|12.50          |
|$.store.book[1].hardcover |is equal to    |false          |
|$.store.book[2].attributes|is equal to    |{"used": false}|
|$.store.book[3].hardcover |is equal to    |true           |
|$.expensive               |is equal to    |10             |
|$.expensive               |is not equal to|#{null}        |
|$.store.book[0].category  |is not equal to|#{null}        |
|$.store.book[1].hardcover |is not equal to|#{null}        |
|$.store.book[1]           |is not equal to|#{null}        |
|$.store.book[0].isbn      |is not equal to|12             |

Scenario: Step verification: 'When I convert JSON `$json` to $scopes variable `$variableName`'
When I convert JSON `${json}` to scenario variable `jsonData`
Then `${jsonData.store.book[0].price}` is = `8.95`


Scenario: Step verification 'When I find $comparisonRule `$elementsNumber` JSON elements in `$json` by `$jsonPath` and until variable `$variableName` $comparisonRule `$expectedValue` for each element I do:$stepsToExecute'
When I find > `1` JSON elements in `${json}` by `$.store.book` and until variable `title` matches `M.+` for each element I do:
|step|
|When I save JSON element value from `${json-context}` by JSON path `$.title` to scenario variable `title`|
Then `Moby Dick` is = `${title}`
When I find > `1` JSON elements in `${json}` by `$.store.book` and until variable `title` matches `S.+` for each element I do:
|step|
|When I save JSON element value from `${json-context}` by JSON path `$.title` to scenario variable `title`|
Then `Sayings of the Century` is = `${title}`


Scenario: Verify step "Then JSON element from `$json` by JSON path `$jsonPath` is equal to `$expectedData`$options"
Given I initialize scenario variable `current-date` with value `#{generateDate(P, yyyy-MM-DD)}`
Given I initialize scenario variable `expected-json` using template `/data/json-validation-template.ftl` with parameters:
|currentDate   |
|${current-date}|
Then JSON element from `
{
    "currentDateWithAnyTime": "${current-date}T22:20:35+07:00",
    "fieldToBeIgnored": "valueToBeIgnored"
}
` by JSON path `$` is equal to `${expected-json}` ignoring extra fields


Scenario: Verify step 'When I find $comparisonRule `$elementsNumber` JSON elements from `$json` by `$jsonPath` and for each element do$stepsToExecute' with zero elements
When I find <= `1` JSON elements from `{}` by `$.name` and for each element do
|step                    |
|Then `0` is equal to `1`|


Scenario: Verify JSON validator can successfully compare int vs float numbers
Then JSON element from `{"number":0.0}` by JSON path `$.number` is equal to `0`


Scenario: Validate long float numbers are not trimmed
Meta:
    @issueId 3471
Then JSON element value from `{
    "long-long-float":485690.3866338789319252000000135498000000
}` by JSON path `$.long-long-float` is equal to `485690.3866338789319252000000135498000000`

Scenario: Validate JSON against JSON schema
Then JSON `
{
   "productId": 1,
   "productName": "A desk lamp",
   "price": 12.50,
   "tags": [ "lamp", "desk" ]
}
` is valid against schema `
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://example.com/product.schema.json",
  "title": "Product",
  "description": "A product from catalog",
  "type": "object",
  "properties": {
    "productId": {
      "description": "The unique identifier for a product",
      "type": "integer"
    },
    "productName": {
      "type": "string"
    },
    "price": {
      "type": "number",
      "exclusiveMinimum": 0
    },
    "tags": {
      "description": "Tags for the product",
      "type": "array",
      "prefixItems": [{
        "type": "string",
        "enum": ["lamp", "desk"]
      }],
      "minItems": 1,
      "uniqueItems": true
    }
  },
  "required": [ "productId", "productName", "price", "tags" ]
}
`

Scenario: Validate anyOf json-unit matcher (store expected value in variable)
Given I initialize SCENARIO variable `expected-array` with value `[{\"category\":\"poetry\",\"author\":\"Yakub Kolas\",\"title\":\"Novaya zyamlya\",\"isbn\":null,\"price\":8.95},{\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99,\"hardcover\":false}]`
Then JSON element from `${json}` by JSON path `$.store` is equal to `
{
  "book":"#{json-unit.matches:anyOf}${expected-array}",
  "bicycle":{
    "color":"red",
    "price":19.95
  }
}
`

Scenario: Validate anyOf json-unit matcher (with escapeJson expression)
Then JSON element from `${json}` by JSON path `$.store` is equal to `
{
  "book":"#{json-unit.matches:anyOf}#{escapeJson(
  [
    {
        "category":"poetry",
        "author":"Yakub Kolas",
        "title":"Novaya zyamlya",
        "isbn":null,
        "price":8.95
    },
    {
        "category": "fiction",
        "author": "Evelyn Waugh",
        "title": "Sword of Honour",
        "price": 12.99,
        "hardcover": false
    }
  ])}",
  "bicycle":{
    "color":"red",
    "price":19.95
  }
}
`
