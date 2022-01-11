Description: Integration tests for various Vividus ExamplesTable transformers

Meta:
    @epic vividus-engine
    @feature table-transformers

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


Scenario: Get random row with byRandomRows
Meta:
    @requirementId 1895
Then `<column1>` matches `(A|D|G|J|M)`
Then `<column2>` matches `(B|E|H|K|N)`
Examples:
{transformer=FILTERING, byRandomRows=1, byMaxColumns=2}
|column1|column2|column3|
|A      |B      |C      |
|D      |E      |F      |
|G      |H      |I      |
|J      |K      |L      |
|M      |N      |O      |


Scenario: Verify FILTERING transformer with byColumnNames parameter and external table
Then `<column3>` is equal to `C`
Examples:
{transformer=FILTERING, byColumnNames=column3}
/data/for-filtering-transformer.table


Scenario: Verify RESOLVING_EXPRESSIONS_EAGERLY transformer
When I initialize story variable `table` with values:
/data/with-replacing-transformer.table
Then `${table[0].name}` is equal to `#{invalid(Address.firstName)}`
When I initialize story variable `table-resolved` with values:
{transformer=RESOLVING_EXPRESSIONS_EAGERLY}
/data/with-replacing-transformer.table
Then `${table-resolved[0].name}` is not equal to `#{invalid(Address.firstName)}`


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


Scenario: Verify FILTERING transformer by regex
Then `<shouldMatchRegex>` matches `^([01]+|\W+|[A-Z]+)$`
Examples:
{transformer=FILTERING, column.shouldMatchRegex=^([01]+|\W+|[A-Z]+)$}
|shouldMatchRegex|
|10100101011     |
|+!@#<>          |
|@(-_-)@         |
|ABCDEFGXYZU     |
|jhbdahjabw      |
|738162378613896 |

Scenario: Verify FROM_EXCEL transformer
Meta:
    @issueId 647
Then `<joined>` is equal to `line 1 line 2 line 3`
Examples:
{transformer=FROM_EXCEL, path=/data/excel.xlsx, sheet=Sheet1, addresses=A1, column=joined, \{lineBreakReplacement|VERBATIM\}= }

Scenario: Verify ExamplesTable property value with space
Meta:
    @issueId 767
Then `<joined>` is equal to `line 1 line 2 line 3`
Examples:
{transformer=FROM_EXCEL, path=/data/excel with spaces in name.xlsx, sheet=Sheet1, addresses=A1, column=joined, \{lineBreakReplacement|VERBATIM\}= }

Scenario: Verify nested transformers
When I initialize the story variable `table-row-number` with value `#{eval(${table-row-number:0} + 1)}`
Then `<repeated>` is equal to `/to/be/repeated/for/each/row/`
Then `<repeated2>` is equal to `one-more-repeated-value`
Then `<repeated3>` is equal to `we need more repetitions`
Then `<number>` is equal to `${table-row-number}`
Then `<name>` is equal to `Name ${table-row-number}`
Examples:
{transformer=MERGING, mergeMode=columns, fillerValue=/to/be/repeated/for/each/row/, tables=|repeated|;
    \{transformer=MERGING\, mergeMode=columns\, fillerValue=one-more-repeated-value\, tables=|repeated2|\;
        \\{transformer=MERGING\\, mergeMode=columns\\, fillerValue=we need more repetitions\\, tables=|repeated3|\\;
            \\\{transformer=FROM_EXCEL\\\, path=/data/complex-data.xlsx\\\, sheet=data\\\, range=A4:A20\\\, increment=8\\\, column=number\\\}\\;
            \\\{transformer=FROM_EXCEL\\\, path=/data/complex-data.xlsx\\\, sheet=data\\\, range=B4:B20\\\, increment=8\\\, column=name\\\}
        \\}
    \}
}

Scenario: Verify variable from nested transformers scenario
Then `${table-row-number}` is equal to `3`

Scenario: Verify processing of tables with table separators in cells by table transformers
Meta:
    @issueId 951
Then `<col1>` is equal to `A | B`
Then `<col2>` is equal to `C ! D`
Examples:
{transformer=MERGING, mergeMode=columns, tables=
    \{transformer=FROM_EXCEL\, path=/data/complex-data.xlsx\, sheet=with separators\, range=A1:A2\, column=col1\};
    \{transformer=FROM_EXCEL\, path=/data/complex-data.xlsx\, sheet=with separators\, range=B1:B2\, column=col2\}
}
{transformer=FILTERING, column.col1=.+}

Scenario: Verify DISTINCTING transformer
Meta:
    @requirementId 992
Then `<column1>` is equal to `a a b b`
Then `<column3>` is equal to `a b a b`
Examples:
{transformer=DISTINCTING, byColumnNames=column1;column3}
{transformer=JOINING, joinMode=rows}
|column1|column2|column3|
|a      |x      |a      |
|a      |y      |a      |
|a      |x      |b      |
|a      |y      |b      |
|b      |x      |a      |
|b      |y      |a      |
|b      |x      |b      |
|b      |y      |b      |

Scenario: Verify possibility to use the range of ALL cells
Meta:
    @requirementId 919
Then `<date1>` is equal to `line1 line4`
Then `<date2>` is equal to `line2 line5`
Then `<date3>` is equal to `line3 line6`
Examples:
{transformer=FROM_EXCEL, path=/data/complex-data.xlsx, sheet=range-all-cells, range=B2:D4}
{transformer=JOINING, joinMode=rows}


Scenario: Verify CARTESIAN_PRODUCT transformer
When I initialize scenario variable `cartesianProductTable` with values:
{transformer=CARTESIAN_PRODUCT, tables=/data/multirow.table}
|col1 |col2 |col3 |
|row11|row12|row13|
|row21|row22|row23|
|row31|row32|row33|
Then `${cartesianProductTable}` is equal to table:
|id|name   |planet  |col1 |col2 |col3 |
|1 |Junit  |Jupiter |row11|row12|row13|
|1 |Junit  |Jupiter |row21|row22|row23|
|1 |Junit  |Jupiter |row31|row32|row33|
|2 |Freddie|Mercury |row11|row12|row13|
|2 |Freddie|Mercury |row21|row22|row23|
|2 |Freddie|Mercury |row31|row32|row33|
|3 |AWS    |Neptune |row11|row12|row13|
|3 |AWS    |Neptune |row21|row22|row23|
|3 |AWS    |Neptune |row31|row32|row33|
