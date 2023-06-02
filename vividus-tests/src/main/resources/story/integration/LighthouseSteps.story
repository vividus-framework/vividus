Meta:
    @epic vividus-plugin-lighthouse

Scenario: Step verification: When I perform Lighthouse scan on $strategy for `$url` page:$rules
When I perform Lighthouse desktop scan of `${vividus-test-site-url}` page:
|metric                  |rule     |threshold|
|First Contentful Paint  |LESS_THAN|2500     |
|Largest Contentful Paint|LESS_THAN|2500     |
|Total Blocking Time     |LESS_THAN|500      |
|Cumulative Layout Shift |LESS_THAN|500      |
|Speed Index             |LESS_THAN|1500     |
