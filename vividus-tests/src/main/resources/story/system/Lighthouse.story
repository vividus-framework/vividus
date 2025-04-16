Meta:
    @epic vividus-plugin-lighthouse

Scenario: Step verification: When I perform Lighthouse scan on $strategy for `$url` page:$rules
When I perform Lighthouse desktop scan of `${vividus-test-site-url}` page:
|metric                  |rule        |threshold|
|First Contentful Paint  |LESS_THAN   |2500     |
|Largest Contentful Paint|LESS_THAN   |2500     |
|Total Blocking Time     |LESS_THAN   |500      |
|Cumulative Layout Shift |LESS_THAN   |500      |
|Speed Index             |LESS_THAN   |1500     |
|Accessibility Score     |GREATER_THAN|85       |
|Best Practices Score    |GREATER_THAN|90       |
|Performance Score       |GREATER_THAN|90       |
|SEO Score               |GREATER_THAN|85       |


Scenario: Step verification: Then Lighthouse $scanType audit scores for `$checkpointPage` page are not less than for `$baselinePage` page
Then Lighthouse desktop audit scores for `${vividus-test-site-url}` page are not less than for `${vividus-test-site-url}` page


Scenario: Step verification: When I perform local Lighthouse scan of `$webPageUrl` page with options `$options`:$metricsValidations
When I perform local Lighthouse scan of `${vividus-test-site-url}` page with options `--only-categories=performance --preset=desktop --chrome-flags="--no-sandbox --headless --disable-dev-shm-usage"`:
|metric                  |rule        |threshold|
|First Contentful Paint  |LESS_THAN   |2500     |
|Largest Contentful Paint|LESS_THAN   |2500     |
|Total Blocking Time     |LESS_THAN   |500      |
|Cumulative Layout Shift |LESS_THAN   |500      |
|Speed Index             |LESS_THAN   |1500     |
