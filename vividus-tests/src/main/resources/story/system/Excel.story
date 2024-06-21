Meta:
    @epic vividus-plugin-excel

Lifecycle:
Before:
Scope: STORY
When I execute HTTP GET request for resource with URL `https://github.com/vividus-framework/vividus/blob/master/vividus-plugin-excel/src/test/resources/TestTemplate.xlsx?raw=true`
Then response code is equal to `200`

Scenario: Validate cell formatting is not preserved
Given I initialize scenario variable `excel` with value `${response-as-bytes}`
Then `${excel}` contains excel sheet with name `AsString` and records:
|cellsRange |valueRegex |
|A1         |true       |
|B3         |3\.0       |

Scenario: Check loading excel table with different data types using FROM_EXCEL transformer - without preserving cell formatting
Meta:
    @issueId 2908
When I initialize scenario variable `expectedTable` with values:
|StringValue|NumericValue|BooleanValue|FormulaValue|FormulaErrorValue|
|City       |17.0        |false       |289.0       |                 |
Then `${expectedTable}` is equal to table:
{transformer=FROM_EXCEL, path=/data/excel.xlsx, sheet=DifferentTypes, range=A1:E2}

Scenario: Check FROM_EXCEL transformer with multiple ranges (separate ranges for header and data) - without preserving cell formatting
Meta:
    @issueId 5084
When I initialize scenario variable `expectedTable` with values:
|StringValue |NumericValue |BooleanValue |FormulaValue |
|Timezone    |21.0         |false        |441.0        |
|City        |17.0         |false        |289.0        |
|Country     |19.0         |true         |361.0        |
Then `${expectedTable}` is equal to table:
{transformer=FROM_EXCEL, path=/data/excel.xlsx, sheet=DifferentTypes, range=A1:D1;A4:D4;A2:D3;}

Scenario: Check FROM_EXCEL transformer with multiple ranges (separate ranges for header with data and additional data) - without preserving cell formatting
Meta:
    @issueId 5084
When I initialize scenario variable `expectedTable` with values:
|NumericValue |BooleanValue |FormulaValue |
|17.0         |false        |289.0        |
|19.0         |true         |361.0        |
Then `${expectedTable}` is equal to table:
{transformer=FROM_EXCEL, path=/data/excel.xlsx, sheet=DifferentTypes, range=B1:D2;B3:D3}
