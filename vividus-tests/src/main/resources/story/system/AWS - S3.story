Meta:
    @epic vividus-plugin-aws-s3
    @requirementId 1175

Scenario: Create new object
Given I initialize story variable `start-time` with value `#{generateDate(PT0S)}`
Given I initialize story variable `prefix` with value `folder/object-key-#{generate(regexify '[a-z]{2}')}`
Given I initialize story variable `object-key` with value `${prefix}#{generate(regexify '[a-z]{2}')}`
When I upload data `Hello from Vividus!` with key `${object-key}.txt` and content type `text/plain` to S3 bucket `vividus-bucket`
When I upload data `#{loadResource(/data/csv.csv)}` with key `${object-key}.csv` and content type `text/csv` to S3 bucket `vividus-bucket`

Scenario: Fetch the created object
When I fetch object with key `${object-key}.txt` from S3 bucket `vividus-bucket` and save result to scenario variable `content`
Then `${content}` is equal to `Hello from Vividus!`

Scenario: Set the created object ACL
Meta:
    @requirementId 1210
When I set ACL `aws-exec-read` for object with key `${object-key}.txt` from S3 bucket `vividus-bucket`

Scenario: Collect object keys
Meta:
    @requirementId 1313; 1315
When I collect objects keys filtered by:
|filterType                      |filterValue   |
|key prefix                      |${prefix}     |
in S3 bucket `vividus-bucket` and save result to scenario variable `keys`
Then `${keys[0]}` matches `${object-key}\.(txt|csv)`
Then `${keys[1]}` matches `${object-key}\.(txt|csv)`
When I collect objects keys filtered by:
|filterType                      |filterValue   |
|key suffix                      |.csv          |
|object modified not earlier than|${start-time} |
in S3 bucket `vividus-bucket` and save result to scenario variable `keys2`
Then `${keys2[0]}` is equal to `${object-key}.csv`

Scenario: Delete the created object
When I delete object with key `${object-key}.txt` from S3 bucket `vividus-bucket`
When I delete object with key `${object-key}.csv` from S3 bucket `vividus-bucket`
