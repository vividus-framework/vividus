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
Given I initialize story variable `for-merging-transformer` with value `X`

Scenario: Verify MERGING transformer with table body
Then `<column1>` is equal to `A`
Then `<column2>` is equal to `X`
Then `<column3>` is equal to `B`
Examples:
{transformer=MERGING, mergeMode=columns, tables=/data/for-merging-transformer.table}
|column1|column2                   |
|A      |${for-merging-transformer}|


Scenario: Create a counter
Given I initialize story variable `counter` with value `0`


Scenario: Update the counter through ITERATING with a global variable
Given I initialize story variable `counter` with value `#{eval(${counter} + 1)}`
Examples:
{transformer=ITERATING, startInclusive=1, endInclusive=$\{iterationLimit\}}


Scenario: Verify the number of actual iterations
Then `${counter}` is = `${iterationLimit}`


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
Then `${table[0].name}` is equal to `#{invalid(Name.firstName)}`
When I initialize story variable `table-resolved` with values:
{transformer=RESOLVING_EXPRESSIONS_EAGERLY}
/data/with-replacing-transformer.table
Then `${table-resolved[0].name}` is not equal to `#{invalid(Name.firstName)}`


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

Scenario: Check loading excel table with different data types using FROM_EXCEL transformer
Meta:
    @issueId 2908
When I initialize scenario variable `expectedTable` with values:
|StringValue|NumericValue|BooleanValue|FormulaValue|FormulaErrorValue|
|City       |17.0        |false       |289.0       |                 |
Then `${expectedTable}` is equal to table:
{transformer=FROM_EXCEL, path=/data/excel.xlsx, sheet=DifferentTypes, range=A1:E2}

Scenario: Verify ExamplesTable property value with space
Meta:
    @issueId 767
Then `<joined>` is equal to `line 1 line 2 line 3`
Examples:
{transformer=FROM_EXCEL, path=/data/excel with spaces in name.xlsx, sheet=Sheet1, addresses=A1, column=joined, \{lineBreakReplacement|VERBATIM\}= }

Scenario: Verify nested transformers
Given I initialize story variable `table-row-number` with value `#{eval(${table-row-number:0} + 1)}`
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

Scenario: Verify CARTESIAN_PRODUCT transformer with empty table (should not be executed)
Then `should` is equal to `be skipped`
Examples:
{transformer=CARTESIAN_PRODUCT, tables=/data/multirow.table}
|col1 |

Scenario: Verify sequence of transformers
Meta:
    @issueId 1812
When I initialize scenario variable `resultSequenceOfTransformers` with values:
{transformer=FILTERING, byColumnNames=column2}
{transformer=CARTESIAN_PRODUCT, tables=/data/multirow.table}
/data/for-filtering-transformer.table
Then `${resultSequenceOfTransformers}` is equal to table:
|id|name   |planet  |column2|
|1 |Junit  |Jupiter |B      |
|2 |Freddie|Mercury |B      |
|3 |AWS    |Neptune |B      |


Scenario: Verify FROM_HEADLESS_CRAWLING transformer
Meta:
    @issueId 2451
Then `<relativeUrl>` matches `.*links.*`
Examples:
{transformer=FROM_HEADLESS_CRAWLING, column=relativeUrl}

Scenario: Verify FROM_SITEMAP transformer
When I initialize scenario variable `sitemapTransformerTable` with values:
{transformer=FROM_SITEMAP, siteMapRelativeUrl=/sitemap.xml, column=sitemapUrl}
Then `${sitemapTransformerTable}` is equal to table:
|sitemapUrl       |
|/checkboxes.html |
|/index.html      |

Scenario: Verify FROM_SITEMAP transformer ignoring sitemap parsing errors
When I initialize scenario variable `sitemapTransformerTable` with values:
{transformer=FROM_SITEMAP, siteMapRelativeUrl=/index.html, column=sitemapUrl, ignoreErrors=true}
Then `${sitemapTransformerTable}` is equal to table:
|sitemapUrl |

Scenario: Verify INDEXING transformer ASCENDING order
Then `<index>` is = `<expected>`
Examples:
{transformer=INDEXING, order=ASCENDING}
|expected|
|0       |
|1       |
|2       |

Scenario: Verify INDEXING transformer DESCENDING order
Then `<index>` is = `<expected>`
Examples:
{transformer=INDEXING, order=DESCENDING}
|expected|
|2       |
|1       |
|0       |

Scenario: Verify INNER_JOIN transformer with empty table (should not be executed)
Meta:
    @requirementId 1810
Then `should` is equal to `be skipped`
Examples:
{transformer=INNER_JOIN, leftTableJoinColumn=joinID, rightTableJoinColumn=joinID1, tables=/data/for-inner-join-transformer.table}
|joinID1 |

Scenario: Verify INNER_JOIN transformer with table body
Meta:
    @requirementId 1810
When I initialize scenario variable `innerJoinTable` with values:
{transformer=INNER_JOIN, leftTableJoinColumn=joinID, rightTableJoinColumn=joinID, tables=/data/for-inner-join-transformer.table}
|joinID|column4|column5|
|5     |row45  |row51  |
|3     |row43  |row53  |
|1     |row41  |row51  |
|3     |row433 |row533 |
Then `${innerJoinTable}` is equal to table:
|column1|joinID|column5|column4|column3|column2|
|row11  |1     |row51  |row41  |row31  |row21  |
|row13  |3     |row53  |row43  |row33  |row23  |
|row13  |3     |row533 |row433 |row33  |row23  |
|row133 |3     |row53  |row43  |row333 |row233 |
|row133 |3     |row533 |row433 |row333 |row233 |

Scenario: Verify SORTING transformer with default order
When I initialize scenario variable `sortingTable` with values:
{transformer=SORTING, byColumns=key1|key2}
|key1|key2|key3|
|bb  |d   |1   |
|ba  |c   |2   |
|bb  |b   |3   |
|aa  |a   |4   |
Then `${sortingTable}` is equal to table:
|key1|key2|key3|
|aa  |a   |4   |
|ba  |c   |2   |
|bb  |b   |3   |
|bb  |d   |1   |

Scenario: Verify SORTING transformer with ASCENDING order
When I initialize scenario variable `sortingTable` with values:
{transformer=SORTING, byColumns=key1|key2, order=ASCENDING}
|key1|key2|key3|
|bb  |d   |1   |
|ba  |c   |2   |
|bb  |b   |3   |
|aa  |a   |4   |
Then `${sortingTable}` is equal to table:
|key1|key2|key3|
|aa  |a   |4   |
|ba  |c   |2   |
|bb  |b   |3   |
|bb  |d   |1   |

Scenario: Verify SORTING transformer with DESCENDING order
When I initialize scenario variable `sortingTable` with values:
{transformer=SORTING, byColumns=key1|key2, order=DESCENDING}
|key1|key2|key3|
|bb  |d   |1   |
|ba  |c   |2   |
|bb  |b   |3   |
|aa  |a   |4   |
Then `${sortingTable}` is equal to table:
|key1|key2|key3|
|bb  |d   |1   |
|bb  |b   |3   |
|ba  |c   |2   |
|aa  |a   |4   |

Scenario: Verify SORTING transformer with DESCENDING order and NUMBER sorting type
When I initialize scenario variable `sortingTable` with values:
{transformer=SORTING, byColumns=key2, order=DESCENDING, sortingTypes=NUMBER}
|key1|key2|
|a   |10  |
|b   |2.3 |
|c   |10.2|
Then `${sortingTable}` is equal to table:
|key1|key2|
|c   |10.2|
|a   |10  |
|b   |2.3 |

Scenario: Verify SORTING transformer with DESCENDING order and STRING sorting type
When I initialize scenario variable `sortingTable` with values:
{transformer=SORTING, byColumns=key2, order=DESCENDING, sortingTypes=STRING}
|key1|key2|
|a   |10  |
|b   |2.3 |
|c   |10.2|
Then `${sortingTable}` is equal to table:
|key1|key2|
|b   |2.3 |
|c   |10.2|
|a   |10  |

Scenario: Verify SORTING transformer with DESCENDING order and default sorting type
When I initialize scenario variable `sortingTable` with values:
{transformer=SORTING, byColumns=key2, order=DESCENDING}
|key1|key2|
|a   |10  |
|b   |2.3 |
|c   |10.2|
Then `${sortingTable}` is equal to table:
|key1|key2|
|b   |2.3 |
|c   |10.2|
|a   |10  |

Scenario: Verify SORTING transformer with default order and STRING|NUMBER sorting types
When I initialize scenario variable `sortingTable` with values:
{transformer=SORTING, byColumns=key1|key2, sortingTypes=STRING|NUMBER}
|key1|key2|
|a1  |10  |
|a1  |2.3 |
|a2  |10.2|
Then `${sortingTable}` is equal to table:
|key1|key2|
|a1  |2.3 |
|a1  |10  |
|a2  |10.2|
