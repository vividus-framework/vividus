Meta:
    @epic vividus-plugin-shell

Scenario: Validate shell commands execution steps

When I execute command `echo 'Hello World!'` and save result to scenario variable `result`
Then `${result.exit-code}` is = `0`
Then `${result.stderr}` is = ``
Then `${result.stdout}` matches `.*Hello World!.*`
When I execute command `bash -c 'echo "Hello World!"'` and save result to scenario variable `result`
Then `${result.exit-code}` is = `0`
Then `${result.stderr}` is = ``
Then `${result.stdout}` matches `.*Hello World!.*`
Given I initialize scenario variable `shell` with value `#{evalGroovy(return org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS ? 'powershell' : 'bash')}`
When I execute command `echo 'Hello World!'` using ${shell} and save result to scenario variable `result`
Then `${result.exit-code}` is = `0`
Then `${result.stderr}` is = ``
Then `${result.stdout}` matches `.*Hello World!.*`
