Meta:
    @epic vividus-plugin-aws-lambda
    @requirementId 1132

Scenario: Invoke AWS Lambda
When I invoke AWS Lambda function `arn:aws:lambda:us-east-1:681188967882:function:hello-world` with payload `
{
  "key1": "value1",
  "key2": "value2",
  "key3": "value3"
}
` and save result to scenario variable `function-result`
Then `${function-result.payload}` is equal to `"value1"`
Then `${function-result.status-code}` is equal to `200`
Then `${function-result.executed-version}` is equal to `$LATEST`
