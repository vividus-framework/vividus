Description: Integration tests for RedirectValidationSteps class

Meta:
    @epic vividus-plugin-rest-api

Scenario: Step verification "Then I validate HTTP redirects: $expectedRedirects" with redirects number
Then I validate HTTP redirects:
|startUrl                              |endUrl                              |redirectsNumber |
|${vividus-test-site-url}/api/redirect |${vividus-test-site-url}/index.html |1               |

Scenario: Step verification "Then I validate HTTP redirects: $expectedRedirects" with status code
Then I validate HTTP redirects:
|startUrl                              |endUrl                              |statusCodes |
|${vividus-test-site-url}/api/redirect |${vividus-test-site-url}/index.html |302         |

Scenario: Step verification "Then I validate HTTP redirects: $expectedRedirects" without optional parameters
Then I validate HTTP redirects:
|startUrl                              |endUrl                              |
|${vividus-test-site-url}/api/redirect |${vividus-test-site-url}/index.html |
