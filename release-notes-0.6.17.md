# Added

### #6274 [vividus] Add step to perform steps if variable is set
New step:
```gherkin
When variable `$name` is set I do:$stepsToExecute
```
The step executes provided sub-steps only if the specified variable is set. This is a counterpart to the existing step that executes sub-steps if a variable is *not* set.

### #6193 [vividus] Add ability to disable retrieval of property secrets from secret managers
New properties to enable/disable secret manager integrations:
```properties
secrets-manager.vault.enabled=true
secrets-manager.aws-secrets-manager.enabled=true
secrets-manager.azure-key-vault.enabled=true
```
When disabled, the corresponding secret manager will not be queried for property values. This is useful for local development and debugging without access to external secret managers.

### #6387, #6390 [vividus-plugin-web-app] Add ability to execute steps in native context in mobile web tests
New steps for mobile web testing:
```gherkin
When I switch to native context
When I switch to web view with name that $comparisonRule `$value`
When I execute steps in native context:$stepsToExecute
```
The `When I execute steps in native context` step switches to the mobile native context, executes the provided sub-steps, and then automatically switches back to the original context.

### #6432 [vividus-plugin-web-app] Introduce CDP-based shooting strategy
New shooting strategy `CDP` using Chrome DevTools Protocol for capturing screenshots. This strategy leverages browser-native capabilities for efficient and accurate screenshot capture.
> **IMPORTANT:** Can be used only in Chrome/Chromium-based browsers.

### #6378 [vividus-plugin-web-app] Use Edge CLI args and experimental options for remote sessions
Edge remote sessions now support CLI arguments and experimental options, providing feature parity with Chrome browser configuration for remote sessions.

### #6433 [vividus-plugin-visual] Add ability to mask text in visual web checks
New screenshot configuration parameter:
- `textToMask` - The text to be masked with a placeholder before capturing the page screenshot and reverted back after capturing is finished. *(Web only)*

### #6445 [vividus-plugin-visual] Add extended failure message for visual checks
New property:
```properties
ui.visual.extended-assertion-format-enabled=false
```
When enabled, assertion messages include the baseline name for easier debugging, e.g. `Visual check for 'login-page' baseline is failed`.

### #6367 [vividus-plugin-visual] Save baseline comparison delta into a separate folder
New property:
```properties
ui.visual.baseline-storage.filesystem.delta-folder=C:/Workspace/vividus-tests/src/main/resources/delta
```
The path to the folder for saving baseline comparison deltas. Unlike the baseline folder, the delta folder contains only mismatched baselines, simplifying their management. This property requires `ui.visual.override-baselines` to be set to `true`, otherwise it has no effect.

### #6335 [vividus-plugin-visual] Handle path separators in visual baselines names as separate folders
Baseline names with path separators (e.g., `folder/images/full-page`) now create corresponding folder hierarchies on the filesystem, improving organization of visual baselines.

### #6295 [vividus-plugin-yaml] Add step to save number of YAML elements
New step:
```gherkin
When I save number of elements from `$yaml` by YAML path `$yamlPath` to $scopes variable `$variableName`
```

### #6235 [vividus-plugin-ssh] Add step for SSH port forwarding
New step:
```gherkin
When I forward port through SSH using parameters:$parameters
```
The step establishes SSH port forwarding (local port tunneling) using the specified parameters. Parameters include `local-port`, `remote-port`, `remote-host` along with standard SSH connection parameters (`username`, `password`, `host`, `port`, etc.). Sessions are automatically closed after all stories complete.

### #6198 [vividus-plugin-redis] Add step to flush Redis DB keys
New step:
```gherkin
When I flush `$dbIndex` database on `$instanceKey` Redis instance
```
Flushes (removes all keys from) the specified Redis database on a given Redis instance.

### #6236 [vividus-bom] Add popular JDBC drivers dependencies to BOM
Popular JDBC drivers are now included in the VIVIDUS BOM, so users no longer need to specify exact versions for drivers like MySQL, PostgreSQL, SQL Server, Oracle, Snowflake, H2, DB2, and CSV JDBC.

# Changed

### #6470 [vividus] Migrate to Spring 7 and Spring Boot 4
VIVIDUS has been migrated to Spring Framework 7 and Spring Boot 4.

### #6217 [vividus] Migrate to JUnit 6
The testing framework has been migrated from JUnit 5 to JUnit 6.

### #6383, #6384, #6385, #6228 [vividus-plugin-json] Migrate to Jackson 3 and json-schema-validator 2.0.0
The JSON plugin has been migrated to Jackson 3 and `com.networknt:json-schema-validator` 2.0.0. This includes migration of JSON expression processor, matcher, schema validation steps, and steps for patching JSONs.

### #6327, #6379, #6412, #6456 [vividus-plugin-web-app] Update Selenium DevTools to v143
Selenium DevTools has been updated from v142 to v143, providing support for Chrome v143+ and bumped to 4.41.0.

### #6353 [vividus-plugin-visual] Avoid baseline overrides for passed comparisons
When `ui.visual.override-baselines` is set to `true`, only **failed** comparisons will update baselines. Previously, baselines were overridden for both passed and failed comparisons.

### #6343 [vividus-plugin-web-app-to-rest-api] Keep order of URLs in `FROM_SITEMAP` and `FROM_HEADLESS_CRAWLING` transformers
URLs discovered via `FROM_SITEMAP` and `FROM_HEADLESS_CRAWLING` transformers now maintain their original order for predictable test execution and reporting.

# Fixed

### #6472 [vividus-to-azure-devops-exporter] Add HTML escaping of special characters
Special characters (e.g., `<`, `>`, `&`) in test steps and descriptions are now properly HTML-escaped when exported to Azure DevOps, preventing malformed output.

### #6402 [vividus-allure-adaptor] Fix handling of invalid severity and priority meta values
Invalid `@severity` or `@priority` meta values (outside the valid range) no longer cause errors. Instead, a warning is logged, and the invalid value is skipped.

### #6394 [vividus-allure-adaptor] Fix displaying of JSONs with large numbers in report
Large numbers in JSON (e.g., IDs exceeding JavaScript's safe integer limit) are now displayed correctly in Allure report attachments.

### #6395 [vividus-plugin-web-app] Always consider mobile native elements configuration during screenshot strategy preparation
Mobile native elements configuration (header/footer cut settings) is now correctly applied for all screenshot strategies, not just the default one.

### #6275 [vividus-plugin-web-app] Fix execution of DevTools-based tests on Chrome 142+
DevTools-based tests are fixed to work correctly on Chrome version 142 and above.

### #6311 [vividus-engine] Fix reporting of nested ignorable steps
Nested ignorable steps (steps marked as ignorable that call other steps) are now properly reported in test reports.

### #6306 [vividus-engine] Skip parameter conversion for ignorable steps
Parameter conversion is now skipped for ignorable steps, preventing failures when ignorable steps contain invalid or unresolvable parameters.

### #6260 [vividus-plugin-aws-secrets-manager] Improve error message on missing resource
Error messages for missing AWS secrets now include the secret name and profile for easier troubleshooting.
