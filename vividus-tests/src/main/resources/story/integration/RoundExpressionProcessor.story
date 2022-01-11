Description: Integration tests for RoundExpressionProcessor class

Meta:
    @epic vividus-engine
    @feature expressions

Scenario: Verify rounding works correctly

Then `#{round(-5.9, 0)}` is equal to `-6`
Then `#{round(-5.5, 0)}` is equal to `-5`
Then `#{round(5.5, 0)}` is equal to `6`
Then `#{round(5.9, 0)}` is equal to `6`
Then `#{round(-5.59, 1)}` is equal to `-5.6`
Then `#{round(-5.55, 1)}` is equal to `-5.5`
Then `#{round(5.55, 1)}` is equal to `5.6`
Then `#{round(5.59, 1)}` is equal to `5.6`
Then `#{round(-5.559)}` is equal to `-5.56`
Then `#{round(-5.555)}` is equal to `-5.55`
Then `#{round(5.555)}` is equal to `5.56`
Then `#{round(5.559)}` is equal to `5.56`
Then `#{round(0.0)}` is equal to `0`
Then `#{round(5.0)}` is equal to `5`
Then `#{round(-0.9, 0)}` is equal to `-1`
Then `#{round(-0.5, 0)}` is equal to `0`
Then `#{round(1.4, 0, ceiling)}` is equal to `2`
Then `#{round(-1.6, 0, ceiling)}` is equal to `-1`
Then `#{round(1.6, 0, floor)}` is equal to `1`
Then `#{round(-1.4, 0, floor)}` is equal to `-2`
Then `#{round(1.1, 0, up)}` is equal to `2`
Then `#{round(-1.1, 0, up)}` is equal to `-2`
Then `#{round(1.6, 0, down)}` is equal to `1`
Then `#{round(-1.6, 0, down)}` is equal to `-1`
Then `#{round(2.5, 0, half_up)}` is equal to `3`
Then `#{round(-2.5, 0, half_up)}` is equal to `-3`
Then `#{round(-2.5, 0, half up)}` is equal to `-3`
Then `#{round(2.5, 0, half_down)}` is equal to `2`
Then `#{round(-2.5, 0, half_down)}` is equal to `-2`
Then `#{round(2.5, 0, half down)}` is equal to `2`
Then `#{round(5.5, 0, half_even)}` is equal to `6`
Then `#{round(2.5, 0, half_even)}` is equal to `2`
Then `#{round(1.5, 0, half_even)}` is equal to `2`
Then `#{round(1.5, 0, half even)}` is equal to `2`
Then `#{round(-5.5, 0, half_even)}` is equal to `-6`
Then `#{round(-2.5, 0, half_even)}` is equal to `-2`
Then `#{round(-1.5, 0, half_even)}` is equal to `-2`
Then `#{round(-5.5, 0, half even)}` is equal to `-6`
Then `#{round(-1.1, 2, unnecessary)}` is equal to `-1.1`
Then `#{round(0.15237E2, 2)}` is equal to `15.24`
