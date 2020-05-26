Description: Integration tests for various Vividus ExamplesTable transformers

Meta:
    @epic vividus-bdd-engine
    @feature table-transformers

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


Scenario: Verify FILTERING transformer with byMax parameters
Then `<column1>` is equal to `A`
Then `<column2>` is equal to `B`
Examples:
{transformer=FILTERING, byMaxColumns=2, byMaxRows=1}
|column1|column2|column3|
|A      |B      |C      |
|D      |E      |F      |


Scenario: Verify FILTERING transformer with byColumnNames parameter and external table
Then `<column3>` is equal to `C`
Examples:
{transformer=FILTERING, byColumnNames=column3}
/data/for-filtering-transformer.table


Scenario: Verify RESOLVING_EXPRESSIONS_EAGERLY transformer
When I initialize story variable `table` with values:
/data/without-eagerly-transformer.table
Then `${table[0].name}` is not equal to `${table[0].name}`
When I initialize story variable `table_resolved` with values:
/data/with-eagerly-transformer.table
Then `${table_resolved[0].name}` is equal to `${table_resolved[0].name}`


Scenario: Verify RESOLVING_SELF_REFERENCES_EAGERLY transformer chained
Then `<column1>` is equal to `A`
Then `<column2>` is equal to `A`
Then `<column3>` is equal to `A`
Examples:
{transformer=RESOLVING_SELF_REFERENCES_EAGERLY}
|column1|column2  |column3  |
|A      |<column3>|<column1>|


Scenario: Verify RESOLVING_SELF_REFERENCES_EAGERLY transformer inlined
Then `<column2>` is equal to `aAa`
Examples:
{transformer=RESOLVING_SELF_REFERENCES_EAGERLY}
|column1|column2    |
|A      |a<column1>a|


Scenario: Verify RESOLVING_SELF_REFERENCES_EAGERLY transformer default behavior
Then `<column2>` is equal to `<column>`
Then `<column3>` is equal to ``
Examples:
{transformer=RESOLVING_SELF_REFERENCES_EAGERLY}
|column1|column2 |column3|
|A      |<column>|


Scenario: Verify REPEATING transformer
Then `<column1>` is equal to `A B A B A B`
Then `<column2>` is equal to `C D C D C D`
Examples:
{transformer=REPEATING, times=3}
{transformer=JOINING, joinMode=rows}
|column1|column2|
|A      |C      |
|B      |D      |
