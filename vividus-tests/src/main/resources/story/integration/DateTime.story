Meta:
    @epic vividus-plugin-datetime

Scenario: Validate step 'Then the date '$date1' is $comparisonRule the date '$date2''
Then the date '2021-04-20T01:02:03.004Z' is greater than the date '2021-04-20T01:02:03.003Z'
Then the date '2021-04-19' is less than the date '2021-04-20'
