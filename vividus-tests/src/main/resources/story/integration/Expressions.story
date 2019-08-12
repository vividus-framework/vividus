Description: Integration tests for various Vividus expressions

Meta:
    @group expressions

Scenario: [Deprecated] Verify date generation and format
When I initialize the scenario variable `currentDate` with value `#{P}`
Then `#{formatDateTo(${currentDate}, yyyy-MM-dd, yyyy)}` is equal to `#{P(yyyy)}`

Scenario: Verify date generation and format
Then `#{formatDateTo(#{generateDate(P)}, yyyy-MM-dd, yyyy)}` is equal to `#{generateDate(P, yyyy)}`
