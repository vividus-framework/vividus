Scenario: Verify image based visual checks
Given I initialize scenario variable `image-as-bytes` with value `#{loadBinaryResource(/baselines/context.png)}`
When I compare_against baseline with name `context` from image `${image-as-bytes}`
When I compare_against baseline with name `context` from image `${image-as-bytes}` using storage `filesystem`
When I compare_against baseline with name `context-element-with-acceptable-diff-percentage` from image `${image-as-bytes}` ignoring:
|ACCEPTABLE_DIFF_PERCENTAGE|
|100                       |
When I compare_against baseline with name `context-element-with-acceptable-diff-percentage` from image `${image-as-bytes}` using storage `filesystem` and ignoring:
|ACCEPTABLE_DIFF_PERCENTAGE|
|100                       |
