Meta:
    @epic vividus-plugin-aws-s3
    @requirementId 1175

Scenario: Create new object
When I initialize the story variable `object-key` with value `object-key-#{generate(regexify '[a-z]{4}')}`
When I upload data `Hello from Vividus!` with key `${object-key}` and content type `text/plain` to S3 bucket `vividus-bucket`

Scenario: Fetch the created object
When I fetch object with key `${object-key}` from S3 bucket `vividus-bucket` and save result to scenario variable `content`
Then `${content}` is equal to `Hello from Vividus!`

Scenario: Set the created object ACL
Meta:
    @requirementId 1210
When I set ACL `aws-exec-read` for object with key `${object-key}` from S3 bucket `vividus-bucket`

Scenario: Delete the created object
When I delete object with key `${object-key}` from S3 bucket `vividus-bucket`
