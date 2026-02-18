Meta:
    @epic vividus-plugin-excel

Lifecycle:
Before:
Scope: STORY
When I execute HTTP GET request for resource with URL `https://github.com/vividus-framework/vividus/blob/master/vividus-plugin-excel/src/test/resources/TestTemplate.xlsx?raw=true`
Then response code is equal to `200`

Scenario: Validate step 'Then `$excelDocument` contains excel sheet with name `$name` at index `$index`'
Meta:
    @requirementId 6016
Then `${response-as-bytes}` contains excel sheet with name `Mapping` at index `0`

Scenario: Validate step 'Then `$excelDocument` contains excel sheet with index `$index` and records:$records'
Then `${response-as-bytes}` contains excel sheet with index `0` and records:
{valueSeparator=!}
|cellsRange|valueRegex             |
!A4:B5     !(Product|Price)\d+\s*  !
!B3        !Price                  !
!C1:C5     !                       !

Scenario: Validate variable initialization with binary data and step 'Then `$excelDocument` contains excel sheet with name `$name` and records:$records'
Given I initialize scenario variable `excel` with value `${response-as-bytes}`
Then `${excel}` contains excel sheet with name `Mapping` and records:
{valueSeparator=!}
|cellsRange|valueRegex             |
!A4:B5     !(Product|Price)\d+\s*  !
!B3        !Price                  !
!C1:C5     !                       !

Scenario: Validate cell formatting is preserved
Given I initialize scenario variable `excel` with value `${response-as-bytes}`
Then `${excel}` contains excel sheet with name `AsString` and records:
|cellsRange |valueRegex |
|A1         |TRUE       |
|B3         |3          |

Scenario: Validate step 'When I create temporary excel file with content:$content and put path to $scopes variable `$variableName`' and transformer FROM_EXCEL
Meta:
    @requirementId 1028, 2953
    @feature table-transformers
When I create temporary excel file with content:
|key1  |key2  |
|value1|value2|
and put path to scenario variable `path`
Given I initialize scenario variable `fileExists` with value `#{evalGroovy(return path.toFile().exists())}`
Then `${fileExists}` is = `true`
When I initialize scenario variable `excel-data` with values:
|key1  |key2  |
|value1|value2|
Then `${excel-data}` is equal to table:
{transformer=FROM_EXCEL, path=$\{path\}, sheet=Sheet0, range=A1:B2}

Scenario: Validate step 'When I create temporary excel file with content:$content and put path to $scopes variable `$variableName`' with different cells type and formatting
Meta:
    @requirementId 4553
When I create temporary excel file with content:
|numeric                                       |formula                              |date                                                                              |boolean                         |#{withColumnCellsType(date, multi-type col)} |
|#{withCellType(Numeric, 100500 format #.000)} |#{withCellType(formula, left(A2,3))} |#{withCellType(DATE,  03/31/2024)}                                                |#{withCellType(BOOLEAN, true)}  |31-Mar-2024 input dd-MMM-yyyy                |
|#{withCellType(Numeric, 99500)}               |#{withCellType(formula,A2+A3)}       |#{withCellType(DATE, 31-Mar-2024 input dd-MMM-yyyy)}                              |#{withCellType(BOOLEAN, false)} |#{withCellType(Numeric, 100500)}             |
|                                              |                                     |#{withCellType(DATE, 03/31/2024 format m.d.yyyy h:mm)}                            |                                |#{withCellType(String, 105)}                 |
|                                              |                                     |#{withCellType(DATE, 2024/03/31 13:04 input yyyy/MM/dd HH:mm format m.d.yy h:mm)} |                                |#{withCellType(String, Empty)}               |
|                                              |                                     |#{withCellType(DATE, 2024,03,31 format m.d.yy h:mm input yyyy,MM,dd)}             |                                |#{withCellType(String, not allowed)}         |
and put path to scenario variable `path`
When I initialize scenario variable `excel-data` with values:
|numeric    |formula |date            |boolean |multi-type col |
|100500.000 |100     |3/31/24         |TRUE    |3/31/24        |
|99500.0    |200000  |3/31/24         |FALSE   |100500.0       |
|           |        |3.31.2024 0:00  |        |105            |
|           |        |3.31.24 13:04   |        |Empty          |
|           |        |3.31.24 0:00    |        |not allowed    |
Then `${excel-data}` is equal to table:
{transformer=FROM_EXCEL, path=$\{path\}, sheet=Sheet0, range=A1:E6}

Scenario: Validate steps 'When I create temporary excel file containing sheet with name `$sheetName` and content:$content and put its path to $scopes variable `$variableName`' and 'When I add sheet with name `$sheetName` and content $content to excel file at path `$path`'
Meta:
    @requirementId 2953
When I create temporary excel file containing sheet with name `my-sheet-name` and content:
|key1  |key2  |
|value1|value2|
and put its path to scenario variable `path`
When I initialize scenario variable `excel-data` with values:
|key1  |key2  |
|value1|value2|

When I add sheet with name `added-sheet` and content:
|animal             |weight |
|Komodo Dragon      |91 kg  |
|Whale Shark        |21 ton |
|Saltwater Crocodile|1075 kg|
to excel file at path `${path}`
When I initialize scenario variable `added-sheet-excel-data` with values:
|animal             |weight |
|Komodo Dragon      |91 kg  |
|Whale Shark        |21 ton |
|Saltwater Crocodile|1075 kg|

Then `${excel-data}` is equal to table:
{transformer=FROM_EXCEL, path=$\{path\}, sheet=my-sheet-name, range=A1:B2}
Then `${added-sheet-excel-data}` is equal to table:
{transformer=FROM_EXCEL, path=$\{path\}, sheet=added-sheet, range=A1:B4}

Scenario: Validate FROM_EXCEL transformer
Meta:
    @issueId 647
Then `<joined>` is equal to `line 1 line 2 line 3`
Examples:
{transformer=FROM_EXCEL, path=/data/excel.xlsx, sheet=Sheet1, addresses=A1, column=joined, \{lineBreakReplacement|VERBATIM\}= }

Scenario: Check loading excel table with different data types using FROM_EXCEL transformer
Meta:
    @issueId 2908
When I initialize scenario variable `expectedTable` with values:
|StringValue|NumericValue|BooleanValue|FormulaValue|FormulaErrorValue|
|City       |17          |FALSE       |289         |#VALUE!          |
|Country    |19          |TRUE        |361         |null             |
Then `${expectedTable}` is equal to table:
{transformer=FROM_EXCEL, path=/data/excel.xlsx, sheet=DifferentTypes, range=A1:E3}

Scenario: Check FROM_EXCEL transformer with multiple ranges (separate ranges for header and data)
Meta:
    @issueId 5084
When I initialize scenario variable `expectedTable` with values:
|StringValue |NumericValue |BooleanValue |FormulaValue |
|Timezone    |21           |FALSE        |441          |
|City        |17           |FALSE        |289          |
|Country     |19           |TRUE         |361          |
Then `${expectedTable}` is equal to table:
{transformer=FROM_EXCEL, path=/data/excel.xlsx, sheet=DifferentTypes, range=A1:D1;A4:D4;A2:D3;}

Scenario: Check FROM_EXCEL transformer with multiple ranges (separate ranges for header with data and additional data)
Meta:
    @issueId 5084
When I initialize scenario variable `expectedTable` with values:
|NumericValue |BooleanValue |FormulaValue |
|17           |FALSE        |289          |
|19           |TRUE         |361          |
Then `${expectedTable}` is equal to table:
{transformer=FROM_EXCEL, path=/data/excel.xlsx, sheet=DifferentTypes, range=B1:D2;B3:D3}

Scenario: Validate step 'Then `$excelDocument` contains exactly `$expectedRowCount` rows in sheet with index `$sheetIndex`'
Meta:
    @requirementId 6017
Then `${response-as-bytes}` contains exactly `5` rows in sheet with index `0`
