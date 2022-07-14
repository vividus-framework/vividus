Scenario: Verify image based visual checks
When I execute HTTP GET request for resource with URL `https://raw.githubusercontent.com/vividus-framework/vividus/master/vividus-tests/src/main/resources/baselines/context.png`
When I compare_against baseline with name `context` from image `${response-as-bytes}`
When I compare_against baseline with name `context` from image `${response-as-bytes}` using storage `filesystem`
When I compare_against baseline with name `context-element-with-acceptable-diff-percentage` from image `${response-as-bytes}` ignoring:
|ACCEPTABLE_DIFF_PERCENTAGE|
|100                       |
When I compare_against baseline with name `context-element-with-acceptable-diff-percentage` from image `${response-as-bytes}` using storage `filesystem` and ignoring:
|ACCEPTABLE_DIFF_PERCENTAGE|
|100                       |
