Description: Integration tests for various Vividus expressions

Meta:
    @epic vividus-bdd-engine
    @feature expressions

Scenario: [Deprecated] Verify date generation and format
When I initialize the scenario variable `currentDate` with value `#{P}`
Then `#{formatDateTo(${currentDate}, yyyy-MM-dd, yyyy)}` is equal to `#{P(yyyy)}`

Scenario: Verify date generation and format
Then `#{formatDateTo(#{generateDate(P)}, yyyy-MM-dd, yyyy)}` is equal to `#{generateDate(P, yyyy)}`

Scenario: Verify epoch generation and conversion
When I initialize the SCENARIO variable `date` with value `#{generateDate(P, yyyy-MM-dd'T'HH:mm:ss)}`
When I initialize the SCENARIO variable `epoch` with value `#{toEpochSecond(${date})}`
Then `${date}` is equal to `#{fromEpochSecond(${epoch})}`

Scenario: Verify anyOf expression
Then `#{anyOf(1, 2\,3,3)}` matches `1|2,3|3`

Scenario: Verify diffDate with formatting
Then `777` is = `#{diffDate(2019-01-01T12:00:00.223Z,yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:00:01.000Z,yyyy-MM-dd'T'HH:mm:ss.SSSVV, milliS)}`
