Scenario: Verify step: "Then response archive contains entries with the names:$parameters"
Meta:
    @requirementId 2545
!-- Deprecated
When I execute HTTP GET request for resource with URL `${vividus-test-site-url}/api/zip-archive`
Then response archive contains entries with names:
|rule             |name                |
|MATCHES          |.+\.data            |
|DOES_NOT_CONTAIN |restrictedData.data |
Then response archive contains entries with names:
|name                         |
|txtFileFromZipArchive.txt    |
|emptyDataFromZipArchive.data |

Scenario: Verify step: "Then `$archiveData` archive contains entries with the names:$parameters"
Meta:
    @requirementId 2545
When I execute HTTP GET request for resource with URL `${vividus-test-site-url}/api/zip-archive`
Then `${response-as-bytes}` archive contains entries with names:
|rule             |name                |
|MATCHES          |.+\.data            |
|DOES_NOT_CONTAIN |restrictedData.data |
Then `${response-as-bytes}` archive contains entries with names:
|name                         |
|txtFileFromZipArchive.txt    |
|emptyDataFromZipArchive.data |

Scenario: Verify step: "When I save content of `$archiveData` archive entries to variables:$parameters"
When I save content of `${response-as-bytes}` archive entries to variables:
|path                     |variableName|scopes  |outputFormat|
|txtFileFromZipArchive.txt|text        |SCENARIO|TEXT        |
|txtFileFromZipArchive.txt|base64      |SCENARIO|BASE64      |
Then `${text}` is = `Response text from ZIP archive`
Then `${base64}` is = `UmVzcG9uc2UgdGV4dCBmcm9tIFpJUCBhcmNoaXZl`

Scenario: Verify step: "When I save content of the response archive entries to the variables:$parameters"
!-- Deprecated
When I save content of the response archive entries to the variables:
|path                     |variableName|scopes  |outputFormat|
|txtFileFromZipArchive.txt|text        |SCENARIO|TEXT        |
|txtFileFromZipArchive.txt|base64      |SCENARIO|BASE64      |
Then `${text}` is = `Response text from ZIP archive`
Then `${base64}` is = `UmVzcG9uc2UgdGV4dCBmcm9tIFpJUCBhcmNoaXZl`
