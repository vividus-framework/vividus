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

Scenario: Step verification 'When I save JSON element value from context by JSON path `$jsonPath` to $scopes variable `$variableName`'
Meta:
    @requirementId 2114
When I find = `1` JSON elements from `${json}` by `$.store` and for each element do
|step                                                                                                         |
|When I save JSON element value from context by JSON path `<jsonPath>` to scenario variable `jsonElementValue`|
|Then `${jsonElementValue}` is equal to `<expected>`                                                          |
Examples:
|jsonPath            |expected       |
|$.book[0].category  |reference      |
|$.book[0].isbn      |null           |
|$.book[1].price     |12.99          |
|$.book[1].hardcover |false          |
|$.book[2].price     |9              |
|$.book[2].attributes|{"used": false}|
|$.book[3].hardcover |true           |

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

Scenario: Step verification 'Then JSON element value from context by JSON path `$jsonPath` $comparisonRule `$expectedValue`'
Meta:
    @requirementId 2114
When I find = `1` JSON elements from `${json}` by `$.store` and for each element do
|step                                                                                   |
|Then JSON element value from context by JSON path `<jsonPath>` <rule> `<expected>`|
Examples:
|jsonPath            |rule           |expected       |
|$.book[0].category  |contains       |feren          |
!-- |$.book[0].isbn      |is equal to    |#{null}        | <- TODO: think of ability to pass #{null} to sub-steps
|$.book[1].price     |is greater than|12.50          |
|$.book[1].hardcover |is equal to    |false          |
|$.book[2].price     |is equal to    |9              |
|$.book[2].attributes|is equal to    |{"used": false}|
|$.book[3].hardcover |is equal to    |true           |

Scenario: Step verification: 'When I convert JSON `$json` to $scopes variable `$variableName`' and 'When I convert JSON from context to $scopes variable `$variableName`'
When I convert JSON `${json}` to scenario variable `jsonData`
Then `${jsonData.store.book[0].price}` is = `8.95`
When I find = `1` JSON elements from `${json}` by `$..book[?(@.title == 'Sword of Honour')]` and for each element do
|step|
|When I convert JSON from context to scenario variable `jsonData`|
Then `${jsonData.title}` is = `Sword of Honour`


Scenario: Step verification 'When I find $comparisonRule `$elementsNumber` JSON elements in `$json` by `$jsonPath` and until variable `$variableName` $comparisonRule `$expectedValue` for each element I do:$stepsToExecute'
When I find > `1` JSON elements in `${json}` by `$.store.book` and until variable `title` matches `M.+` for each element I do:
|step|
|When I save JSON element value from context by JSON path `$.title` to scenario variable `title`|
Then `Moby Dick` is = `${title}`
When I find > `1` JSON elements in `${json}` by `$.store.book` and until variable `title` matches `S.+` for each element I do:
|step|
|When I save JSON element value from context by JSON path `$.title` to scenario variable `title`|
Then `Sayings of the Century` is = `${title}`


Scenario: Step verification 'When I find $comparisonRule `$elementsNumber` JSON elements in context by `$jsonPath` and until variable `$variableName` $comparisonRule `$expectedValue` for each element I do:$stepsToExecute'
When I execute HTTP GET request for resource with URL `http://jsonpath.herokuapp.com/json/goessner.json`
When I find > `1` JSON elements in context by `$.store.book` and until variable `title` matches `M.+` for each element I do:
|step                                                                                           |
|When I save JSON element value from context by JSON path `$.title` to scenario variable `title`|
Then `Moby Dick` is = `${title}`
When I find > `1` JSON elements in context by `$.store.book` and until variable `title` matches `S.+` for each element I do:
|step                                                                                           |
|When I save JSON element value from context by JSON path `$.title` to scenario variable `title`|
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
