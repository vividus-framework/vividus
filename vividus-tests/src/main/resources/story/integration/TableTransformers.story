Description: Integration tests for various Vividus ExamplesTable transformers

Meta:
    @group table-transformers

Scenario: Verify FROM_CSV transformer
Then `<country>` is equal to `Belarus`
Then `<capital>` is equal to `Minsk`
Then `<data>` is equal to `{"sheet": [{"cols": 1, "name": "A", "rows": 2}], "name": "tests"}`
Examples:
{transformer=FROM_CSV, csvPath=/data/csv.csv}

Scenario: Verify JOINING transformer in default columns mode
Then `<joinedColumn>` is equal to `A B`
Examples:
{transformer=JOINING, joinMode=columns, joinedColumn=joinedColumn}
|column1|column2|
|A      |B      |

Scenario: Verify JOINING transformer in configured columns mode
Then `<joinedColumn>` is equal to `B D`
Examples:
{transformer=JOINING, joinMode=columns, joinedColumn=joinedColumn, columnsToJoin=column2;column4}
|column1|column2|column3|column4|
|A      |B      |C      |D      |

Scenario: Verify JOINING transformer in default rows mode
Then `<column1>` is equal to `A B`
Then `<column2>` is equal to `C D`
Examples:
{transformer=JOINING, joinMode=rows}
|column1|column2|
|A      |C      |
|B      |D      |

Scenario: Declare story variable to be used in the next scenario
When I initialize the story variable `for-merging-transformer` with value `X`

Scenario: Verify MERGING transformer with table body
Then `<column1>` is equal to `A`
Then `<column2>` is equal to `X`
Then `<column3>` is equal to `B`
Examples:
{transformer=MERGING, mergeMode=columns, tables=/data/for-merging-transformer.table}
|column1|column2                   |
|A      |${for-merging-transformer}|


Scenario: Create a counter
When I initialize the STORY variable `updatedNumber` with value `0`


Scenario: Update the counter through ITERATING with a global variable
When I initialize the STORY variable `updatedNumber` with value `#{eval(${updatedNumber} + 1)}`
Examples:
{transformer=ITERATING, limit=$\{iterationLimit\}}


Scenario: Verify the number of actual iterations
Then `${updatedNumber}` is = `${iterationLimit}`
